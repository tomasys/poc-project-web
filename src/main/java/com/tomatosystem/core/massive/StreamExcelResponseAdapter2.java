package com.tomatosystem.core.massive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleopatra.export.ExcelExporter;
import com.cleopatra.export.ExporterFactory;
import com.cleopatra.export.ExporterFactory.EXPORTTYPE;
import com.cleopatra.export.source.JSONDataSource;
import com.cleopatra.json.JSONArray;
import com.cleopatra.json.JSONObject;

/**
 * 신규 포맷 전용 Excel Export Adapter
 *
 * 수신 포맷:
 *   - gridInfo : header 렌더링용 (ExcelExporter 처리)
 *   - data     : {
 *       "layout": {
 *           "header":  [...],  ← 각 항목에 style 포함
 *           "detail":  [...],  ← 각 항목에 style 포함 (컬럼별로 다를 수 있음)
 *           "gfooter": [...],
 *           "footer":  [...]
 *       },  ← isFirstSend 시 1회
 *       "style": [...],  ← layout 없을 때 fallback (isFirstSend 시 1회)
 *       "data":  [ {"region":"detail|gfooter|gheader", "data":[...]} ]
 *     }
 *   - footer   : { "style": [...], "data": [[합계행...]] }
 *
 * 스타일 정책:
 *   - layout 각 항목의 style 객체를 컬럼별로 파싱하여 적용
 *   - layout 없을 때 서버 고정값 fallback
 *   - 테두리는 항상 서버 고정값
 *   - number 컬럼: 값에 소수점 있으면 소수 포맷, 없으면 정수 포맷 동적 적용
 *
 * 병합 정책:
 *   - layout의 colspan > 1 항목만 같은 행 내 컬럼 병합(colspan) 적용
 *   - rowspan은 SXSSF 구조상 미지원
 *   - 병합 없는 그리드는 hasMerge=false로 writeRow() 내 병합 로직 완전 스킵
 */
public class StreamExcelResponseAdapter2 {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // ========== 서버 고정 배경색 (layout style 없을 때 fallback) ==========
    private static final String BG_DETAIL    = "#FFFFFF";
    private static final String BG_GHEADER   = "#EEEEEE";
    private static final String BG_GFOOTER   = "#EFF5FF";
    private static final String BG_FOOTER    = "#D3E1FB";
    private static final String BG_HEADER    = "#DDDDDD";
    private static final String BORDER_COLOR = "#BBBBBB";

    // ========== 필드 ==========
    private String fileNm;
    private File tempFile;
    private FileOutputStream fileOutputStream;

    private SXSSFWorkbook workbook;
    private Sheet sheet;
    private int rowNum     = 0;
    private int sheetIndex = 0;
    private int headerNum  = 0;

    /**
     * region → 컬럼별 [정수스타일, 소수스타일] 쌍 리스트
     * - index 0: 정수 스타일
     * - index 1: 소수 스타일
     * - string 컬럼은 두 스타일 동일
     */
    private Map<String, List<CellStyle[]>> regionStylePairs = new HashMap<>();

    /** region → 병합 정보 리스트 (colspan > 1인 항목만) */
    private Map<String, List<MergeInfo>> regionMerges = new HashMap<>();

    /**
     * region → 컬럼별 ColumnMeta 리스트
     * region마다 별도 파싱 (컬럼별 style이 다를 수 있으므로)
     */
    private Map<String, List<ColumnMeta>> regionColumnMetas = new HashMap<>();

    /** detail 기준 컬럼 수 (다른 region fallback용) */
    private int totalColumnCount = 0;

    /** 초기화 완료 여부 */
    private boolean initialized = false;

    /**
     * 병합 존재 여부 플래그
     * layout 파싱 시 colspan > 1 항목이 하나라도 있으면 true
     */
    private boolean hasMerge = false;

    private static final int    MAX_ROWS_PER_SHEET  = 1000000;
    private static final int    SXSSF_WINDOW_SIZE   = 500;
    private static final int    BUFFER_SIZE         = 8192;
    private static final String TEMPLATE_SHEET_NAME = "header_template_for_copy";

    // ========== 내부 클래스 ==========

    /**
     * 컬럼 메타 정보
     * layout 각 항목에서 파싱: type, format, style 객체
     */
    private static class ColumnMeta {
        String type;       // "string" | "number"
        String format;     // 소수 포맷 (예: #,##0.##)
        String formatInt;  // 정수 포맷 (예: #,##0)
        JSONObject style;  // layout 항목의 style 객체 (없으면 null)

        ColumnMeta(String type, String format, String formatInt, JSONObject style) {
            this.type      = type;
            this.format    = format;
            this.formatInt = formatInt;
            this.style     = style;
        }
    }

    private static class MergeInfo {
        int colStart;
        int colEnd;

        MergeInfo(int colStart, int colEnd) {
            this.colStart = colStart;
            this.colEnd   = colEnd;
        }
    }

    // ========== 생성자 ==========

    public StreamExcelResponseAdapter2(String key, JSONObject requestData) throws IOException {
        this.fileNm = requestData.getString("fileName");
        this.tempFile = File.createTempFile(key, ".xlsx");
        this.fileOutputStream = new FileOutputStream(this.tempFile);

        JSONObject gridInfo   = (JSONObject) requestData.get("gridInfo");
        ExporterFactory factory   = ExporterFactory.getInstance();
        ExcelExporter exporter    = (ExcelExporter) factory.getExporter(EXPORTTYPE.SXLSX);
        JSONDataSource dataSource = new JSONDataSource(key, gridInfo);
        this.workbook  = (SXSSFWorkbook) exporter.export(dataSource);
        this.sheet     = this.workbook.getSheetAt(0);
        this.workbook.setSheetName(0, "sheet1");
        this.headerNum = this.sheet.getLastRowNum();
        this.rowNum    = this.headerNum + 1;

        createHeaderTemplateSheet();
    }

    // ========== 외부 호출 메서드 ==========

    public void processData(JSONObject dataObj) throws IOException {
        if (!initialized) {
            if (dataObj.has("layout")) {
                parseLayout((JSONObject) dataObj.get("layout"));
            } else {
                // layout 없을 때 fallback: style 배열 기반 파싱
                JSONArray styleArr = (JSONArray) dataObj.get("style");
                JSONArray dataArr  = (JSONArray) dataObj.get("data");
                int actualColCount = resolveActualColumnCount(styleArr, dataArr);
                List<ColumnMeta> metas = parseColumnMetasFromStyle(styleArr, actualColCount);
                // 모든 region에 동일 메타 적용
                String[] regions = {"detail", "gfooter", "gheader", "footer", "header"};
                for (String r : regions) {
                    regionColumnMetas.put(r, metas);
                }
                this.totalColumnCount = actualColCount;
                this.hasMerge = false;
                initRegionStylePairs();
            }
            initialized = true;
        }

        JSONArray dataArr = (JSONArray) dataObj.get("data");
        try {
            for (int i = 0; i < dataArr.length(); i++) {
                JSONObject rowObj = dataArr.getJSONObject(i);
                String region     = rowObj.getString("region");
                JSONArray rowData = rowObj.getJSONArray("data");

                // 미등록 region은 detail fallback
                List<CellStyle[]> stylePairs = regionStylePairs.containsKey(region)
                    ? regionStylePairs.get(region)
                    : regionStylePairs.get("detail");
                writeRow(rowData, stylePairs, region);
            }
        } catch (Exception e) {
            logger.error("data 처리 중 오류 발생", e);
            cleanupResources();
            throw new IOException("data 처리 실패", e);
        }
    }

    public void processFooter(JSONObject footerObj) throws IOException {
        if (!initialized) {
            JSONArray styleArr = (JSONArray) footerObj.get("style");
            List<ColumnMeta> metas = parseColumnMetasFromStyle(styleArr, styleArr.length());
            regionColumnMetas.put("footer", metas);
            this.totalColumnCount = styleArr.length();
            initRegionStylePairs();
            initialized = true;
        }

        JSONArray dataArr = (JSONArray) footerObj.get("data");
        try {
            for (int i = 0; i < dataArr.length(); i++) {
                writeRow(dataArr.getJSONArray(i), regionStylePairs.get("footer"), "footer");
            }
        } catch (Exception e) {
            logger.error("footer 처리 중 오류 발생", e);
            cleanupResources();
            throw new IOException("footer 처리 실패", e);
        }
    }

    // ========== layout 파싱 ==========

    /**
     * layout 객체에서 region별 ColumnMeta + 병합 정보 파싱
     * layout: { "detail":[...], "gfooter":[...], "gheader":[...], "footer":[...], "header":[...] }
     * 각 항목: { "colindex": N, "colspan": M, "type": "...", "format": "...", "style": {...} }
     */
    private void parseLayout(JSONObject layout) {
        String[] regions = {"detail", "gheader", "gfooter", "footer", "header"};

        for (String region : regions) {
            if (!layout.has(region)) continue;

            JSONArray items = (JSONArray) layout.get(region);
            List<MergeInfo> merges  = new ArrayList<>();
            int maxColCount = 0;

            // colindex 최대값 확인
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                int colIndex    = item.optInt("colindex", i);
                int colspan     = item.optInt("colspan", 1);
                maxColCount = Math.max(maxColCount, colIndex + colspan);

                if (colspan > 1) {
                    merges.add(new MergeInfo(colIndex, colIndex + colspan - 1));
                    this.hasMerge = true;
                }
            }
            regionMerges.put(region, merges);

            // 컬럼 메타 파싱 (style 포함)
            List<ColumnMeta> metas = new ArrayList<>();
            for (int i = 0; i < maxColCount; i++) {
                metas.add(new ColumnMeta("string", "", "", null)); // 기본값
            }
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                int colIndex    = item.optInt("colindex", i);
                if (colIndex >= maxColCount) continue;

                String type       = item.optString("type", "string");
                String rawFormat  = item.optString("format", "");
                JSONObject styleObj = item.has("style") ? item.getJSONObject("style") : null;

                metas.set(colIndex, new ColumnMeta(
                    type,
                    normalizeFormat(rawFormat),
                    normalizeFormatInt(rawFormat),
                    styleObj
                ));
            }
            regionColumnMetas.put(region, metas);

            // detail 기준으로 전체 컬럼 수 설정
            if ("detail".equals(region)) {
                this.totalColumnCount = maxColCount;
            }
        }

        // detail이 없으면 다른 region에서 최대 컬럼 수 추정
        if (this.totalColumnCount == 0) {
            for (List<ColumnMeta> metas : regionColumnMetas.values()) {
                this.totalColumnCount = Math.max(this.totalColumnCount, metas.size());
            }
        }

        // 미등록 region은 detail 메타로 채움
        List<ColumnMeta> detailMetas = regionColumnMetas.getOrDefault("detail",
            regionColumnMetas.values().iterator().next());
        for (String r : regions) {
            if (!regionColumnMetas.containsKey(r)) {
                regionColumnMetas.put(r, detailMetas);
            }
        }

        initRegionStylePairs();
        logger.debug("layout 파싱 완료: 컬럼={}, hasMerge={}", this.totalColumnCount, this.hasMerge);
    }

    // ========== 컬럼 메타 파싱 (style 배열 기반 fallback) ==========

    private List<ColumnMeta> parseColumnMetasFromStyle(JSONArray items, int columnCount) {
        List<ColumnMeta> metas = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            metas.add(new ColumnMeta("string", "", "", null));
        }
        for (int i = 0; i < items.length(); i++) {
            JSONObject item  = items.getJSONObject(i);
            int colIndex     = item.optInt("colindex", i);
            if (colIndex >= columnCount) continue;
            String type      = item.optString("type", "string");
            String rawFormat = item.optString("format", "");
            JSONObject styleObj = item.has("style") ? item.getJSONObject("style") : null;
            metas.set(colIndex, new ColumnMeta(
                type,
                normalizeFormat(rawFormat),
                normalizeFormatInt(rawFormat),
                styleObj
            ));
        }
        return metas;
    }

    private int resolveActualColumnCount(JSONArray styleArr, JSONArray dataArr) {
        int styleColCount = 0;
        for (int i = 0; i < styleArr.length(); i++) {
            int colIndex = styleArr.getJSONObject(i).optInt("colindex", i);
            styleColCount = Math.max(styleColCount, colIndex + 1);
        }
        for (int i = 0; i < dataArr.length(); i++) {
            JSONObject row = dataArr.getJSONObject(i);
            if ("detail".equals(row.optString("region"))) {
                return Math.max(styleColCount, row.getJSONArray("data").length());
            }
        }
        return styleColCount;
    }

    // ========== 포맷 변환 ==========

    /** s#,##0.99 → #,##0.## (소수 포맷) */
    private String normalizeFormat(String rawFormat) {
        if (rawFormat == null || rawFormat.isEmpty()) return "";
        String fmt = rawFormat.replaceAll("^[sS]", "");
        int dotIdx = fmt.indexOf('.');
        if (dotIdx >= 0) {
            return fmt.substring(0, dotIdx) + "." + fmt.substring(dotIdx + 1).replace('9', '#');
        }
        return fmt;
    }

    /** s#,##0.99 → #,##0 (정수 포맷, 소수점 이하 제거) */
    private String normalizeFormatInt(String rawFormat) {
        if (rawFormat == null || rawFormat.isEmpty()) return "";
        String fmt = rawFormat.replaceAll("^[sS]", "");
        int dotIdx = fmt.indexOf('.');
        return dotIdx >= 0 ? fmt.substring(0, dotIdx) : fmt;
    }

    // ========== 스타일 초기화 ==========

    private void initRegionStylePairs() {
        for (String region : regionColumnMetas.keySet()) {
            regionStylePairs.put(region, buildFixedStylePairs(region));
        }
    }

    /**
     * region별 [정수스타일, 소수스타일] 쌍 리스트 생성
     * 각 컬럼의 ColumnMeta.style 객체를 우선 적용, 없으면 서버 고정값
     */
    private List<CellStyle[]> buildFixedStylePairs(String region) {
        List<CellStyle[]> result  = new ArrayList<>();
        DataFormat dataFormat     = workbook.createDataFormat();
        List<ColumnMeta> metas    = regionColumnMetas.getOrDefault(region,
            regionColumnMetas.getOrDefault("detail", new ArrayList<>()));

        for (int colIdx = 0; colIdx < metas.size(); colIdx++) {
            ColumnMeta meta = metas.get(colIdx);

            XSSFCellStyle intStyle = buildBaseStyle(region, meta);
            XSSFCellStyle decStyle = buildBaseStyle(region, meta);

            if ("number".equals(meta.type)) {
                if (!meta.formatInt.isEmpty()) {
                    intStyle.setDataFormat(dataFormat.getFormat(meta.formatInt));
                }
                if (!meta.format.isEmpty()) {
                    decStyle.setDataFormat(dataFormat.getFormat(meta.format));
                }
            }

            result.add(new CellStyle[]{intStyle, decStyle});
        }
        return result;
    }

    /**
     * 컬럼별 기본 스타일 생성
     * - meta.style 있으면 해당 CSS 적용
     * - 없으면 서버 고정값 적용
     * - 테두리는 항상 서버 고정값
     */
    private XSSFCellStyle buildBaseStyle(String region, ColumnMeta meta) {
        XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font       = (XSSFFont) workbook.createFont();

        JSONObject css = meta.style;
        if (css != null) {
            // 배경색
            String bgColor = css.optString("background-color", getDefaultBg(region));
            if (!bgColor.startsWith("#")) {
            	bgColor = namedColorToHex(bgColor);
            }
            applyBgColor(style, bgColor);

            // 폰트 굵기
            font.setBold("bold".equals(css.optString("font-weight", "normal")));

            // 폰트 크기
            String fontSizeStr = css.optString("font-size", "11px").replace("px", "").trim();
            try {
                font.setFontHeightInPoints((short) Integer.parseInt(fontSizeStr));
            } catch (NumberFormatException e) {
                font.setFontHeightInPoints((short) 11);
            }

            // 글자색 (hex 및 named color 모두 지원)
            applyFontColor(font, css.optString("color", ""));

            // 텍스트 정렬
            style.setAlignment(toHorizontalAlignment(css.optString("text-align", "center")));
            style.setVerticalAlignment(toVerticalAlignment(css.optString("vertical-align", "middle")));

        } else {
            // 서버 고정값 fallback
            applyBgColor(style, getDefaultBg(region));
            font.setBold(!"detail".equals(region));
            style.setAlignment(HorizontalAlignment.CENTER);
            style.setVerticalAlignment(VerticalAlignment.CENTER);
        }

        // 테두리: 항상 서버 고정값
        XSSFColor border = toXSSFColor(BORDER_COLOR);
        style.setBorderTop(BorderStyle.THIN);    style.setTopBorderColor(border);
        style.setBorderBottom(BorderStyle.THIN); style.setBottomBorderColor(border);
        style.setBorderLeft(BorderStyle.THIN);   style.setLeftBorderColor(border);
        style.setBorderRight(BorderStyle.THIN);  style.setRightBorderColor(border);

        style.setFont(font);
        return style;
    }

    private String getDefaultBg(String region) {
        switch (region) {
            case "header":  return BG_HEADER;
            case "gheader": return BG_GHEADER;
            case "gfooter": return BG_GFOOTER;
            case "footer":  return BG_FOOTER;
            default:        return BG_DETAIL;
        }
    }

    // ========== 내부 처리 ==========

    private void writeRow(JSONArray rowData, List<CellStyle[]> stylePairs, String region) {
        if (this.rowNum > (MAX_ROWS_PER_SHEET + this.headerNum)) {
            switchToNextSheet();
        }

        int currentRow = this.rowNum;
        Row row = this.sheet.createRow(this.rowNum++);
        int cellCount = Math.min(rowData.length(), stylePairs.size());

        for (int j = 0; j < cellCount; j++) {
            Object value     = rowData.get(j);
            Cell cell        = row.createCell(j);
            setCellValue(cell, value);
            CellStyle[] pair = stylePairs.get(j);
            cell.setCellStyle(isDecimal(value) ? pair[1] : pair[0]);
        }

        if (this.hasMerge) {
            List<MergeInfo> merges = regionMerges.get(region);
            if (merges != null && !merges.isEmpty()) {
                for (MergeInfo m : merges) {
                    sheet.addMergedRegion(
                        new CellRangeAddress(currentRow, currentRow, m.colStart, m.colEnd)
                    );
                }
            }
        }
    }

    private boolean isDecimal(Object value) {
        if (value instanceof Double)     return ((Double) value) % 1 != 0;
        if (value instanceof BigDecimal) return ((BigDecimal) value).stripTrailingZeros().scale() > 0;
        return false;
    }

    // ========== 시트 전환 ==========

    private void switchToNextSheet() {
        this.sheetIndex++;
        this.sheet = this.workbook.createSheet("Sheet" + (this.sheetIndex + 1));
        this.rowNum = 0;
        copyHeadersFromTemplate(this.sheet);
        this.rowNum = this.sheet.getLastRowNum() + 1;
    }

    private void createHeaderTemplateSheet() {
        Sheet templateSheet = this.workbook.createSheet(TEMPLATE_SHEET_NAME);
        if (templateSheet instanceof SXSSFSheet) {
            ((SXSSFSheet) templateSheet).setRandomAccessWindowSize(-1);
        }

        int lastHeaderRow = this.sheet.getLastRowNum();
        for (int i = 0; i <= lastHeaderRow; i++) {
            Row sourceRow = this.sheet.getRow(i);
            if (sourceRow == null) continue;
            Row destRow = templateSheet.createRow(i);
            destRow.setHeight(sourceRow.getHeight());
            short lastCellNum = sourceRow.getLastCellNum();
            for (int j = 0; j < lastCellNum; j++) {
                Cell sourceCell = sourceRow.getCell(j);
                if (sourceCell == null) continue;
                Cell destCell = destRow.createCell(j);
                destCell.setCellStyle(sourceCell.getCellStyle());
                copyCellValue(sourceCell, destCell);
                templateSheet.setColumnWidth(j, this.sheet.getColumnWidth(j));
            }
        }
        this.workbook.setSheetHidden(this.workbook.getSheetIndex(templateSheet), true);
    }

    private void copyHeadersFromTemplate(Sheet newSheet) {
        Sheet templateSheet = this.workbook.getSheet(TEMPLATE_SHEET_NAME);
        if (templateSheet == null) return;

        int lastHeaderRow = templateSheet.getLastRowNum();
        for (int i = 0; i <= lastHeaderRow; i++) {
            Row sourceRow = templateSheet.getRow(i);
            if (sourceRow == null) continue;
            Row destRow = newSheet.createRow(i);
            destRow.setHeight(sourceRow.getHeight());
            short lastCellNum = sourceRow.getLastCellNum();
            for (int j = 0; j < lastCellNum; j++) {
                Cell sourceCell = sourceRow.getCell(j);
                if (sourceCell == null) continue;
                Cell destCell = destRow.createCell(j);
                destCell.setCellStyle(sourceCell.getCellStyle());
                copyCellValue(sourceCell, destCell);
                newSheet.setColumnWidth(j, templateSheet.getColumnWidth(j));
            }
        }
    }

    private void copyCellValue(Cell src, Cell dest) {
        switch (src.getCellType()) {
            case NUMERIC:  dest.setCellValue(src.getNumericCellValue()); break;
            case BOOLEAN:  dest.setCellValue(src.getBooleanCellValue()); break;
            case FORMULA:  dest.setCellFormula(src.getCellFormula());    break;
            case STRING:   dest.setCellValue(src.getStringCellValue());  break;
            default: break;
        }
    }

    // ========== 응답 전송 ==========

    public void endResponseMassive(HttpServletResponse response) {
        OutputStream out = null;

        try {
            this.workbook.write(this.fileOutputStream);
            this.fileOutputStream.flush();
            this.fileOutputStream.close();
            this.workbook.close();

            String encodedFileName = URLEncoder.encode(this.fileNm + ".xlsx", "UTF-8");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            out = response.getOutputStream();

            try (FileInputStream fis = new FileInputStream(this.tempFile)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            out.flush();

        } catch (IOException e) {
            logger.error("Excel 파일 응답 중 오류 발생", e);
        } finally {
            cleanupResources();
        }
    }

    // ========== 유틸리티 ==========

    private void setCellValue(Cell cell, Object value) {
        if (value == null)               { cell.setCellValue(""); return; }
        if (value instanceof Integer)    { cell.setCellValue(((Integer) value).intValue()); return; }
        if (value instanceof Long)       { cell.setCellValue(((Long) value).longValue()); return; }
        if (value instanceof Double)     { cell.setCellValue(((Double) value).doubleValue()); return; }
        if (value instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) value;
            cell.setCellValue(bd.stripTrailingZeros().scale() <= 0 ? bd.longValue() : bd.doubleValue());
            return;
        }
        if (value instanceof Boolean)    { cell.setCellValue((Boolean) value); return; }
        cell.setCellValue(value.toString());
    }

    private void applyBgColor(XSSFCellStyle style, String hex) {
        try {
            style.setFillForegroundColor(toXSSFColor(hex));
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        } catch (Exception e) {
            logger.warn("배경색 적용 실패 - hex: {}", hex);
        }
    }

    private void applyFontColor(XSSFFont font, String color) {
        if (color == null || color.isEmpty()) return;
        try {
            if (color.startsWith("#")) {
                font.setColor(toXSSFColor(color));
            } else {
                String hex = namedColorToHex(color);
                if (hex != null) {
                    font.setColor(toXSSFColor(hex));
                }
            }
        } catch (Exception e) {
            logger.warn("폰트 색상 적용 실패 - color: {}", color);
        }
    }

    private String namedColorToHex(String name) {
        if (name == null) return null;
        switch (name.toLowerCase()) {
            case "black":   return "#000000";
            case "white":   return "#FFFFFF";
            case "red":     return "#FF0000";
            case "green":   return "#008000";
            case "blue":    return "#0000FF";
            case "yellow":  return "#FFFF00";
            case "orange":  return "#FFA500";
            case "purple":  return "#800080";
            case "gray":
            case "grey":    return "#808080";
            case "pink":    return "#FFC0CB";
            case "navy":    return "#000080";
            case "teal":    return "#008080";
            default:        return null;
        }
    }

    private XSSFColor toXSSFColor(String hex) {
        java.awt.Color c = java.awt.Color.decode(hex);
        return new XSSFColor(
            new byte[]{(byte) c.getRed(), (byte) c.getGreen(), (byte) c.getBlue()},
            new DefaultIndexedColorMap()
        );
    }

    private HorizontalAlignment toHorizontalAlignment(String value) {
        if (value == null) return HorizontalAlignment.CENTER;
        switch (value.toLowerCase()) {
            case "left":  return HorizontalAlignment.LEFT;
            case "right": return HorizontalAlignment.RIGHT;
            default:      return HorizontalAlignment.CENTER;
        }
    }

    private VerticalAlignment toVerticalAlignment(String value) {
        if (value == null) return VerticalAlignment.CENTER;
        switch (value.toLowerCase()) {
            case "top":    return VerticalAlignment.TOP;
            case "bottom": return VerticalAlignment.BOTTOM;
            default:       return VerticalAlignment.CENTER;
        }
    }

    private void cleanupResources() {
        if (this.fileOutputStream != null) {
            try { this.fileOutputStream.close(); } catch (IOException e) { logger.warn("FileOutputStream 닫기 실패", e); }
        }
        if (this.workbook != null) {
            try { this.workbook.close(); this.workbook.dispose(); }
            catch (IOException e) { logger.warn("Workbook 닫기 실패", e); }
            this.workbook = null;
        }
        if (this.tempFile != null && this.tempFile.exists()) {
            this.tempFile.delete();
            this.tempFile = null;
        }
    }
}