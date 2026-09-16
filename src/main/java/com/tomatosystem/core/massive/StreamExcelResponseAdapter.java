package com.tomatosystem.core.massive;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleopatra.export.ExcelExporter;
import com.cleopatra.export.ExporterFactory;
import com.cleopatra.export.ExporterFactory.EXPORTTYPE;
import com.cleopatra.export.source.JSONDataSource;
import com.cleopatra.json.JSONArray;
import com.cleopatra.json.JSONObject;
import com.tomatosystem.core.util.HttpWebUtil;

public class StreamExcelResponseAdapter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private HttpServletResponse response;
	private Workbook workbook;
	private int rowNum = 1;
	private OutputStream out;
	private String[] header = new String[] {};
	private String FileNm = "";

	private Sheet sheet;
	private File tempFile;
	private FileOutputStream fileOutputStream;

	private CellStyle numberCellStyle;
	private CellStyle numberCellStyle2;
	private CellStyle defaultCalibriStyle;
	public boolean isServerExport = false;

	private Map<String, List<CellStyle[]>> regionStyles = new HashMap<>();  // [정수스타일, 소수스타일] 쌍
	private Map<String, CellStyle> globalStyleCache = new HashMap<>(); // 전역 스타일 캐시 추가
	
	private static final int MAX_ROWS_PER_SHEET = 1000000;
	private static final int SXSSF_WINDOW_SIZE = 500; // 성능 개선: 100 → 500
	private static final int FLUSH_INTERVAL = 5000; // flush 주기
	private static final int BUFFER_SIZE = 8192; // I/O 버퍼 크기
	
	private int sheetIndex = 0;
	private int headerNum;
	private static final String TEMPLATE_SHEET_NAME = "header_template_for_copy";
	
	public StreamExcelResponseAdapter(HttpServletRequest request, HttpServletResponse response, String strFileName)
			throws IOException {
		this.response = response;
		String strFileNm = strFileName;
		strFileNm = HttpWebUtil.getUrlEncodedFileName(request, strFileNm);

		String resCharset = request.getHeader("res-charset");
		if ((resCharset == null) || (resCharset.equalsIgnoreCase(""))) {
			resCharset = "UTF-8";
		}
		out = response.getOutputStream();

		response.setContentType("application/x-msdownload" + ";charset=" + resCharset);
		response.setHeader("Content-Disposition", "attachment;filename=\"" + strFileNm + ".xlsx" + "\"");

		// workbook - 윈도우 크기 최적화
		workbook = new SXSSFWorkbook(SXSSF_WINDOW_SIZE);
		workbook.createSheet("Sheet1");

		// 스타일 미리 생성
		this.numberCellStyle = createNumberStyle(workbook, "#,###");
		this.numberCellStyle2 = createNumberStyle(workbook, "#,###.###");
	}

	public StreamExcelResponseAdapter(String key, JSONObject requestData)
			throws IOException, NoSuchMethodException, SecurityException {
		this.FileNm = requestData.getString("fileName");
		this.tempFile = File.createTempFile(key, ".xlsx");

		this.fileOutputStream = new FileOutputStream(this.tempFile);
		ExporterFactory factory = ExporterFactory.getInstance();
		ExcelExporter exporter = (ExcelExporter) factory.getExporter(EXPORTTYPE.SXLSX);

		JSONObject jsonObject = requestData.getJSONObject("gridInfo");
		JSONDataSource dataSource = new JSONDataSource(key, jsonObject);
		this.workbook = exporter.export(dataSource);
		this.sheet = this.workbook.getSheetAt(0);
		this.workbook.setSheetName(0, "sheet1");
		this.rowNum = this.sheet.getLastRowNum() + 1;
		this.headerNum = this.sheet.getLastRowNum();
		createHeaderTemplateSheet();

		if (requestData.has("columnInfo")) {
			JSONArray columns = (JSONArray) requestData.get("columnInfo");
			String[] headerKeys = new String[columns.length()];
			for (int i = 0; i < columns.length(); i++) {
				headerKeys[i] = (String) columns.get(i);
			}
			this.header = headerKeys;
		}

		// 스타일 미리 생성
		this.numberCellStyle = createNumberStyle(workbook, "#,###");
		this.numberCellStyle2 = createNumberStyle(workbook, "#,###.###");
		this.defaultCalibriStyle = createDefaultCalibriStyle(this.workbook);
	}

	public void addHeader(LinkedHashMap<String, Object> mapHeader) {
		CellStyle cellStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 11);
		headerFont.setColor(IndexedColors.BLACK.getIndex());

		cellStyle.setFont(headerFont);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		cellStyle.setWrapText(true);

		cellStyle.setBorderRight(BorderStyle.THIN);
		cellStyle.setBorderLeft(BorderStyle.THIN);
		cellStyle.setBorderTop(BorderStyle.THIN);
		cellStyle.setBorderBottom(BorderStyle.THIN);
		cellStyle.setRightBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		cellStyle.setLeftBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		cellStyle.setTopBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());
		cellStyle.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

		cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

		Sheet sheet = this.workbook.getSheetAt(0);
		Row headerRow = sheet.createRow(0);
		
		Object key;
		int idx = 0;
		Cell headerCell = null;
		Iterator<String> iterHeader = mapHeader.keySet().iterator();
		header = new String[mapHeader.keySet().size()];
		
		while (iterHeader.hasNext()) {
			key = iterHeader.next();
			headerCell = headerRow.createCell(idx);
			headerCell.setCellStyle(cellStyle);
			sheet.setColumnWidth(idx, 4200);
			headerCell.setCellValue((double) mapHeader.get(key));
			header[idx] = (String) key;
			idx++;
		}
	}

	public void addRowMassive(JSONObject data, String region, String excelKey) throws IOException {
		JSONArray dataVal = data.getJSONArray("data");

		// 스타일 초기화 (최초 1회만) - [정수스타일, 소수스타일] 쌍으로 등록
		if (!this.regionStyles.containsKey(region)) {
			List<CellStyle[]> columnStyles = new ArrayList<>();
			JSONArray styles = data.getJSONArray("style");

			for (int j = 0; j < styles.length(); j++) {
				JSONObject styleContainer = styles.getJSONObject(j);
				Map<String, Object> styleContainerMap = styleContainer.toMap();

				String rawFormat = styleContainerMap.containsKey("format")
					? String.valueOf(styleContainerMap.get("format")) : "";

				// 소수 포맷 스타일 (기본)
				Map<String, Object> decMap = new java.util.LinkedHashMap<>(styleContainerMap);
				if (!rawFormat.isEmpty()) decMap.put("format", normalizeNumFmt(rawFormat));
				CellStyle decStyle = getCachedStyle(decMap);

				// 정수 포맷 스타일 (소수점 이하 제거)
				CellStyle intStyle;
				if (!rawFormat.isEmpty() && rawFormat.indexOf('.') >= 0) {
					Map<String, Object> intMap = new java.util.LinkedHashMap<>(styleContainerMap);
					intMap.put("format", normalizeNumFmtInt(rawFormat));
					intStyle = getCachedStyle(intMap);
				} else {
					intStyle = decStyle;
				}

				columnStyles.add(new CellStyle[]{intStyle, decStyle});
			}
			this.regionStyles.put(region, columnStyles);
		}

		List<CellStyle[]> currentColumnStyles = this.regionStyles.get(region);
		int dataLength = dataVal.length();
		int columnCount = currentColumnStyles.size();

		try {
			for (int i = 0; i < dataLength; i++) {
				// 시트 전환 체크
				if (this.rowNum > (MAX_ROWS_PER_SHEET + this.headerNum)) {
					this.sheetIndex++;
					String newSheetName = "Sheet" + (this.sheetIndex + 1);
					this.sheet = this.workbook.createSheet(newSheetName);
					this.rowNum = 0;

					copyHeadersFromTemplate(this.sheet);
					this.rowNum = this.sheet.getLastRowNum() + 1;
					this.regionStyles.clear();
				}

				Row row = this.sheet.createRow(this.rowNum++);
				JSONArray datavalue = dataVal.getJSONArray(i);
				int cellCount = Math.min(datavalue.length(), columnCount);

				// 셀 생성 및 값 설정
				for (int j = 0; j < cellCount; j++) {
					Object cellValue = datavalue.get(j);
					Cell cell = row.createCell(j);

					// 타입별 처리 (빈번한 타입부터)
					if (cellValue == null) {
						cell.setCellValue("");
					} else if (cellValue instanceof Integer) {
						cell.setCellValue(((Integer) cellValue).intValue());
					} else if (cellValue instanceof Long) {
						cell.setCellValue(((Long) cellValue).longValue());
					} else if (cellValue instanceof Double) {
						cell.setCellValue(((Double) cellValue).doubleValue());
					} else if (cellValue instanceof BigDecimal) {
						BigDecimal numValue = (BigDecimal) cellValue;
						if (numValue.stripTrailingZeros().scale() <= 0) {
							cell.setCellValue(numValue.longValue());
						} else {
							cell.setCellValue(numValue.doubleValue());
						}
					} else if (cellValue instanceof Boolean) {
						cell.setCellValue((Boolean) cellValue);
					} else {
						cell.setCellValue(cellValue.toString());
					}

					// 정수/소수 여부에 따라 스타일 선택
					CellStyle[] pair = currentColumnStyles.get(j);
					cell.setCellStyle(isDecimal(cellValue) ? pair[1] : pair[0]);
				}

				// 주기적으로 flush (메모리 관리)
				if (this.rowNum % FLUSH_INTERVAL == 0) {
					try {
						((SXSSFSheet) this.sheet).flushRows();
					} catch (IOException e) {
						logger.warn("Sheet flush 실패: {}", e.getMessage());
					}
				}
				ProgressExcelStore.progressMap.put(excelKey, this.rowNum);
				ProgressExcelStore.progressMap.put(excelKey+"_sheet", this.sheetIndex);
			}
		} catch (Exception e) {
			logger.error("행 추가 중 오류 발생", e);
			cleanupResources();
			throw new IOException("행 추가 실패", e);
		}
	}
	
	public void addRowMassive(JSONObject data, String region) throws IOException {
		JSONArray dataVal = data.getJSONArray("data");

		// 스타일 초기화 (최초 1회만) - [정수스타일, 소수스타일] 쌍으로 등록
		if (!this.regionStyles.containsKey(region)) {
			List<CellStyle[]> columnStyles = new ArrayList<>();
			JSONArray styles = data.getJSONArray("style");

			for (int j = 0; j < styles.length(); j++) {
				JSONObject styleContainer = styles.getJSONObject(j);
				Map<String, Object> styleContainerMap = styleContainer.toMap();

				String rawFormat = styleContainerMap.containsKey("format")
					? String.valueOf(styleContainerMap.get("format")) : "";

				// 소수 포맷 스타일 (기본)
				Map<String, Object> decMap = new java.util.LinkedHashMap<>(styleContainerMap);
				if (!rawFormat.isEmpty()) decMap.put("format", normalizeNumFmt(rawFormat));
				CellStyle decStyle = getCachedStyle(decMap);

				// 정수 포맷 스타일 (소수점 이하 제거)
				CellStyle intStyle;
				if (!rawFormat.isEmpty() && rawFormat.indexOf('.') >= 0) {
					Map<String, Object> intMap = new java.util.LinkedHashMap<>(styleContainerMap);
					intMap.put("format", normalizeNumFmtInt(rawFormat));
					intStyle = getCachedStyle(intMap);
				} else {
					intStyle = decStyle;
				}

				columnStyles.add(new CellStyle[]{intStyle, decStyle});
			}
			this.regionStyles.put(region, columnStyles);
		}

		List<CellStyle[]> currentColumnStyles = this.regionStyles.get(region);
		int dataLength = dataVal.length();
		int columnCount = currentColumnStyles.size();

		try {
			for (int i = 0; i < dataLength; i++) {
				// 시트 전환 체크
				if (this.rowNum > (MAX_ROWS_PER_SHEET + this.headerNum)) {
					this.sheetIndex++;
					String newSheetName = "Sheet" + (this.sheetIndex + 1);
					this.sheet = this.workbook.createSheet(newSheetName);
					this.rowNum = 0;

					copyHeadersFromTemplate(this.sheet);
					this.rowNum = this.sheet.getLastRowNum() + 1;
					this.regionStyles.clear();
				}

				Row row = this.sheet.createRow(this.rowNum++);
				JSONArray datavalue = dataVal.getJSONArray(i);
				int cellCount = Math.min(datavalue.length(), columnCount);

				// 셀 생성 및 값 설정
				for (int j = 0; j < cellCount; j++) {
					Object cellValue = datavalue.get(j);
					Cell cell = row.createCell(j);

					// 타입별 처리 (빈번한 타입부터)
					if (cellValue == null) {
						cell.setCellValue("");
					} else if (cellValue instanceof Integer) {
						cell.setCellValue(((Integer) cellValue).intValue());
					} else if (cellValue instanceof Long) {
						cell.setCellValue(((Long) cellValue).longValue());
					} else if (cellValue instanceof Double) {
						cell.setCellValue(((Double) cellValue).doubleValue());
					} else if (cellValue instanceof BigDecimal) {
						BigDecimal numValue = (BigDecimal) cellValue;
						if (numValue.stripTrailingZeros().scale() <= 0) {
							cell.setCellValue(numValue.longValue());
						} else {
							cell.setCellValue(numValue.doubleValue());
						}
					} else if (cellValue instanceof Boolean) {
						cell.setCellValue((Boolean) cellValue);
					} else {
						cell.setCellValue(cellValue.toString());
					}

					// 정수/소수 여부에 따라 스타일 선택
					CellStyle[] pair = currentColumnStyles.get(j);
					cell.setCellStyle(isDecimal(cellValue) ? pair[1] : pair[0]);
				}

				// 주기적으로 flush (메모리 관리)
				if (this.rowNum % FLUSH_INTERVAL == 0) {
					try {
						((SXSSFSheet) this.sheet).flushRows();
					} catch (IOException e) {
						logger.warn("Sheet flush 실패: {}", e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			logger.error("행 추가 중 오류 발생", e);
			cleanupResources();
			throw new IOException("행 추가 실패", e);
		}
	}
	
//	public void endResponseMassive(HttpServletResponse response, String progressId) {
//		try {
//			
//			this.workbook.write(this.fileOutputStream);
//			this.fileOutputStream.flush();
//			this.fileOutputStream.close();
//			this.workbook.close();
//			
//			String encodedFileName = URLEncoder.encode(this.FileNm + ".xlsx", StandardCharsets.UTF_8.toString());
//
//			response.setContentType("application/octet-stream");
//			response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
//			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
//			response.setHeader("Pragma", "no-cache");
//			response.setHeader("Expires", "0");
//
//			OutputStream out = response.getOutputStream();
////			ProgressExcelStore.progressMap.remove(progressId);
//			try (FileInputStream fis = new FileInputStream(this.tempFile)) {
//				byte[] buffer = new byte[BUFFER_SIZE]; // 버퍼 크기 증가
//				int bytesRead;
//				while ((bytesRead = fis.read(buffer)) != -1) {
//					out.write(buffer, 0, bytesRead);
//				}
//			}
//			out.flush();
//
//		} catch (IOException e) {
//			logger.error("Excel 파일 응답 중 오류 발생", e);
//		} finally {
//			if (this.tempFile != null && this.tempFile.exists()) {
//				this.tempFile.delete();
//			}
////			ProgressExcelStore.progressMap.remove(progressId);
//		}
//	}
	
	
	public void endResponseMassive(HttpServletResponse response) {
		
		 FileInputStream fis = null;
		 OutputStream out = null;
		 
		try {
			this.workbook.write(this.fileOutputStream);
			this.fileOutputStream.flush();
			this.fileOutputStream.close();
			this.workbook.close();
			
			String encodedFileName = URLEncoder.encode(this.FileNm + ".xlsx", StandardCharsets.UTF_8.toString());

			response.setContentType("application/octet-stream");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
			response.setHeader("Pragma", "no-cache");
			response.setHeader("Expires", "0");

			out = response.getOutputStream();
			fis = new FileInputStream(this.tempFile);
			 
			byte[] buffer = new byte[BUFFER_SIZE]; // 버퍼 크기 증가
			int bytesRead;
			
			while ((bytesRead = fis.read(buffer)) != -1) {
				out.write(buffer, 0, bytesRead);
			}
			out.flush();

		} catch (IOException e) {
			logger.error("Excel 파일 응답 중 오류 발생", e);
		} finally {
			// 5. 리소스 정리
	        try {
	            if (fis != null) fis.close();
	        } catch (IOException e) {
	            logger.warn("FileInputStream 닫기 실패", e);
	        }
	        
			if (this.tempFile != null && this.tempFile.exists()) {
				this.tempFile.delete();
			}
		}
	}
	
	private void createHeaderTemplateSheet() {
		Sheet firstSheet = this.workbook.getSheetAt(0);
		Sheet templateSheet = this.workbook.createSheet(TEMPLATE_SHEET_NAME);

		int lastHeaderRow = firstSheet.getLastRowNum();

		for (int i = 0; i <= lastHeaderRow; i++) {
			Row sourceRow = firstSheet.getRow(i);
			if (sourceRow == null) continue;
			
			Row destRow = templateSheet.createRow(i);
			destRow.setHeight(sourceRow.getHeight());

			short lastCellNum = sourceRow.getLastCellNum();
			for (int j = 0; j < lastCellNum; j++) {
				Cell sourceCell = sourceRow.getCell(j);
				if (sourceCell == null) continue;
				
				Cell destCell = destRow.createCell(j);
				destCell.setCellStyle(sourceCell.getCellStyle());
				
				switch (sourceCell.getCellType()) {
				case NUMERIC:
					destCell.setCellValue(sourceCell.getNumericCellValue());
					break;
				case BOOLEAN:
					destCell.setCellValue(sourceCell.getBooleanCellValue());
					break;
				case FORMULA:
					destCell.setCellFormula(sourceCell.getCellFormula());
					break;
				case STRING:
					destCell.setCellValue(sourceCell.getStringCellValue());
					break;
				default:
					break;
				}
			}
		}

		Row firstRow = firstSheet.getRow(0);
		if (firstRow != null) {
			short lastCellNum = firstRow.getLastCellNum();
			for (int i = 0; i < lastCellNum; i++) {
				templateSheet.setColumnWidth(i, firstSheet.getColumnWidth(i));
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
				
				switch (sourceCell.getCellType()) {
				case NUMERIC:
					destCell.setCellValue(sourceCell.getNumericCellValue());
					break;
				case BOOLEAN:
					destCell.setCellValue(sourceCell.getBooleanCellValue());
					break;
				case FORMULA:
					destCell.setCellFormula(sourceCell.getCellFormula());
					break;
				case STRING:
					destCell.setCellValue(sourceCell.getStringCellValue());
					break;
				default:
					break;
				}
			}
		}

		Row firstRow = templateSheet.getRow(0);
		if (firstRow != null) {
			short lastCellNum = firstRow.getLastCellNum();
			for (int i = 0; i < lastCellNum; i++) {
				newSheet.setColumnWidth(i, templateSheet.getColumnWidth(i));
			}
		}
	}

	public void addRow(Object data) {
		if (!(data instanceof Map)) return;

		if (this.rowNum > (MAX_ROWS_PER_SHEET + this.headerNum)) {
			this.sheetIndex++;
			String newSheetName = "Sheet" + (this.sheetIndex + 1);
			this.sheet = this.workbook.createSheet(newSheetName);
			this.rowNum = 0;

			copyHeadersFromTemplate(this.sheet);
			this.rowNum = this.sheet.getLastRowNum() + 1;
			this.regionStyles.clear();
		}

		Map mapData = (Map) data;
		Row row = this.sheet.createRow(this.rowNum);

		int cellIdx = 0;
		Object value = null;
		double dvalue;
		
		for (int i = 0, len = this.header.length; i < len; i++) {
			value = mapData.get(this.header[i]);
			Cell cell = row.createCell(cellIdx);

			if (value instanceof Integer) {
				cell.setCellValue(((Integer) value).intValue());
				cell.setCellStyle(this.numberCellStyle);
			} else if (value instanceof BigDecimal) {
				dvalue = ((BigDecimal) value).doubleValue();
				cell.setCellValue(dvalue);
				if ((dvalue - (int) dvalue) > 0) {
					cell.setCellStyle(this.numberCellStyle2);
				} else {
					cell.setCellStyle(this.numberCellStyle);
				}
			} else {
				cell.setCellValue((String) value);
				cell.setCellStyle(this.defaultCalibriStyle);
			}

			cellIdx++;
		}

		this.rowNum++;
	}
	
	
	public void addRow(Object data, String progressId) {
		if (!(data instanceof Map)) return;

		if (this.rowNum > (MAX_ROWS_PER_SHEET + this.headerNum)) {
			this.sheetIndex++;
			String newSheetName = "Sheet" + (this.sheetIndex + 1);
			this.sheet = this.workbook.createSheet(newSheetName);
			this.rowNum = 0;

			copyHeadersFromTemplate(this.sheet);
			this.rowNum = this.sheet.getLastRowNum() + 1;
			this.regionStyles.clear();
		}

		Map mapData = (Map) data;
		Row row = this.sheet.createRow(this.rowNum);

		int cellIdx = 0;
		Object value = null;
		double dvalue;
		
		for (int i = 0, len = this.header.length; i < len; i++) {
			value = mapData.get(this.header[i]);
			Cell cell = row.createCell(cellIdx);

			if (value instanceof Integer) {
				cell.setCellValue(((Integer) value).intValue());
				cell.setCellStyle(this.numberCellStyle);
			} else if (value instanceof BigDecimal) {
				dvalue = ((BigDecimal) value).doubleValue();
				cell.setCellValue(dvalue);
				if ((dvalue - (int) dvalue) > 0) {
					cell.setCellStyle(this.numberCellStyle2);
				} else {
					cell.setCellStyle(this.numberCellStyle);
				}
			} else {
				cell.setCellValue((String) value);
				cell.setCellStyle(this.defaultCalibriStyle);
			}

			cellIdx++;
		}
		ProgressExcelStore.progressMap.put(progressId, this.rowNum);
		ProgressExcelStore.progressMap.put(progressId+"_sheet", this.sheetIndex);
		this.rowNum++;
		

	}
	
	public void endReponse(String progressId) {
		try {
			this.workbook.write(out);
			this.response.flushBuffer();
		} catch (IOException e) {
			logger.error("응답 종료 중 오류 발생", e);
		} finally {
			cleanupResources();
//			ProgressExcelStore.progressMap.put(progressId, -1);
		}
	}
	
	public void endReponse() {
		try {
			this.workbook.write(out);
			this.response.flushBuffer();
		} catch (IOException e) {
			logger.error("응답 종료 중 오류 발생", e);
		} finally {
			cleanupResources();
		}
	}
	// ========== 유틸리티 메서드 ==========
	
	/**
	 * 스타일 캐시에서 가져오거나 새로 생성
	 */
	private CellStyle getCachedStyle(Map<String, Object> styleMap) {
		String styleKey = generateStyleKey(styleMap);
		
		if (!globalStyleCache.containsKey(styleKey)) {
			CellStyle style = ExcelStyleUtil.getOrCreateCellStyle(this.sheet, styleMap);
			globalStyleCache.put(styleKey, style);
		}
		
		return globalStyleCache.get(styleKey);
	}
	
	/**
	 * 스타일 맵으로부터 고유 키 생성
	 */
	private String generateStyleKey(Map<String, Object> styleMap) {
		StringBuilder key = new StringBuilder();
		for (Map.Entry<String, Object> entry : styleMap.entrySet()) {
			key.append(entry.getKey()).append(":").append(entry.getValue()).append("|");
		}
		return key.toString();
	}
	
	/**
	 * 클라이언트 포맷 문자열을 엑셀 포맷으로 정규화
	 * s#,##0.99 → #,##0.##  (소수 포맷)
	 * s#,##0    → #,##0     (정수 포맷)
	 */
	private String normalizeNumFmt(String raw) {
		if (raw == null || raw.isEmpty()) return raw;
		String fmt = raw.replaceAll("^[sS]", "");
		int dotIdx = fmt.indexOf('.');
		if (dotIdx >= 0) {
			return fmt.substring(0, dotIdx + 1)
				 + fmt.substring(dotIdx + 1).replace('9', '#');
		}
		return fmt;
	}

	/**
	 * 정수 포맷 변환: s#,##0.99 → #,##0 (소수점 이하 제거)
	 */
	private String normalizeNumFmtInt(String raw) {
		if (raw == null || raw.isEmpty()) return raw;
		String fmt = raw.replaceAll("^[sS]", "");
		int dotIdx = fmt.indexOf('.');
		return dotIdx >= 0 ? fmt.substring(0, dotIdx) : fmt;
	}

	/**
	 * 값이 소수인지 판별
	 */
	private boolean isDecimal(Object value) {
		if (value instanceof Double)     return ((Double) value) % 1 != 0;
		if (value instanceof BigDecimal) return ((BigDecimal) value).stripTrailingZeros().scale() > 0;
		return false;
	}

	/**
	 * 숫자 포맷 스타일 생성
	 */
	private CellStyle createNumberStyle(Workbook workbook, String format) {
		CellStyle style = workbook.createCellStyle();
		DataFormat dataFormat = workbook.createDataFormat();
		style.setDataFormat(dataFormat.getFormat(format));
		return style;
	}
	
	/**
	 * 기본 Calibri 스타일 생성
	 */
	private CellStyle createDefaultCalibriStyle(Workbook workbook) {
		XSSFFont font = (XSSFFont) workbook.createFont();
		font.setFontName("Calibri");
		XSSFCellStyle style = (XSSFCellStyle) workbook.createCellStyle();
		style.setFont(font);
		return style;
	}
	
	/**
	 * 리소스 정리
	 */
	private void cleanupResources() {
		if (this.fileOutputStream != null) {
			try {
				this.fileOutputStream.close();
			} catch (IOException e) {
				logger.warn("FileOutputStream 닫기 실패", e);
			}
		}
		
		if (this.workbook != null) {
			try {
				this.workbook.close();
				if (this.workbook instanceof SXSSFWorkbook) {
					((SXSSFWorkbook) this.workbook).dispose();
				}
			} catch (IOException e) {
				logger.warn("Workbook 닫기 실패", e);
			}
			this.workbook = null;
		}
		
		if (this.tempFile != null && this.tempFile.exists()) {
			this.tempFile.delete();
		}
	}
}