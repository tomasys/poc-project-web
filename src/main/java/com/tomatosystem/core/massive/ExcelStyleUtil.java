package com.tomatosystem.core.massive;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

/**
 * Apache POI를 사용하여 엑셀 셀 스타일을 적용하는 유틸리티 클래스.
 * 셀 스타일을 캐싱하여 재사용함으로써 대용량 엑셀 생성 시 성능을 향상시킵니다.
 */
public class ExcelStyleUtil {

    // 워크북별로 스타일 캐시를 관리하여 동일한 스타일 객체의 중복 생성을 방지합니다.
    private static final Map<Workbook, Map<String, CellStyle>> WORKBOOK_STYLE_CACHE = new HashMap<>();

    /**
     * 셀에 스타일을 적용합니다.
     * 내부적으로 getOrCreateCellStyle를 호출하여 스타일을 가져온 후 셀에 설정합니다.
     * @param sheet 현재 시트
     * @param cell 스타일을 적용할 셀
     * @param styleContainer 스타일 정보(style, format, type 등)가 포함된 Map
     */
    public static void applyCellStyle(Sheet sheet, Cell cell, Map<String, Object> styleContainer) {
        CellStyle cellStyle = getOrCreateCellStyle(sheet, styleContainer);
        cell.setCellStyle(cellStyle);
    }

    /**
     * 스타일 정보를 바탕으로 CellStyle 객체를 생성하거나 캐시에서 가져옵니다.
     * 이 메서드는 스타일 객체만 반환하며, 셀에 직접 적용하지 않습니다.
     * 대용량 데이터 처리 시, 컬럼별 스타일을 미리 생성해두는 최적화에 사용됩니다.
     *
     * @param sheet 워크북에 접근하기 위한 시트 객체
     * @param styleContainer 스타일 정보(style, format, type 등)를 담고 있는 Map
     * @return 생성되거나 캐시된 CellStyle 객체
     */
    public static CellStyle getOrCreateCellStyle(Sheet sheet, Map<String, Object> styleContainer) {
        Workbook workbook = sheet.getWorkbook();

        @SuppressWarnings("unchecked")
        Map<String, Object> styleMap = (Map<String, Object>) styleContainer.get("style");
        if (styleMap == null) {
            styleMap = new HashMap<>();
        }

        // 워크북에 해당하는 스타일 캐시를 가져오거나 새로 생성합니다.
        Map<String, CellStyle> styleCache = WORKBOOK_STYLE_CACHE.computeIfAbsent(workbook, k -> new HashMap<>());

        // 스타일 속성을 기반으로 고유한 캐시 키를 생성합니다.
        String styleKey = generateStyleKey(styleMap, styleContainer);

        // 캐시에 이미 스타일이 존재하면 즉시 반환합니다.
        if (styleCache.containsKey(styleKey)) {
            return styleCache.get(styleKey);
        }

        // 캐시에 없는 경우, 새로운 스타일과 폰트를 생성합니다.
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();

        // 배경색 적용
        if (styleMap.containsKey("background-color")) {
            String colorHex = (String) styleMap.get("background-color");
            XSSFColor bgColor = getSpreadSheetColor(colorHex, workbook);
            if (bgColor != null) {
                cellStyle.setFillForegroundColor(bgColor);
                cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }
        }

        // 폰트 크기 및 스타일 적용
        if (styleMap.containsKey("font-size")) {
            font.setFontHeightInPoints(Short.parseShort(styleMap.get("font-size").toString().replace("px", "")));
        }
        if ("bold".equals(styleMap.get("font-weight"))) {
            font.setBold(true);
        }

        // 정렬 적용
        if (styleMap.containsKey("text-align")) {
            cellStyle.setAlignment(getHorizontalAlignment((String) styleMap.get("text-align")));
        }
        if (styleMap.containsKey("vertical-align")) {
            cellStyle.setVerticalAlignment(getVerticalAlignment((String) styleMap.get("vertical-align")));
        }

        // 테두리 스타일 적용
        for (String styleName : styleMap.keySet()) {
            String styleValue = styleMap.get(styleName).toString();
            if (styleName.startsWith("border-")) {
                setBorderStyle(cellStyle, styleName, styleValue, sheet);
            }
        }

        // 폰트 설정 적용
        cellStyle.setFont(font);

        // 숫자/날짜 포맷 적용
        if (styleContainer.containsKey("format") && styleContainer.containsKey("type")) {
            applyDataFormat(workbook, cellStyle, styleContainer);
        }

        // 생성된 스타일을 캐시에 저장하고 반환합니다.
        styleCache.put(styleKey, cellStyle);
        return cellStyle;
    }


    /**
     * 스타일 정보와 포맷 정보를 조합하여 고유한 캐시 키를 생성합니다.
     */
    private static String generateStyleKey(Map<String, Object> styleMap, Map<String, Object> styleContainer) {
        String format = styleContainer.getOrDefault("format", "").toString();
        String type = styleContainer.getOrDefault("type", "").toString();
        // Map.toString()은 순서를 보장하므로 정렬된 맵을 사용하지 않아도 일관된 키를 생성합니다.
        return styleMap.toString() + "|format=" + format + "|type=" + type;
    }

    /**
     * CSS의 'text-align' 값을 POI의 HorizontalAlignment 값으로 변환합니다.
     */
    private static HorizontalAlignment getHorizontalAlignment(String value) {
        switch (value.toLowerCase()) {
            case "left":
                return HorizontalAlignment.LEFT;
            case "center":
                return HorizontalAlignment.CENTER;
            case "right":
                return HorizontalAlignment.RIGHT;
            default:
                return HorizontalAlignment.GENERAL;
        }
    }

    /**
     * CSS의 'vertical-align' 값을 POI의 VerticalAlignment 값으로 변환합니다.
     */
    private static VerticalAlignment getVerticalAlignment(String value) {
        switch (value.toLowerCase()) {
            case "top":
                return VerticalAlignment.TOP;
            case "middle":
                return VerticalAlignment.CENTER;
            case "bottom":
                return VerticalAlignment.BOTTOM;
            default:
                return VerticalAlignment.CENTER; // 기본값
        }
    }

    /**
     * 테두리 스타일 및 색상을 설정합니다.
     */
    private static void setBorderStyle(XSSFCellStyle cellStyle, String styleName, String styleValue, Sheet sheet) {
        // 색상 설정 처리
        if (styleName.endsWith("-color")) {
            XSSFColor color = getSpreadSheetColor(styleValue, sheet.getWorkbook());
            if (color == null) return;

            if (styleName.equals("border-bottom-color")) {
                cellStyle.setBottomBorderColor(color);
            } else if (styleName.equals("border-top-color")) {
                cellStyle.setTopBorderColor(color);
            } else if (styleName.equals("border-left-color")) {
                cellStyle.setLeftBorderColor(color);
            } else if (styleName.equals("border-right-color")) {
                cellStyle.setRightBorderColor(color);
            }
            return;
        }

        // 스타일 종류 설정 처리
        BorderStyle borderStyle = BorderStyle.THIN; // 기본값
        switch (styleValue.toLowerCase()) {
            case "solid":
                borderStyle = BorderStyle.THIN;
                break;
            case "dashed":
                borderStyle = BorderStyle.DASHED;
                break;
            case "dotted":
                borderStyle = BorderStyle.DOTTED;
                break;
            case "double":
                borderStyle = BorderStyle.DOUBLE;
                break;
            case "none":
                 borderStyle = BorderStyle.NONE;
                 break;
        }

        if (styleName.equals("border-bottom-style") || styleName.equals("border-bottom")) {
            cellStyle.setBorderBottom(borderStyle);
        } else if (styleName.equals("border-top-style") || styleName.equals("border-top")) {
            cellStyle.setBorderTop(borderStyle);
        } else if (styleName.equals("border-left-style") || styleName.equals("border-left")) {
            cellStyle.setBorderLeft(borderStyle);
        } else if (styleName.equals("border-right-style") || styleName.equals("border-right")) {
            cellStyle.setBorderRight(borderStyle);
        }
    }

    /**
     * 셀 서식을 적용합니다 (숫자, 문자열 등).
     */
    private static void applyDataFormat(Workbook workbook, CellStyle cellStyle, Map<String, Object> styleContainer) {
        Object formatObj = styleContainer.get("format");
        Object typeObj = styleContainer.get("type");

        if (formatObj instanceof String && !((String) formatObj).trim().isEmpty() &&
            typeObj instanceof String) {

            String dataFormatString = ((String) formatObj).trim();
            String dataType = ((String) typeObj).toLowerCase();

         
            if ("number".equals(dataType)) {
                dataFormatString = dataFormatString.replaceAll("[sS]", "").replaceAll("9", "#");
            } else if ("string".equals(dataType)) {
                dataFormatString = "@";
            }
            
            // 날짜(date) 타입은 일반적으로 'yyyy-MM-dd'와 같은 형식이므로 별도 변환 없이 사용
            try {
                DataFormat dataFormat = workbook.createDataFormat();
                short formatIndex = dataFormat.getFormat(dataFormatString);
                cellStyle.setDataFormat(formatIndex);
            } catch (Exception e) {
                System.err.println("Invalid Excel data format: " + dataFormatString);
            }
        }
    }

    /**
     * 16진수 색상 코드(예: #FF0000)를 POI의 XSSFColor 객체로 변환합니다.
     */
    public static XSSFColor getSpreadSheetColor(String hexColor, Workbook workbook) {
        if (hexColor == null || !hexColor.matches("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$")) {
            return null; // 유효하지 않은 HEX 코드 형식일 경우 null 반환
        }
        
        try {
            Color color = Color.decode(hexColor);
            byte[] rgb = new byte[]{(byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue()};
            return new XSSFColor(rgb, new DefaultIndexedColorMap());
        } catch(NumberFormatException e) {
            return null;
        }
    }
}