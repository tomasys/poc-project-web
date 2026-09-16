package com.tomatosystem.app.sample.web;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.DataResponse;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.protocol.data.UploadFile;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.util.ExcelImporter;
import com.tomatosystem.core.util.FileUtil;
import com.tomatosystem.core.util.StringUtil;
import com.tomatosystem.core.vo.ExcelVO;

@Controller
@RequestMapping("/CmnExcelImport")
public class CmnExcelImportContoller {
	/**
	 * 엑셀을 읽어들여 그리드에 세팅할 값으로 변경하여 json 데이타를 보내준다.
	 * @param request
	 * @param response
	 * @throws Exception
	 */
//	@RequestMapping("import.do")
//	public View excelReadJsonSet(HttpServletRequest request, HttpServletResponse response,DataRequest requestData) throws Exception {
//		ParameterGroup dmParam = requestData.getParameterGroup("dmParam");
//		String strStartRowIndex = StringUtil.fixNull(dmParam.getValue("startRowIndex"));
//		String strStartCellIndex = StringUtil.fixNull(dmParam.getValue("startCellIndex"));
//		if("".equals(strStartRowIndex)){
//			strStartRowIndex = "1";
//		}
//		if("".equals(strStartCellIndex)){
//			strStartCellIndex = "0";
//		}
//		
//		ExcelImporter excelImporter = new ExcelImporter();
//		
//		List<ExcelVO> dataList = excelImporter.getCellDataList(request, response, requestData, Integer.parseInt(strStartRowIndex), Integer.parseInt(strStartCellIndex));
//		//excelImporter.getCellDataList2(request, response, requestData, Integer.parseInt(strStartRowIndex), Integer.parseInt(strStartCellIndex));
//		requestData.setResponse("dsExcel", dataList);
//		
//		return new JSONDataView();
//	}
	
	private static int extractKeyNumber(String key) {
        Matcher matcher = Pattern.compile("\\d+").matcher(key);
        return matcher.find() ? Integer.parseInt(matcher.group()) : 0;
    }
	
	@RequestMapping("/importMulti.do")
	public View importMulti(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		if(headerLine == null || "".equals(headerLine)) {
			headerLine = "1";
		}
		int numHeaderLine = Integer.parseInt(headerLine);
		
		// 엑셀 업로드 시, 업로드 하기 위한 컬럼명을 직접 설정 (default: 엑셀 내 각 셀의 value)
		String strColumnInfo = dataRequest.getParameter("columnInfo");
		JSONObject columnJSON = null;
		if(!"".equals(strColumnInfo) && strColumnInfo != null) {
			JSONParser parser = new JSONParser();
			columnJSON = (JSONObject) parser.parse(strColumnInfo);
		}
		
		Map<String, File[]> uploadFiles = dataRequest.getFiles();
		Set<Entry<String, File[]>> entries = uploadFiles.entrySet();
		for(Entry<String, File[]> entry : entries) {
			String name = entry.getKey();
			File[] files = entry.getValue();
			if(files != null && files.length > 0) {
				// 순차적으로 각 시트별 데이터 읽기 위한 정렬
				List<String> sortedKeys = new ArrayList<>(columnJSON.keySet());
				sortedKeys.sort(Comparator.comparingInt(CmnExcelImportContoller::extractKeyNumber));
				
				for (String key : sortedKeys) {
					int sheetNum = sortedKeys.indexOf(key);
					List<Map<String, String>> data = this.parseMultiData(files[0], (JSONObject) columnJSON.get(key), numHeaderLine, sheetNum);
					if(data != null) {
						dataRequest.setResponse(key, data);
					}
		        }
			}
		}
		return new JSONDataView();
	}
	
	@RequestMapping("/import.do")
	public View importExcel(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		if(headerLine == null || "".equals(headerLine)) {
			headerLine = "1";
		}
		int numHeaderLine = Integer.parseInt(headerLine);
		
		// 엑셀 업로드 시, 업로드 하기 위한 컬럼명을 직접 설정 (default: 엑셀 내 각 셀의 value)
		String strColumnInfo = dataRequest.getParameter("columnInfo");
		JSONObject columnJSON = null;
		if(!"".equals(strColumnInfo) && strColumnInfo != null) {
			JSONParser parser = new JSONParser();
			columnJSON = (JSONObject) parser.parse(strColumnInfo);
		}
		
		// 업로드 할 데이터셋 ID
		String strDataset = dataRequest.getParameter("datasetId");
		if(strDataset == null || "".equals(strDataset)) {
			strDataset = "dsExcel";
		}
		
		Map<String, File[]> uploadFiles = dataRequest.getFiles();
		Set<Entry<String, File[]>> entries = uploadFiles.entrySet();
		for(Entry<String, File[]> entry : entries) {
			String name = entry.getKey();
			File[] files = entry.getValue();
			if(files != null && files.length > 0) {
				List<Map<String, String>> data = this.parseData(files[0], columnJSON, numHeaderLine);
				if(data != null) {
					dataRequest.setResponse(strDataset, data);
				}
			}
		}
		
		return new JSONDataView();
	}
	
	@SuppressWarnings("deprecation")
	private List<Map<String, String>> parseMultiData(File file, JSONObject colInfo, int headerLine, int sheetNum) throws Exception {
		if(file == null || file.exists() == false) {
			return null;
		}
		
		List<Map<String,String>> data = new ArrayList<Map<String, String>>();
		
		List<String> keys = new ArrayList<>(colInfo.keySet());
		keys.sort(Comparator.comparingInt(CmnExcelImportContoller::extractKeyNumber));
		
		FileInputStream in = null;
		Workbook workbook = null;
		Map<Integer, String> header = new HashMap<Integer, String>();
		try {
			in = new FileInputStream(file);
			workbook = new XSSFWorkbook(in); //.XLSX
			
			Sheet datatypeSheet = workbook.getSheetAt(sheetNum);
            Iterator<Row> iterator = datatypeSheet.iterator();
            
            // single row header
            for (int i = 0; i < headerLine; i++) {
            	if(iterator.hasNext()) {
            		Row row = iterator.next();
            		Iterator<Cell> cellIterator = row.iterator();
            		while(cellIterator.hasNext()) {
            			Cell cell = cellIterator.next();
            			int colIdx = cell.getColumnIndex();
            			String value = cell.getStringCellValue();
            			
            			// 셀이 병합되서 value가 없는 경우는 continue 처리
						Boolean isMergeCell = isMerged(datatypeSheet, cell.getRowIndex(), colIdx);
						if (value == null || "".equals(value.trim())) {
							if(isMergeCell == false) {
								break;
							}
						}
						
						if(colInfo != null) {
							if (colInfo.containsValue(value)) {
								for (String key : keys) {
									if (value.equals(colInfo.get(key))) {
										header.put(colIdx, key);
										keys.remove(keys.indexOf(key)); // 동일한 헤더명이 있을 경우, 이미 체크한 헤더는 제외하기위해 remove 처리
										break;
									}
								}
								
							} else {
								if (colInfo.size() > 0) {
									if(colInfo.get("index-" + colIdx) != null) {
										header.put(colIdx, colInfo.get("index-" + colIdx).toString());
									}
								} else {
									header.put(colIdx, "column" + (colIdx + 1));
								}
							}
						} else {
							header.put(colIdx, value);
						}
            		}
            	}
            }
            
            // single row data
            while(iterator.hasNext()) {
            	Map<String, String> rowData = new HashMap<String, String>();
            	Collection<String> headers = header.values();
            	for(String h : headers) {
            		rowData.put(h, null);
            	}
            	
            	Row row = iterator.next();
            	Iterator<Cell> cellIterator = row.iterator();
            	while(cellIterator.hasNext()) {
            		Cell cell = cellIterator.next();
            		int colIdx = cell.getColumnIndex();
            		String name = header.get(new Integer(colIdx));
            		String value = null;
            		if(cell.getCellTypeEnum() == CellType.STRING) {
                		value = cell.getStringCellValue();
            		} else if(cell.getCellTypeEnum() == CellType.NUMERIC) {
            			
            			// Date format 은 NUMERIC 에서 체크하도록 수정 (2023.10.31)
						if (DateUtil.isCellDateFormatted(cell)) {
							Date tempDate = cell.getDateCellValue();
							value = new SimpleDateFormat("YYYY-MM-dd").format(tempDate);
						} else {
							DataFormatter formatter = new DataFormatter();
							FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
							value = formatter.formatCellValue(cell, evaluator);
						}
						
//            			value = "" + cell.getNumericCellValue();
            		} else if (cell.getCellType() == CellType.FORMULA) {
						switch (cell.getCachedFormulaResultType()) {
						case NUMERIC:
							value = "" + Math.round(cell.getNumericCellValue());
							break;
						case STRING:
							value = cell.getStringCellValue();
							break;
						default:
							break;
						}
					} 
            		rowData.put(name, value);
            	}
            	
            	data.add(rowData);
            }
        } finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(workbook != null) {
				try {
					workbook.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return data;
	}
	
	
	@SuppressWarnings("deprecation")
	private List<Map<String, String>> parseData(File file, JSONObject colInfo, int headerLine) throws Exception {
		if(file == null || file.exists() == false) {
			return null;
		}
		
		List<Map<String,String>> data = new ArrayList<Map<String, String>>();
		List<String> keys = new ArrayList<>(colInfo.keySet());
		keys.sort(Comparator.comparingInt(CmnExcelImportContoller::extractKeyNumber));
		
		FileInputStream in = null;
		Workbook workbook = null;
		Map<Integer, String> header = new HashMap<Integer, String>();
		try {
			in = new FileInputStream(file);
			workbook = new XSSFWorkbook(in); //.XLSX
//			workbook = new HSSFWorkbook(in); //.XLS
			
			Sheet datatypeSheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = datatypeSheet.iterator();
            
            // single row header
            for (int i = 0; i < headerLine; i++) {
            	if(iterator.hasNext()) {
            		Row row = iterator.next();
            		Iterator<Cell> cellIterator = row.iterator();
            		while(cellIterator.hasNext()) {
            			Cell cell = cellIterator.next();
            			int colIdx = cell.getColumnIndex();
            			String value = cell.getStringCellValue();
            			
            			// 셀이 병합되서 value가 없는 경우는 continue 처리
						Boolean isMergeCell = isMerged(datatypeSheet, cell.getRowIndex(), colIdx);
						if (value == null || "".equals(value.trim())) {
							if(isMergeCell == false) {
								break;
							}
						}
						
						if(colInfo != null) {
							if (colInfo.containsValue(value)) {
								String targetKey = keys.get(colIdx);
								if(targetKey != null) {
									header.put(colIdx, targetKey);
								}
//							if (colInfo.containsKey(value) && !"".equals(colInfo.get(value)) && colInfo.get(value) != null) {
//								if(colInfo.get(value) != null) {
//									header.put(colIdx, colInfo.get(value).toString());
//								}
							} else {
								if (colInfo.size() > 0) {
									if(colInfo.get("index-" + colIdx) != null) {
										header.put(colIdx, colInfo.get("index-" + colIdx).toString());
									}
								} else {
									header.put(colIdx, "column" + (colIdx + 1));
								}
							}
						} else {
							header.put(colIdx, value);
						}
            		}
            	}
            }
            
            // single row data
            while(iterator.hasNext()) {
            	Map<String, String> rowData = new HashMap<String, String>();
            	Collection<String> headers = header.values();
            	for(String h : headers) {
            		rowData.put(h, null);
            	}
            	
            	Row row = iterator.next();
            	Iterator<Cell> cellIterator = row.iterator();
            	while(cellIterator.hasNext()) {
            		Cell cell = cellIterator.next();
            		int colIdx = cell.getColumnIndex();
            		String name = header.get(new Integer(colIdx));
            		String value = null;
            		if(cell.getCellTypeEnum() == CellType.STRING) {
                		value = cell.getStringCellValue();
            		} else if(cell.getCellTypeEnum() == CellType.NUMERIC) {
            			
            			// Date format 은 NUMERIC 에서 체크하도록 수정 (2023.10.31)
						if (DateUtil.isCellDateFormatted(cell)) {
							Date tempDate = cell.getDateCellValue();
							value = new SimpleDateFormat("YYYY-MM-dd").format(tempDate);
						} else {
							DataFormatter formatter = new DataFormatter();
							FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
							value = formatter.formatCellValue(cell, evaluator);
						}
						
//            			value = "" + cell.getNumericCellValue();
            		} else if (cell.getCellType() == CellType.FORMULA) {
						switch (cell.getCachedFormulaResultType()) {
						case NUMERIC:
							value = "" + Math.round(cell.getNumericCellValue());
							break;
						case STRING:
							value = cell.getStringCellValue();
							break;
						default:
							break;
						}
					} 
            		rowData.put(name, value);
            	}
            	
            	data.add(rowData);
            }
        } finally {
			if(in != null) {
				try {
					in.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			if(workbook != null) {
				try {
					workbook.close();
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return data;
	}
	
	/**
	 * 엑셀을 읽어들여 그리드에 세팅할 값으로 변경하여 json 데이타를 보내준다.
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("csvImport.do")
	public View excelCsvReadJsonSet(HttpServletRequest request, HttpServletResponse response,DataRequest requestData) throws Exception {
		ParameterGroup dmParam = requestData.getParameterGroup("dmParam");
		String strStartRowIndex = StringUtil.fixNull(dmParam.getValue("startRowIndex"));
		String strStartCellIndex = StringUtil.fixNull(dmParam.getValue("startCellIndex"));
		String strUploadUserNm = StringUtil.fixNull(dmParam.getValue("uploadUserNm"));
		
		LocalDateTime currentDateTime = LocalDateTime.now();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	    String strUploadDate = currentDateTime.format(formatter);
	        
		if("".equals(strStartRowIndex)){
			strStartRowIndex = "1";
		}
		if("".equals(strStartCellIndex)){
			strStartCellIndex = "0";
		}
		
		ExcelImporter excelImporter = new ExcelImporter();
		List<String> dataList = excelImporter.getCellDataList2(request, response, requestData, Integer.parseInt(strStartRowIndex), Integer.parseInt(strStartCellIndex), strUploadUserNm);
		
		requestData.setResponse("dsExcel", dataList);
		
		return new JSONDataView();
	}
	
	/**
	 * 엑셀을 읽어들여 그리드에 세팅할 값으로 변경하여 json 데이타를 보내준다.
	 * @param request
	 * @param response
	 * @throws Exception
	 */
	@RequestMapping("tsvImport.do")
	public void excelReadJsonSetTsv(HttpServletRequest request, HttpServletResponse response,DataRequest requestData) throws Exception {
		ParameterGroup dmParam = requestData.getParameterGroup("dmParam");
		String strStartRowIndex = StringUtil.fixNull(dmParam.getValue("startRowIndex"));
		String strStartCellIndex = StringUtil.fixNull(dmParam.getValue("startCellIndex"));
		if("".equals(strStartRowIndex)){
			strStartRowIndex = "1";
		}
		if("".equals(strStartCellIndex)){
			strStartCellIndex = "0";
		}
		
		ExcelImporter excelImporter = new ExcelImporter();
		final DataResponse dataRes = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
		Map<String, UploadFile[]> uploadFiles = requestData.getUploadFiles();
		
		List<ExcelVO> excelData = null;
		if(uploadFiles != null && uploadFiles.size() > 0) {
			Set<Entry<String, UploadFile[]>> entries = uploadFiles.entrySet();
			for(Entry<String, UploadFile[]> entry : entries) {
				UploadFile[] uFiles = entry.getValue();
				if(uFiles.length > 0){
					String strFileExtNm = FileUtil.getFileExtNm(uFiles[0].getFileName());
					excelImporter.tsvSend(uFiles[0].getFile(), strStartRowIndex, strStartCellIndex, null, response);
					break;
				}
			}
		}
	}
	
	
	private boolean isMerged(Sheet sheet, int rowIdx, int colIdx) {

		for (int i = 0; i < sheet.getNumMergedRegions(); ++i) {
			CellRangeAddress range = sheet.getMergedRegion(i);

			String message = String.format("%d - %d - %d - %d", range.getFirstRow(), range.getLastRow(), range.getFirstColumn(), range.getLastColumn());

			if (rowIdx >= range.getFirstRow() && rowIdx <= range.getLastRow() && colIdx >= range.getFirstColumn() && colIdx <= range.getLastColumn()) {
				return true;
			}
		}
		return false;
	}
}
