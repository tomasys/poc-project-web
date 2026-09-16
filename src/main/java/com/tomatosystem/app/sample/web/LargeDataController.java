package com.tomatosystem.app.sample.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cleopatra.json.JSONArray;
import com.cleopatra.json.JSONObject;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.app.sample.service.LdqServiceImpl;
import com.tomatosystem.core.massive.ProgressExcelStore;
import com.tomatosystem.core.massive.StreamExcelResponseAdapter;
import com.tomatosystem.core.service.MassResponseService;
import com.tomatosystem.core.tsv.PagedTSV;
import com.tomatosystem.core.tsv.PagedTSVSheetHandler;
import com.tomatosystem.core.tsv.PagedTSVWriter;
import com.tomatosystem.core.tsv.ProgressInputStream;
import com.tomatosystem.core.util.CSVSheetHandler;
import com.tomatosystem.core.util.ExcelImportSheetHandler;
import com.tomatosystem.core.util.StringUtil;
import com.tomatosystem.core.util.UtilUuidMgr;

@Controller
@RequestMapping("/LDQ")
public class LargeDataController {

	@Autowired
	private LdqServiceImpl ldqService;
	
	@Autowired
	private MassResponseService massService;
	private static int CACHE_SEQ = 0;
	private static final Map<String, AtomicInteger> progressStore = new ConcurrentHashMap<>();
	private static final Logger logger = LogManager.getLogger(LargeDataController.class);
	
	@RequestMapping("/LdqList.do")
	public void LdqList(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest)
			throws Exception {

		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("ROWCOUNT", param.getValue("ROWCOUNT"));
		try {
//			String progressId = dataRequest.getParameter("progress"); // 클라이언트에서 progressId를 받아옵니다.
//			progressStore.put(progressId, new AtomicInteger(0));
			ldqService.selectLdqTsvList(mapParam, response, null, progressStore);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception();
		}
	}
	@RequestMapping("/LdqImportCsv.do")
	public void LdqImportCsv(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
				
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		String progressId = dataRequest.getParameter("progress");
		if(headerLine == null || "".equals(headerLine)) {
			headerLine = "1";
		}
		int numHeaderLine = Integer.parseInt(headerLine);
		
		// 엑셀 업로드 시, 업로드 하기 위한 컬럼명을 직접 설정 (default: 엑셀 내 각 셀의 value)
		String strColumnInfo = dataRequest.getParameter("columnInfo");
		org.json.simple.JSONObject columnJSON = null;
		if(!"".equals(strColumnInfo) && strColumnInfo != null) {
			JSONParser parser = new JSONParser();
			columnJSON = (org.json.simple.JSONObject) parser.parse(strColumnInfo);
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
				CSVSheetHandler.readCSV(files[0], columnJSON, response);
			}
		}
	}
	
	@RequestMapping("/LdqImportExcel.do")
	public void LdqImportExcel(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
				
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		String progressId = dataRequest.getParameter("progress");
		String isInsert = StringUtil.fixNull(dataRequest.getParameter("isInsert"));
		
		
		if(headerLine == null || "".equals(headerLine)) {
			headerLine = "1";
		}
		int numHeaderLine = Integer.parseInt(headerLine);
		
		// 엑셀 업로드 시, 업로드 하기 위한 컬럼명을 직접 설정 (default: 엑셀 내 각 셀의 value)
		String strColumnInfo = dataRequest.getParameter("columnInfo");
		org.json.simple.JSONObject columnJSON = null;
		if(!"".equals(strColumnInfo) && strColumnInfo != null) {
			JSONParser parser = new JSONParser();
			columnJSON = (org.json.simple.JSONObject) parser.parse(strColumnInfo);
		}
				
		// 업로드 할 데이터셋 ID
		String strDataset = dataRequest.getParameter("datasetId");
		if(strDataset == null || "".equals(strDataset)) {
			strDataset = "dsExcel";
		}
		
		Map<String, File[]> uploadFiles = dataRequest.getFiles();
		Set<Entry<String, File[]>> entries = uploadFiles.entrySet();

		for (Entry<String, File[]> entry : entries) {
		    String name = entry.getKey();
		    File[] files = entry.getValue();

		    if (files != null && files.length > 0) {
		        try (FileInputStream in = new FileInputStream(files[0])) {
		        	if(isInsert.equals("Y")) {
		        		ldqService.uploadLdqExcel(files[0], columnJSON, response);
		        	}else {
		        		ExcelImportSheetHandler.readExcel(files[0], columnJSON, response);
		        	}
		            
		        } catch (Exception e) {
		            e.printStackTrace(); // 필요하면 로깅 처리
		        }
		    }
		}
	}
	
	private PagedTSV buildCache(String cacheId, String progressId, File file, String[] columnTypes)
			throws IOException {
		PagedTSV pagedTSV = new PagedTSV(cacheId, columnTypes, 5000);
		pagedTSV.purge();
		logger.info(MessageFormat.format("신규 캐시 작성: {0}", pagedTSV.getWorkDir().getAbsolutePath()));
		PagedTSVWriter tsvWriter = pagedTSV.openWriter();
		try {
			// 파일 확장자 추출
			String fileName = file.getName().toLowerCase();
//			String fileExtension = "";
//			int lastDotIndex = fileName.lastIndexOf('.');
//			if (lastDotIndex > 0) {
//				fileExtension = fileName.substring(lastDotIndex + 1);
//			}
			
			// 확장자에 따라 분기 처리
			logger.info(MessageFormat.format("파일 타입 감지: {0} (확장자: {1})", file.getName()));
			
			if (fileName.contains(".csv")) {
				// CSV 파일 처리
				parseCsvFile(file, progressId, tsvWriter);
			} else if (fileName.contains(".xlsx") || fileName.contains(".xlsm")) {
				// Excel 파일 처리 (xlsx, xlsm)
				parseExcelSheet(file, progressId, tsvWriter);
			} else if (fileName.contains(".xls")) {
				// 구버전 Excel 파일
				throw new IOException("XLS 파일 형식은 지원하지 않습니다. XLSX 형식으로 변환 후 업로드해주세요.");
			} else {
				// 지원하지 않는 형식
				throw new IOException(MessageFormat.format(
					"지원하지 않는 파일 형식입니다: {0}. CSV 또는 XLSX 파일만 지원합니다.", ""));
			}
			
			pagedTSV.setRowCount(tsvWriter.getLineCount());
			pagedTSV.saveManifest();
			logger.info(MessageFormat.format("캐시 작성 완료: {0} (총 {1}건)", 
				cacheId, tsvWriter.getLineCount()));
			return pagedTSV;
		} catch (IOException e) {
			logger.error(MessageFormat.format("캐시 작성 실패: {0}", cacheId), e);
			throw e;
		} finally {
			tsvWriter.close();
		}
	}

	private void parseExcelSheet(File excelFile, String progressId, PagedTSVWriter tsvWriter)
			throws IOException {
		OPCPackage opc = null;
		try {
			opc = OPCPackage.open(excelFile, PackageAccess.READ);
			XSSFReader xssfReader = new XSSFReader(opc);
			StylesTable styles = xssfReader.getStylesTable();
			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
			
			// SAXParserFactory는 재사용 (성능 개선)
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			saxParserFactory.setNamespaceAware(true);
			
			Iterator<InputStream> sheetsIterator = xssfReader.getSheetsData();
			int sheetIndex = 0;
			
			while (sheetsIterator.hasNext()) {
				InputStream inputStream = null;
				try {
					inputStream = sheetsIterator.next();
					logger.info(MessageFormat.format("시트 {0} 처리 중...", sheetIndex + 1));
					
					// 프로그래스바 표현
//					inputStream = ProgressInputStream.wrapWithKey(inputStream, progressId);
					inputStream = ProgressInputStream.wrapWithKey(inputStream, progressId, sheetIndex + 1);
					
					// 첫 번째 시트만 헤더 포함, 나머지는 데이터만
//					int skipRows = (sheetIndex == 0) ? 0 : 1;
					PagedTSVSheetHandler sheetHandler = new PagedTSVSheetHandler(tsvWriter, 1);
					
					InputSource inputSource = new InputSource(inputStream);
					ContentHandler handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);
					
					SAXParser parser = saxParserFactory.newSAXParser();
					XMLReader xmlReader = parser.getXMLReader();
					xmlReader.setContentHandler(handle);
					xmlReader.parse(inputSource);
					
					logger.info(MessageFormat.format("시트 {0} 처리 완료 (현재 총 라인: {1})", 
						sheetIndex + 1, tsvWriter.getLineCount()));
					
					sheetIndex++;
				} catch (Exception e) {
					logger.error(MessageFormat.format("시트 {0} 처리 중 오류 발생", sheetIndex + 1), e);
					// 한 시트 실패해도 계속 진행
				} finally {
					if (inputStream != null) {
						try {
							inputStream.close();
						} catch (IOException e) {
							logger.warn("InputStream 닫기 실패", e);
						}
					}
				}
			}
			
			logger.info(MessageFormat.format("전체 처리 완료 - 시트 수: {0}, 총 라인 수: {1}", 
				sheetIndex, tsvWriter.getLineCount()));
			
		} catch (Exception e) {
			logger.error("Excel 파일 파싱 중 오류 발생", e);
			throw new IOException("Excel 파일 파싱 실패: " + e.getMessage(), e);
		} finally {
			if (opc != null) {
				try {
					//opc.close() 안할시 POI의 압축(Zip) 처리 및 Big XML 처리 과정이 Temp 디렉토리 사용량 폭증을 유발 C:\Users\WANI\AppData\Local\Temp\poifiles
					opc.close();
				} catch (IOException e) {
					logger.warn("OPCPackage 닫기 실패", e);
				}
			}
		}
	}

	private void parseCsvFile(File csvFile, String progressId, PagedTSVWriter tsvWriter)
			throws IOException {
		BufferedReader reader = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(csvFile);
			
			// 프로그래스바 표현
			inputStream = ProgressInputStream.wrapWithKey(inputStream, progressId);
			
			// CSV 인코딩 감지 및 Reader 생성
			reader = new BufferedReader(new InputStreamReader(inputStream, detectEncoding(csvFile)));
			
			String line;
			int lineCount = 0;
			boolean isFirstLine = true;
			
			logger.info(MessageFormat.format("CSV 파일 처리 시작: {0}", csvFile.getName()));
			
			while ((line = reader.readLine()) != null) {
				if (line.trim().isEmpty()) {
					continue; // 빈 줄 건너뛰기
				}
				
				// 첫 번째 행(헤더)은 건너뛰기
				if (isFirstLine) {
					isFirstLine = false;
					logger.info(MessageFormat.format("헤더 행 건너뛰기: {0}", line));
					continue;
				}
				
				// CSV 파싱 (쉼표로 분리, 따옴표 처리)
				String[] columns = parseCsvLine(line);
				
				// 새로운 행 시작
				tsvWriter.beginNewLine();
				
				// 각 셀 값 작성
				for (String column : columns) {
					tsvWriter.writCellValue(column);
				}
				
				lineCount++;
				
				// 진행 상황 로깅 (10000건마다)
				if (lineCount % 10000 == 0) {
					logger.info(MessageFormat.format("처리 중... {0}건", lineCount));
				}
			}
			
			logger.info(MessageFormat.format("CSV 파일 처리 완료 - 총 라인 수: {0}", lineCount));
			
		} catch (Exception e) {
			logger.error("CSV 파일 파싱 중 오류 발생", e);
			throw new IOException("CSV 파일 파싱 실패: " + e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					logger.warn("BufferedReader 닫기 실패", e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.warn("InputStream 닫기 실패", e);
				}
			}
		}
	}

	/**
	 * CSV 라인을 파싱 (쉼표 구분, 따옴표 처리)
	 */
	private String[] parseCsvLine(String line) {
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;
		
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			
			if (c == '"') {
				// 따옴표 처리
				if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
					// 연속된 따옴표는 하나의 따옴표로
					current.append('"');
					i++;
				} else {
					inQuotes = !inQuotes;
				}
			} else if (c == ',' && !inQuotes) {
				// 따옴표 밖의 쉼표는 구분자
				result.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		
		// 마지막 컬럼 추가
		result.add(current.toString().trim());
		
		return result.toArray(new String[0]);
	}

	/**
	 * 파일 인코딩 감지 (UTF-8, EUC-KR 등)
	 */
	private String detectEncoding(File file) throws IOException {
		// BOM 체크로 UTF-8 감지
		try (InputStream is = new FileInputStream(file)) {
			byte[] bom = new byte[3];
			int read = is.read(bom);
			
			if (read >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF) {
				return "UTF-8";
			}
		}
		
		// 기본값 (시스템 환경에 따라 조정 가능)
		// 한국어 환경이면 EUC-KR 또는 MS949를 시도해볼 수 있음
		return "UTF-8";
	}

	
	private File getFileFromDataRequest(DataRequest dataRequest) {
		// @formatter:off
		Optional<File> uploadedFile = dataRequest.getFiles().entrySet().stream()
			.map(it -> it.getValue())
			.filter(files -> files.length > 0)
			.map(files -> files[0])
			.findAny();
		// @formatter:on

		if (uploadedFile.isPresent()) {
			return uploadedFile.get();
		} else {
			return null;
		}
	}
	
	
	@RequestMapping("/progress.do")
	public JSONDataView getUploadProgress(DataRequest dataRequest) throws Exception {
		String progressId = dataRequest.getParameter("progress");
		int progress = ldqService.getProgress(progressId, progressStore);
		Map<String, Object> data = new HashMap<>();
		data.put("progressId", progressId);
		data.put("progress", progress);
		dataRequest.setMetadata(true, data);
		return new JSONDataView();
	}
	
	@RequestMapping("/progress2.do")
	public JSONDataView getUploadProgress2(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		String progressId = dataRequest.getParameter("progress");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("progressId", progressId);
		data.put("progress", ProgressInputStream.getPercent(progressId));
		data.put("sheetIndex", ProgressInputStream.getSheetIndex(progressId));

		dataRequest.setMetadata(true, data);
		return new JSONDataView();
	}
	
	@RequestMapping("/progressDataRead.do")
	public JSONDataView progressDataRead(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		 
		String progressId = dataRequest.getParameter("progressId");
		String isCsvExport = dataRequest.getParameter("isCsvExport");
		
		
		int progress = -1;
		int sheetIndex = -1;
		if(ProgressExcelStore.progressMap.get(progressId) == null) {
			progress = ProgressExcelStore.progressMap.getOrDefault(progressId, -1);
			sheetIndex = ProgressExcelStore.progressMap.getOrDefault(progressId + "_sheet", -1);
		}else {
			progress = ProgressExcelStore.progressMap.getOrDefault(progressId, 0);
			sheetIndex = ProgressExcelStore.progressMap.getOrDefault(progressId + "_sheet", 0);
		}
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("progress", progress);
		data.put("progressId", progressId);
		data.put("sheetIndex", sheetIndex);
		data.put("isCsvExport", isCsvExport);

		dataRequest.setMetadata(true, data);
		return new JSONDataView();
	}
	
	@RequestMapping("/fetch.do")
	public void fetchDataWindowPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String cacheId = request.getParameter("cacheId");
		int rowStart = Integer.parseInt(request.getParameter("rowStart"));
		int rowCount = Integer.parseInt(request.getParameter("rowCount"));

		int sortIndex = Integer.parseInt(request.getParameter("sortIndex"));
		
		String sortColumns = request.getParameter("sortColumns");
		String sortAscending = request.getParameter("sortAscending");
		
		boolean asc = Boolean.parseBoolean(request.getParameter("asc"));

		response.setContentType("text/tab-separated-values");
		response.setCharacterEncoding("utf-8");

		PagedTSV pagedTSV = new PagedTSV(cacheId);
		
//		if (sortIndex >= 0) {
//			pagedTSV = pagedTSV.getSorted(sortIndex, asc);
//		}
		
		if (sortIndex >= 0 && StringUtil.isNotNull(sortColumns) && StringUtil.isNotNull(sortAscending)) {
			
			String[] arrSortColumns = sortColumns.split(",");
			String[] arrSortAscending = sortAscending.split(",");
			
	        int[] intColumns = new int[arrSortColumns.length];
	        boolean[] isColumnNumType = new boolean[arrSortColumns.length];
	        
	        for (int i = 0; i < arrSortColumns.length; i++) {
	        	intColumns[i] = Integer.parseInt(arrSortColumns[i].trim());
	        	String columnType = pagedTSV.getColumnType(Integer.parseInt(arrSortColumns[i].trim())); 
	        	isColumnNumType[i] = columnType != null && columnType.equals("number");
	        }
	        
	        boolean[] isAsc = new boolean[arrSortAscending.length];
	        for (int i = 0; i < arrSortAscending.length; i++) {
	        	isAsc[i] = Boolean.parseBoolean(arrSortAscending[i].trim()); 
	        }
	        
			pagedTSV = pagedTSV.getSorted(intColumns,isAsc, isColumnNumType, sortIndex, asc);
		}
		
		pagedTSV.openReader().pipeTo(response.getWriter(), rowStart, rowCount);
	}
	
	@RequestMapping("/massiveExcelExport.do")
	public void massiveExport(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		JSONObject data = dataRequest.getRequestObject();
		
		HttpSession session = request.getSession();
		
		String randomUUID = data.getString("excelKey");
		
		if(StringUtil.isNotNullEmpty(randomUUID)) {
			randomUUID = UtilUuidMgr.randomUUID().toString();
		}
		
		// randomUUID로 관리
		StreamExcelResponseAdapter adapter = (StreamExcelResponseAdapter) session.getAttribute("export" + randomUUID);
		
//		JSONObject jsonObject = data.getJSONObject("searchParam");
//		Map<String, Object> searchParamMap = jsonObject.toMap();
		
		adapter = new StreamExcelResponseAdapter("export" + randomUUID, data);
		try {
			ldqService.exportExcelToStream(request, response, dataRequest, "com.tomatosystem.app.sample.mapper.LdqMapper.selectLdgList");			
		}catch (Exception e) {
			// TODO: handle exception
			 e.printStackTrace();
			throw new Exception();
		}
	}
	
	@RequestMapping("/csvExport.do")
	public void csvExport(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		JSONObject data = dataRequest.getRequestObject();
		
		JSONObject jsonObject = data.getJSONObject("searchParam");
		Map<String, Object> searchParamMap = jsonObject.toMap();
		
		String strFileNm = StringUtil.nullToEmpty(data.getString("fileName"));
		JSONArray columnInfo = data.getJSONArray("columnInfo");
		
		LinkedHashMap<String, Object> linkedMap = new LinkedHashMap<>();

		for (int i = 0; i < columnInfo.length(); i++) {
		    Object value = columnInfo.get(i);
		    // key와 value를 동일하게 사용
		    linkedMap.put(String.valueOf(value), value);
		}

		massService.exportExcelToStreamCsv(request, response, strFileNm, linkedMap, "com.tomatosystem.app.sample.mapper.LdqMapper.selectLdgList", searchParamMap);
		
	}
	
	@RequestMapping("/selectCount.do")
	public JSONDataView selectCount(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		 
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("ROWCOUNT", param.getValue("ROWCOUNT"));
		
		int totalRows = ldqService.getSelectLdqCntProgress(mapParam);
		
		if(totalRows > 0) {
			ProgressExcelStore.progressMap.put(param.getValue("progressId"), 0);
			ProgressExcelStore.progressMap.put(param.getValue("progressId")+"_sheet", 0);
		}
		
		Map<String, String> mapRes = new HashMap<>();
		mapRes.put("totRowCnt", String.valueOf(totalRows));
		dataRequest.setResponse("dmResCnt",mapRes);
		return new JSONDataView();
	}
}
