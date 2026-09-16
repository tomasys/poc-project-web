package com.tomatosystem.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.DataResponse;
import com.cleopatra.protocol.data.TSVEntry;
import com.cleopatra.protocol.data.UploadFile;
import com.monitorjbl.xlsx.StreamingReader;
import com.tomatosystem.core.constants.Alert;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.vo.ExcelVO;

public class ExcelImporter {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private int cellLength = 0;
	private int START_READ_ROW_INDEX = 1;
	private int START_READ_CELL_INDEX = 0;
	
	private String fileEncoding = "UTF-8";
	private final String CVS_SEPERATOR_CHAR = ",";
	
	public int getCellLength(){
		return this.cellLength;
	}
	
	public ExcelImporter(){
	}
	
	public ExcelImporter(String encoding){
		fileEncoding = encoding;
	}
	
	/**
	 * 업로드된 엑셀의 데이터를 반환한다.
	 * @param request
	 * @param response
	 * @param DataRequest - 요청 데이터
	 * @throws Exception
	 */
	public List<ExcelVO> getCellDataList(HttpServletRequest request, HttpServletResponse response, DataRequest DataRequest) throws Exception {
		return getCellDataList(request, response, DataRequest, START_READ_ROW_INDEX, START_READ_CELL_INDEX, null);
	}
	
	/**
	 * 업로드된 엑셀의 데이터를 반환한다.
	 * @param request
	 * @param response
	 * @param DataRequest - 요청 데이터
	 * @throws Exception
	 */
	public List<ExcelVO> getCellDataList(HttpServletRequest request, HttpServletResponse response, DataRequest DataRequest, String defaultDateFormat) throws Exception {
		return getCellDataList(request, response, DataRequest, START_READ_ROW_INDEX, START_READ_CELL_INDEX, defaultDateFormat);
	}
	
	public List<ExcelVO> getCellDataList(HttpServletRequest request, HttpServletResponse response, DataRequest DataRequest, int startRowIndex, int startCellIndex) throws Exception {
		return getCellDataList(request, response, DataRequest, START_READ_ROW_INDEX, START_READ_CELL_INDEX, null);
	}
	/**
	 * 업로드된 엑셀의 데이터를 반환한다.
	 * @param request
	 * @param response
	 * @param DataRequest - 요청 데이터
	 * @param startRowIndex - 시작 행(ROW) 인덱스
	 * @param startCellIndex - 시작 컬럼(CELL) 인덱스
	 * @throws Exception
	 */
	public List<ExcelVO> getCellDataList(HttpServletRequest request, HttpServletResponse response, DataRequest DataRequest, int startRowIndex, int startCellIndex, String defaultDateFormat) throws Exception {
		Map<String, UploadFile[]> uploadFiles = DataRequest.getUploadFiles();
		
		List<ExcelVO> excelData = null;
		if(uploadFiles != null && uploadFiles.size() > 0) {
			Set<Entry<String, UploadFile[]>> entries = uploadFiles.entrySet();
			for(Entry<String, UploadFile[]> entry : entries) {
				UploadFile[] uFiles = entry.getValue();
				if(uFiles.length > 0){
					String strFileExtNm = FileUtil.getFileExtNm(uFiles[0].getFileName());
					if("CSV".equals(strFileExtNm.toUpperCase())){
						excelData = this.parseCSV(uFiles[0].getFile(), startRowIndex, startCellIndex);
					}else{
						excelData = this.parseExcel(uFiles[0].getFile(), startRowIndex, startCellIndex, defaultDateFormat);
					}
					break;
				}
			}
		}
		
		return excelData != null ? excelData : new ArrayList<ExcelVO>();
	}

	public List<String> getCellDataList2(HttpServletRequest request, HttpServletResponse response, DataRequest DataRequest, int startRowIndex, int startCellIndex, String uploadUserNm) throws Exception {
		Map<String, UploadFile[]> uploadFiles = DataRequest.getUploadFiles();
		
		List<String> excelData = null;
		if(uploadFiles != null && uploadFiles.size() > 0) {
			Set<Entry<String, UploadFile[]>> entries = uploadFiles.entrySet();
			for(Entry<String, UploadFile[]> entry : entries) {
				UploadFile[] uFiles = entry.getValue();
				if(uFiles.length > 0){
					String strFileExtNm = FileUtil.getFileExtNm(uFiles[0].getFileName());
					if("CSV".equals(strFileExtNm.toUpperCase())){
						excelData = this.parseCSV2(uFiles[0].getFile(), startRowIndex, startCellIndex, uploadUserNm);
					}
					break;
				}
			}
		}
		
		return excelData;
	}
	
	private List<ExcelVO> parseExcel(File file, int strStartRowIndex, int strStartCellIndex, String defaultDateFormat) throws Exception {
		List<ExcelVO> itemList = new ArrayList<ExcelVO>();
		
		//읽을 파일이 없으면... NULL을 반환
		if(file == null || !file.exists()) return itemList;
		
		String strFileExtNm = FileUtil.getFileExtNm(file.getName());
		String dateFormat = defaultDateFormat != null ? defaultDateFormat : "yyyy-MM-dd";
		
		FileInputStream in = null;
		Workbook workbook = null;
		try {
			in = new FileInputStream(file);
			workbook = null;
			if("XLS".equals(strFileExtNm.toUpperCase())){
				workbook = WorkbookFactory.create(in);
			}else{
				workbook = StreamingReader.builder()
				        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
				        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
				        .open(in);
			}
					
			Sheet sheet = workbook.getSheetAt(0);
			
            Iterator<Row> iterator = sheet.iterator();
            
            //헤더 행의 수만큼 skip
            for(int j = 0, jlen = strStartRowIndex; j < jlen; j++) iterator.next();
            
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            // single row data
            ExcelVO item = null;
           
            Row row = null;
            int rowLength = 0;
            Iterator<Cell> cellIterator = null;
            Cell cell = null;
            String value = null;
            int sCellIdx = strStartCellIndex;
            while(iterator.hasNext()) {
            	item = new ExcelVO(); 
            	
            	row = iterator.next();
            	cellIterator = row.iterator();
            	for(int k = 0; k < sCellIdx; k++) {
            		cellIterator.next();
            	}
            	while(cellIterator.hasNext()) {
            		cell = cellIterator.next();
            		if(cell.getCellType() == CellType.STRING) {
                		value = cell.getStringCellValue();
            		} else if(cell.getCellType() == CellType.NUMERIC) {
            			
            			/**
            			 * 엑셀업로드시 날짜 타입의 경우 숫자로 변형되어 잘못된 값을 리턴하여 수정함. (2019-11-20 정정호 수정)
            			 * ex) 2018-10-11 -> 43384 로 잘못 리턴함.
            			 */
            			if(DateUtil.isCellDateFormatted(cell)) {
            				Date date = cell.getDateCellValue();
            				value = simpleDateFormat.format(date);

            			}else{
            				value =  NumberToTextConverter.toText(cell.getNumericCellValue());
            			}
            			
            		} else if(cell.getCellType() == CellType.BOOLEAN) {
            			value = Boolean.toString(cell.getBooleanCellValue());
            		} else if(cell.getCellType() == CellType.BLANK) {
            			value = "";
            		} else if(cell.getCellType() == CellType.FORMULA) {
            			value = "";
            		} else {
            			value = cell.getStringCellValue();
            		}
            		//값 셋팅
            		ExcelVOUtil.setCellValue(item, cell.getColumnIndex()-sCellIdx, value);
            	}
            	itemList.add(item);
            	rowLength++;
            	//2019.03.28 sulmoiho - 잘못 지정된 빈 CELL까지 읽어들여, 수십만건도 읽어들이려는 오류 방지를 위함
    			if(rowLength > 20001){
    				//throw new AppWorksException("엑셀 데이터 읽기 최대 제한을 초과하였습니다.\n최대 20,000건까지만 읽기 가능합니다.");
    			}
            }
            //컬럼 갯수
            if(row != null){
            	cellLength = row.getPhysicalNumberOfCells();
            }
		} catch (IOException e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} catch (AppWorksException e){
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} finally {
        	//리소스 반환
			if(in != null) {
				try {
					in.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			if(workbook != null) {
				try {
					workbook.close();
					workbook = null;
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			//업로드 파일 삭제
			if(file.exists()){
				file.delete();
			}
		}
		
		return itemList;
	}
	
	private List<ExcelVO> parseCSV(File file, int strStartRowIndex, int strStartCellIndex) throws Exception {
		List<ExcelVO> itemList = new ArrayList<ExcelVO>();
		
		//읽을 파일이 없으면... NULL을 반환
		if(file == null || !file.exists()) return itemList;
		
		FileInputStream in = null;
		BufferedReader reader = null;
		int rowLength = -1;
		ExcelVO item = null;
		try {
			in = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(in, fileEncoding));
			
			String line = null;
			String[] columns = null;
			while ((line=reader.readLine()) != null){
				rowLength++;
				if(rowLength < strStartRowIndex) continue;
				
				item = new ExcelVO();
				columns = line.split(CVS_SEPERATOR_CHAR);
				for (int i=0, len=columns.length;i<len;i++){ 
					//값 셋팅
            		ExcelVOUtil.setCellValue(item, i, columns[i]);
				}
				itemList.add(item);
            	//2019.03.28 sulmoiho - 잘못 지정된 빈 CELL까지 읽어들여, 수십만건도 읽어들이려는 오류 방지를 위함
//    			if(rowLength > 20001){
//    				throw new AppWorksException("CSV 데이터 읽기 최대 제한을 초과하였습니다.\n최대 2,000건까지만 읽기 가능합니다.");
//    			}
			}
		} catch (IOException e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} catch (AppWorksException e){
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} finally {
			//리소스 반환
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
        	//리소스 반환
			if(in != null) {
				try {
					in.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			
			//업로드 파일 삭제
			if(file.exists()){
				file.delete();
			}
		}
		
		return itemList;
	}

private List<String> parseCSV2(File file, int strStartRowIndex, int strStartCellIndex, String uploadUserNm) throws Exception {
		
		//읽을 파일이 없으면... NULL을 반환
		if(file == null || !file.exists()) return null;
		
		FileInputStream in = null;
		BufferedReader reader = null;
		int rowLength = -1;
		
		List excelList2 = new ArrayList();
		
		Boolean hasUploadUserNm = false;
		String strUploadDate = null;
		if(!StringUtil.isEmpty(uploadUserNm)) {
			hasUploadUserNm = true;
			
			LocalDateTime currentDateTime = LocalDateTime.now();
		    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		    strUploadDate = currentDateTime.format(formatter);
		}
		
		try {
			 in = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(in, fileEncoding));
			 String line = null;
			 while ((line=reader.readLine()) != null){
					rowLength++;
					if(rowLength < strStartRowIndex) continue;
					String regex = ",";
		            int limit = -1;
		            String column[] = line.split(regex, limit); //컬럼
		            
		            LinkedHashMap mapData = new LinkedHashMap();	            
		            int iHeaderRow = 0;
		            
		            for(int i=0; i < column.length; i++) {
		            	mapData.put("CELL" + i, column[i]);
		            }
		            
		            if(hasUploadUserNm) {
		            	mapData.put("CELL10", strUploadDate);
		            	mapData.put("CELL11", uploadUserNm);
		            }
					excelList2.add(mapData);	
			}
		 
		} catch (IOException e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} catch (AppWorksException e){
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} finally {
			//리소스 반환
			if(reader != null) {
				try {
					reader.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
       	//리소스 반환
			if(in != null) {
				try {
					in.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			
			//업로드 파일 삭제
			if(file.exists()){
				file.delete();
			}
		}
		
		return excelList2;
	}
	
	private void parseExcel2(File file, int strStartRowIndex, int strStartCellIndex, String defaultDateFormat, HttpServletResponse res) throws Exception {
		List<ExcelVO> itemList = new ArrayList<ExcelVO>();
		final DataResponse dataRes = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, res);
		//읽을 파일이 없으면... NULL을 반환
		if(file == null || !file.exists()) return;
		
		String strFileExtNm = FileUtil.getFileExtNm(file.getName());
		String dateFormat = defaultDateFormat != null ? defaultDateFormat : "yyyy-MM-dd";
		
		FileInputStream in = null;
		Workbook workbook = null;
		try {
			in = new FileInputStream(file);
			workbook = null;
			if("XLS".equals(strFileExtNm.toUpperCase())){
				workbook = WorkbookFactory.create(in);
			}else{
				workbook = StreamingReader.builder()
				        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
				        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
				        .open(in);
			}
					
			Sheet sheet = workbook.getSheetAt(0);
			
            Iterator<Row> iterator = sheet.iterator();
            
            //헤더 행의 수만큼 skip
            //for(int j = 0, jlen = strStartRowIndex; j < jlen; j++) iterator.next();
           
            Map<String, String> header = new HashMap<String, String>();
            dataRes.send(new TSVEntry("dsHeaderColumns"));
            // single row header
               if(iterator.hasNext()) {
               	Row row = iterator.next();
               	Iterator<Cell> cellIterator = row.iterator();
               	while(cellIterator.hasNext()) {
               		Cell cell = cellIterator.next();
               		int colIdx = cell.getColumnIndex();
               		String value = cell.getStringCellValue();
               		header.put(Integer.toString(colIdx) , value);
               	}
                dataRes.send(header);
               }
//               List<Map<String,String>> dsHeader = new ArrayList<Map<String, String>>();
//               dsHeader.add(header);
              
            

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            // single row data
            ExcelVO item = null;
            Row row = null;
            int rowLength = 0;
            Iterator<Cell> cellIterator = null;
            Cell cell = null;
            String value = null;
            int sCellIdx = strStartCellIndex;
            dataRes.send(new TSVEntry("dsExcel"));
            while(iterator.hasNext()) {
            	item = new ExcelVO(); 
            	
            	row = iterator.next();
            	cellIterator = row.iterator();
            	for(int k = 0; k < sCellIdx; k++) {
            		cellIterator.next();
            		
            	}
            	Map<String, String> rowData = new HashMap<String, String>();
            	
            	while(cellIterator.hasNext()) {
            		cell = cellIterator.next();
            		if(cell.getCellType() == CellType.STRING) {
                		value = cell.getStringCellValue();
            		} else if(cell.getCellType() == CellType.NUMERIC) {
            			
            			/**
            			 * 엑셀업로드시 날짜 타입의 경우 숫자로 변형되어 잘못된 값을 리턴하여 수정함. (2019-11-20 정정호 수정)
            			 * ex) 2018-10-11 -> 43384 로 잘못 리턴함.
            			 */
            			if(DateUtil.isCellDateFormatted(cell)) {
            				Date date = cell.getDateCellValue();
            				value = simpleDateFormat.format(date);

            			}else{
            				value =  NumberToTextConverter.toText(cell.getNumericCellValue());
            			}
            			
            		} else if(cell.getCellType() == CellType.BOOLEAN) {
            			value = Boolean.toString(cell.getBooleanCellValue());
            		} else if(cell.getCellType() == CellType.BLANK) {
            			value = "";
            		} else if(cell.getCellType() == CellType.FORMULA) {
            			value = "";
            		} else {
            			value = cell.getStringCellValue();
            		}
            		//값 셋팅
            		String headerNm = header.get(cell.getColumnIndex());            	
            		rowData.put("CELL" + cell.getColumnIndex() , value);
            		//ExcelVOUtil.setCellValue(item, cell.getColumnIndex()-sCellIdx, value);
            	}
        		dataRes.send(rowData);
        		rowData.clear();
            	//itemList.add(item);
            	rowLength++;
            	//2019.03.28 sulmoiho - 잘못 지정된 빈 CELL까지 읽어들여, 수십만건도 읽어들이려는 오류 방지를 위함
    			if(rowLength > 20001){
    				//throw new AppWorksException("엑셀 데이터 읽기 최대 제한을 초과하였습니다.\n최대 20,000건까지만 읽기 가능합니다.");
    			}
            }
            dataRes.flush();
            //컬럼 갯수
            if(row != null){
            	cellLength = row.getPhysicalNumberOfCells();
            }
		} catch (IOException e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} catch (AppWorksException e){
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} finally {
        	//리소스 반환
			if(in != null) {
				try {
					in.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			if(workbook != null) {
				try {
					workbook.close();
					workbook = null;
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			//업로드 파일 삭제
			if(file.exists()){
				file.delete();
			}
		}
	}
	
	public void tsvSend(File file, String strStartRowIndex, String strStartCellIndex, String defaultDateFormat, HttpServletResponse res) throws Exception {
		List<ExcelVO> itemList = new ArrayList<ExcelVO>();
		final DataResponse dataRes = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, res);
		//읽을 파일이 없으면... NULL을 반환
		if(file == null || !file.exists()) return;
		
		String strFileExtNm = FileUtil.getFileExtNm(file.getName());
		String dateFormat = defaultDateFormat != null ? defaultDateFormat : "yyyy-MM-dd";
		
		FileInputStream in = null;
		Workbook workbook = null;
		try {
			in = new FileInputStream(file);
			workbook = null;
			if("XLS".equals(strFileExtNm.toUpperCase())){
				workbook = WorkbookFactory.create(in);
			}else{
				workbook = StreamingReader.builder()
				        .rowCacheSize(100)    // number of rows to keep in memory (defaults to 10)
				        .bufferSize(4096)     // buffer size to use when reading InputStream to file (defaults to 1024)
				        .open(in);
			}
					
			Sheet sheet = workbook.getSheetAt(0);
			
            Iterator<Row> iterator = sheet.iterator();
           
            Map<String, String> header = new HashMap<String, String>();
            dataRes.send(new TSVEntry("dsHeaderColumns"));
            // single row header
               if(iterator.hasNext()) {
               	Row row = iterator.next();
               	Iterator<Cell> cellIterator = row.iterator();
               	while(cellIterator.hasNext()) {
               		Cell cell = cellIterator.next();
               		int colIdx = cell.getColumnIndex();
               		String value = cell.getStringCellValue();
               		header.put(Integer.toString(colIdx) , value);
               	}
                dataRes.send(header);
               }
//               List<Map<String,String>> dsHeader = new ArrayList<Map<String, String>>();
//               dsHeader.add(header);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
            // single row data
            ExcelVO item = null;
            Row row = null;
            int rowLength = 0;
            Iterator<Cell> cellIterator = null;
            Cell cell = null;
            String value = null;
            int sCellIdx = Integer.parseInt(strStartCellIndex);
            dataRes.send(new TSVEntry("dsExcel"));
            while(iterator.hasNext()) {
            	item = new ExcelVO(); 
            	
            	row = iterator.next();
            	cellIterator = row.iterator();
            	for(int k = 0; k < sCellIdx; k++) {
            		cellIterator.next();
            		
            	}
            	Map<String, String> rowData = new HashMap<String, String>();
            	
            	while(cellIterator.hasNext()) {
            		cell = cellIterator.next();
            		if(cell.getCellType() == CellType.STRING) {
                		value = cell.getStringCellValue();
            		} else if(cell.getCellType() == CellType.NUMERIC) {
            			
            			/**
            			 * 엑셀업로드시 날짜 타입의 경우 숫자로 변형되어 잘못된 값을 리턴하여 수정함. (2019-11-20 정정호 수정)
            			 * ex) 2018-10-11 -> 43384 로 잘못 리턴함.
            			 */
            			if(DateUtil.isCellDateFormatted(cell)) {
            				Date date = cell.getDateCellValue();
            				value = simpleDateFormat.format(date);

            			}else{
            				value =  NumberToTextConverter.toText(cell.getNumericCellValue());
            			}
            			
            		} else if(cell.getCellType() == CellType.BOOLEAN) {
            			value = Boolean.toString(cell.getBooleanCellValue());
            		} else if(cell.getCellType() == CellType.BLANK) {
            			value = "";
            		} else if(cell.getCellType() == CellType.FORMULA) {
            			value = "";
            		} else {
            			value = cell.getStringCellValue();
            		}
            		//값 셋팅
            		String headerNm = header.get(cell.getColumnIndex());            	
            		rowData.put("CELL" + cell.getColumnIndex() , value);
            		//ExcelVOUtil.setCellValue(item, cell.getColumnIndex()-sCellIdx, value);
            	}
        		dataRes.send(rowData);
        		rowData.clear();
            	//itemList.add(item);
            	rowLength++;
            	//2019.03.28 sulmoiho - 잘못 지정된 빈 CELL까지 읽어들여, 수십만건도 읽어들이려는 오류 방지를 위함
    			if(rowLength > 20001){
    				//throw new AppWorksException("엑셀 데이터 읽기 최대 제한을 초과하였습니다.\n최대 20,000건까지만 읽기 가능합니다.");
    			}
            }
            dataRes.flush();
            //컬럼 갯수
            if(row != null){
            	cellLength = row.getPhysicalNumberOfCells();
            }
		} catch (IOException e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} catch (AppWorksException e){
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException(e.getMessage());
		} catch (Exception e){
			e.printStackTrace();
			//엑셀 파일을 읽는 도중 오류가 발생하였습니다.
			throw new AppWorksException("CMN003.CMN@CMN042", Alert.ERROR);
		} finally {
        	//리소스 반환
			if(in != null) {
				try {
					in.close();
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			if(workbook != null) {
				try {
					workbook.close();
					workbook = null;
				} catch(IOException e){
					logger.debug(e.getMessage());
				} catch(Exception e) {
					logger.debug(e.getMessage());
				}
			}
			//업로드 파일 삭제
			if(file.exists()){
				file.delete();
			}
		}
	}
}
