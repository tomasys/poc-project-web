package com.tomatosystem.app.a3.web;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.binary.XSSFBSheetHandler.SheetContentsHandler;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.massive.StreamCsvResponseAdapter;
import com.tomatosystem.core.massive.StreamExcelResponseAdapter;
import com.tomatosystem.core.profiler.UtilProfilingStack;
import com.tomatosystem.core.tsv.PagedTSV;
import com.tomatosystem.core.tsv.PagedTSVReader;
import com.tomatosystem.core.tsv.PagedTSVSheetHandler;
import com.tomatosystem.core.tsv.PagedTSVWriter;
import com.tomatosystem.core.tsv.PagedXMLToTSVHandler;
import com.tomatosystem.core.tsv.ProgressInputStream;
import com.tomatosystem.core.tsv.XMLToTSVHandler;
import com.tomatosystem.core.util.CSVSheetHandler;
import com.tomatosystem.core.util.ExcelImportSheetHandler;
import com.tomatosystem.core.util.StringUtil;
import com.tomatosystem.core.util.UtilExcel;

@Controller
@RequestMapping("/A37")
public class A37Controller {
	private static final Logger logger = (Logger) LogManager.getLogger(A37Controller.class);
	private static int CACHE_SEQ = 0;

	private StreamExcelResponseAdapter adapter;
    private boolean isHeaderSet = false;
    private StreamCsvResponseAdapter csvAdapter;
	
    @Autowired
	private ResourceLoader resourceLoader;
	static {
	}
	
	@RequestMapping("/excelFileTsv.do")
	public void importExcelFile2(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
				
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		String progressId = dataRequest.getParameter("progress");
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
				ExcelImportSheetHandler.readExcel(files[0], columnJSON, response);
			}
		}
	}
	
	@RequestMapping("/excelFileCsv.do")
	public void importCsvFile(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
				
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		String progressId = dataRequest.getParameter("progress");
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
				CSVSheetHandler.readCSV(files[0], columnJSON, response);
			}
		}
	}
	
	@RequestMapping("/excelDown.do")
	public JSONDataView excelDown(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		ParameterGroup dsMst = dataRequest.getParameterGroup("dsList");
		String gridTitle = dataRequest.getParameter("gridTitle");
//		ParameterGroup dmDataSetInfo = dataRequest.getParameterGroup("dmDataSetInfo");
//		Map<String, String> mapDataSetinfo = dmDataSetInfo.getSingleValueMap();
//		for (String key : mapDataSetinfo.keySet()) {
//			mapHeader.put(key, mapDataSetinfo.get(key));
//		}
		
		List<Map<String, String>> listData = dsMst.getAllRowList();
		UtilExcel.createExcelSendBrowser(request, response, gridTitle + ".xlsx", gridTitle , setHeader(), listData, dataRequest, null, null);

		return new JSONDataView();
	}
	
	@RequestMapping("/progress.do")
	public JSONDataView getUploadProgress(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		String progressId = dataRequest.getParameter("progress");
		String type = dataRequest.getParameter("type");

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("progressId", progressId);
		data.put("progress", ProgressInputStream.getPercent(progressId));
		
		logger.debug(type + "의 xml 파일 읽기 진행률 : " + (Math.abs(ProgressInputStream.getPercent(progressId) * 100)));

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
	
	
	@RequestMapping("/fetchLog.do")
	public void fetchLog(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String cacheId = request.getParameter("cacheId");
		String logFilename = request.getParameter("logFilename");
		int rowStart = Integer.parseInt(request.getParameter("rowStart")); 
		int rowCount = Integer.parseInt(request.getParameter("rowCount"));

		response.setContentType("text/tab-separated-values");
		response.setCharacterEncoding("utf-8");

		PagedTSV pagedTSV = new PagedTSV(cacheId);
		
		ServletContext context = request.getServletContext();
		String realPath = context.getRealPath("ui\\data\\xml\\dsCovInspe.tsv");
		
		pagedTSV.openReader().pipeToLog(response.getWriter(), rowStart, rowCount, "C:\\tomatosystem\\log\\멀티TAB1.log");
	}
	
	@RequestMapping("/xmlToTsvFetch.do")
	public JSONDataView xmlToTsvFetch(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		
		logger.debug("[org.springframework.web.servlet.DispatcherServlet] POST \"/M13/xmlToTsvFetch.do");
		UtilProfilingStack.push("XML_TSV_Fatch_Stream 방식의 파일(16,865건) 읽은 시간");
		
		File excelFile = getFileFromDataRequest(dataRequest);
		String colTypeExp = dataRequest.getParameter("columnTypes");
		String progressId = dataRequest.getParameter("progress");
		String[] columnTypes = colTypeExp != null ? colTypeExp.split("[ ,]+") : null;
		
		
		if(excelFile == null) {
			ServletContext context = request.getServletContext();
			String realPath = context.getRealPath("ui\\data\\xml\\dsCovInspe.xml");
			excelFile = new File(realPath);
		}
		
		if (excelFile != null) {
			String cacheId = String.format("xml-%03d", CACHE_SEQ++);
			ProgressInputStream.setPlaceholder(progressId);
			PagedTSV pagedTSV = parserXMLToTSV(cacheId, progressId, excelFile, columnTypes);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("cacheId", pagedTSV.fCacheId);
			data.put("rowCount", pagedTSV.getRowCount());
			data.put("readTime", UtilProfilingStack.getPop("XML_TSV_Fatch_Stream 방식의 파일(16,865건) 읽은 시간"));
			
			// 엑셀의 캐시 아이디를 클라이언트에 보냄.
			dataRequest.setMetadata(true, data);
		}
		UtilProfilingStack.pop("XML_TSV_Fatch_Stream 방식의 파일(16,865건) 읽은 시간");
		logger.debug("[org.springframework.web.servlet.DispatcherServlet] Completed 200 OK");
		return new JSONDataView();
	}
	
	@RequestMapping("/xmlToTsvFetch200.do")
	public JSONDataView xmlToTsvFetch200(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		
		logger.debug("[org.springframework.web.servlet.DispatcherServlet] POST \"/M13/xmlToTsvFetch200.do");
		UtilProfilingStack.push("XML_TSV_Fatch_Stream 방식의 파일(200,000건) 읽은 시간");
		
		File excelFile = getFileFromDataRequest(dataRequest);
		String colTypeExp = dataRequest.getParameter("columnTypes");
		String progressId = dataRequest.getParameter("progress");
		String[] columnTypes = colTypeExp != null ? colTypeExp.split("[ ,]+") : null;
		
		
		if(excelFile == null) {
			ServletContext context = request.getServletContext();
			String realPath = context.getRealPath("ui\\data\\xml\\dsCovInspe_200000.xml");
			excelFile = new File(realPath);
		}
		
		if (excelFile != null) {
			String cacheId = String.format("xml-%03d", CACHE_SEQ++);
			ProgressInputStream.setPlaceholder(progressId);
			PagedTSV pagedTSV = parserXMLToTSV(cacheId, progressId, excelFile, columnTypes);
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("cacheId", pagedTSV.fCacheId);
			data.put("rowCount", pagedTSV.getRowCount());
			data.put("readTime", UtilProfilingStack.getPop("XML_TSV_Fatch_Stream 방식의 파일(200,000건) 읽은 시간"));
			
			// 엑셀의 캐시 아이디를 클라이언트에 보냄.
			dataRequest.setMetadata(true, data);
		}
		UtilProfilingStack.pop("XML_TSV_Fatch_Stream 방식의 파일(200,000건) 읽은 시간");
		logger.debug("[org.springframework.web.servlet.DispatcherServlet] Completed 200 OK");
		
		return new JSONDataView();
	}
	
	@RequestMapping("/xmlToTsv.do")
	public void xmlToTsv(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		
		logger.debug("[org.springframework.web.servlet.DispatcherServlet] POST \"/M13/xmlToTsv.do");
		UtilProfilingStack.push("XML_TSV_Stream 방식의 파일(16,865건) 읽은 시간");
		
		ServletContext context = request.getServletContext();
		String realPath = context.getRealPath("ui\\data\\xml\\dsCovInspe.xml");
		File file = new File(realPath);
		response.setContentType("text/tab-separated-values");
		response.setCharacterEncoding("utf-8");
		BufferedWriter writer = null;
		
		try {
			writer = new BufferedWriter(response.getWriter());
	        SAXParserFactory factory = SAXParserFactory.newInstance();
	        factory.setNamespaceAware(true);
	        SAXParser saxParser = factory.newSAXParser();
	        
	        XMLToTSVHandler handler = new XMLToTSVHandler(writer);
	        saxParser.parse(file, handler);
	    } catch (Exception e) {
	        throw new IOException("Error processing XML file to TSV", e);
	    }finally {
	    	if(writer != null) {
	    		writer.close();
	    	}
	    	UtilProfilingStack.pop("XML_TSV_Stream 방식의 파일(16,865건) 읽은 시간");
	    	logger.debug("[org.springframework.web.servlet.DispatcherServlet] Completed 200 OK");
	    }
		
//        try {
//			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
//			Document doc = dBuilder.parse(file);
//			doc.getDocumentElement().normalize();
//
//			NodeList nodeList = doc.getElementsByTagName("Row");
//			writer = new BufferedWriter(response.getWriter());
//			// Write the header line (column names)
//			NodeList firstRow = nodeList.item(0).getChildNodes();
//			StringBuilder builder = new StringBuilder();
//			
//			
//			// Header 라인 작성 (컬럼 이름)
//			IntStream.range(0, firstRow.getLength())
//		    .mapToObj(i -> firstRow.item(i)) // 각 인덱스에 해당하는 Node를 가져옴
//		    .filter(node -> node.getNodeType() == Node.ELEMENT_NODE) // Node 타입 필터링
//		    .map(node -> (Element) node) // Node를 Element로 변환
//		    .forEach(element -> builder.append(element.getAttribute("id")).append("\t"));
//
//	        builder.append("\n");
//	        
//	        // 데이터 행 작성
//	        IntStream.range(0, nodeList.getLength())
//	            .mapToObj(nodeList::item)
//	            .filter(nNode -> nNode.getNodeType() == Node.ELEMENT_NODE)
//	            .forEach(nNode -> {
//	                Element eElement = (Element) nNode;
//	                NodeList childNodes = eElement.getChildNodes();
//
//	                IntStream.range(0, childNodes.getLength())
//	                    .mapToObj(childNodes::item)
//	                    .filter(childNode -> childNode.getNodeType() == Node.ELEMENT_NODE)
//	                    .forEach(childElement -> builder.append(childElement.getTextContent()).append("\t"));
//
//	                builder.append("\n");
//	            });
//	        
//	        writer.write(builder.toString());
//	        writer.flush();
//			System.out.println("XML to TSV conversion successful.");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}finally {
//			if(writer != null) {
//				writer.close();
//			}
//			UtilProfilingStack.pop(this.getClass().toString() + " : xmlToTsv");
//		}
	}
	
	
	
	@RequestMapping("/uploadExcel.do")
	public JSONDataView uploadExcel(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {

		File excelFile = getFileFromDataRequest(dataRequest);
		String colTypeExp = dataRequest.getParameter("columnTypes");
		String progressId = dataRequest.getParameter("progress");
		String[] columnTypes = colTypeExp != null ? colTypeExp.split("[ ,]+") : null;
		
		UtilProfilingStack.push("upload_excel_sax_read");
		
		if(excelFile == null) {
			ServletContext context = request.getServletContext();
			String realPath = context.getRealPath("ui\\data\\xml\\dsCovInspe.xlsx");
			excelFile = new File(realPath);
		}
		
		if (excelFile != null) {
			String cacheId = String.format("excel-%03d", CACHE_SEQ++);
			ProgressInputStream.setPlaceholder(progressId);
			PagedTSV pagedTSV = buildCache(cacheId, progressId, excelFile, columnTypes);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("cacheId", pagedTSV.fCacheId);
			data.put("rowCount", pagedTSV.getRowCount());
			data.put("readTime", UtilProfilingStack.getPop("upload_excel_sax_read"));
			
			// 엑셀의 캐시 아이디를 클라이언트에 보냄.
			dataRequest.setMetadata(true, data);
		}
//		UtilProfilingStack.pop("upload_excel_sax_read");
		
		return new JSONDataView();
	}
	
	
	@RequestMapping("/uploadTxt.do")
	public JSONDataView uploadTxt(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {

		File excelFile = getFileFromDataRequest(dataRequest);
		String colTypeExp = dataRequest.getParameter("columnTypes");
		String progressId = dataRequest.getParameter("progress");
		String dataDivCnt = dataRequest.getParameter("dataDivCnt");
		String[] columnTypes = colTypeExp != null ? colTypeExp.split("[ ,]+") : null;
		
		UtilProfilingStack.push("upload_excel_sax_read");
		
		if(excelFile == null) {
			ServletContext context = request.getServletContext();
			String realPath = context.getRealPath("ui\\app\\sce\\A4\\data\\A4-1_"+dataDivCnt+".tsv");
			
//			Resource resource = resourceLoader.getResource("classpath:/static/" + "ui\\app\\sce\\A4\\data\\A4-1_"+dataDivCnt+".tsv");
//			String realPath = resource.getFile().getAbsolutePath();
			excelFile = new File(realPath);
		}
		
		if (excelFile != null) {
			String cacheId = String.format("excel-%03d", CACHE_SEQ++);
			ProgressInputStream.setPlaceholder(progressId);
			PagedTSV pagedTSV = buildTxtCache(cacheId, progressId, excelFile, columnTypes);
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("cacheId", pagedTSV.fCacheId);
			data.put("rowCount", pagedTSV.getRowCount());
			data.put("readTime", UtilProfilingStack.getPop("upload_excel_sax_read"));
			
			// 엑셀의 캐시 아이디를 클라이언트에 보냄.
			dataRequest.setMetadata(true, data);
		}
//		UtilProfilingStack.pop("upload_excel_sax_read");
		
		return new JSONDataView();
	}
	
	
	
	@RequestMapping("/fileReadTsvFetch.do")
	public void fileReadTsvFetch(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		UtilProfilingStack.push(this.getClass().toString() + " : fileReadTsvFetch");
		response.setContentType("text/tab-separated-values");
		response.setCharacterEncoding("utf-8");
		
		ServletContext context = request.getServletContext();
		String realPath = context.getRealPath("ui\\app\\sce\\A1\\data\\A1-2_300000.tsv");
		File file = new File(realPath);
        
		new PagedTSVReader().pipeTo(file, response.getWriter());
		UtilProfilingStack.pop(this.getClass().toString() + " : fileReadTsvFetch");
		
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
	
	/**
	 * 주어진 엑셀 파일에 대한 페이징된 TSV 캐시를 만들고 캐시 아이디를 반환 합니다.
	 * 
	 * @param excelFile
	 *            엑셀 파일.
	 * @return 캐시 아이디.
	 * @throws IOException
	 */
	private PagedTSV buildCache(String cacheId, String progressId, File excelFile, String[] columnTypes)
			throws IOException {

		PagedTSV pagedTSV = new PagedTSV(cacheId, columnTypes, 5000);
		pagedTSV.purge();

		logger.info(MessageFormat.format("신규 캐시 작성: {0}", pagedTSV.getWorkDir().getAbsolutePath()));
		PagedTSVWriter tsvWriter = pagedTSV.openWriter();
		try {
			PagedTSVSheetHandler contentsHandler = new PagedTSVSheetHandler(tsvWriter, 1);
			parseExcelSheet(excelFile, progressId, contentsHandler);
			pagedTSV.setRowCount(tsvWriter.getLineCount());
			pagedTSV.saveManifest();

			logger.info(MessageFormat.format("캐시 작성 완료: {0}", cacheId));
			return pagedTSV;
		} finally {
			tsvWriter.close();
		}
	}
	
	private void parseExcelSheet(File excelFile, String progressId, SheetContentsHandler sheetHandler)
			throws IOException {
		OPCPackage opc = null;
		InputStream inputStream = null;

		try {

			// org.apache.poi.openxml4j.opc.OPCPackage
			opc = OPCPackage.open(excelFile);

			// org.apache.poi.xssf.eventusermodel.XSSFReader
			XSSFReader xssfReader = new XSSFReader(opc);

			// org.apache.poi.xssf.model.StylesTable
			StylesTable styles = xssfReader.getStylesTable();

			// org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
			ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);

			inputStream = xssfReader.getSheetsData().next();
			// 프로그래스바 표현을 위함. 성능 문제로 주석 처리
			inputStream = ProgressInputStream.wrapWithKey(inputStream, progressId);

			// org.xml.sax.InputSource
			InputSource inputSource = new InputSource(inputStream);

			// org.xml.sax.Contenthandler
			ContentHandler handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);

			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			saxParserFactory.setNamespaceAware(true);
			SAXParser parser = saxParserFactory.newSAXParser();
			XMLReader xmlReader = parser.getXMLReader();

			xmlReader.setContentHandler(handle);
			xmlReader.parse(inputSource);
			inputStream.close();

		} catch (Exception e) {

		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
			if (opc != null) {
				opc.close();
			}
		}
	}
	
	public PagedTSV parserXMLToTSV(String cacheId, String progressId, File xmlFile, String[] columnTypes) throws IOException {
        PagedTSV pagedTSV = new PagedTSV(cacheId, columnTypes, 5000);
        pagedTSV.purge();

        PagedTSVWriter tsvWriter = pagedTSV.openWriter();
        InputStream inputStream = null;
        
        try {
            PagedXMLToTSVHandler handler = new PagedXMLToTSVHandler(tsvWriter, 1); // 1 for header rows to ignore
            inputStream = new FileInputStream(xmlFile);
            inputStream = ProgressInputStream.wrapWithKey(inputStream, progressId);
            InputSource inputSource = new InputSource(inputStream);
        	// 프로그래스바 표현을 위함. 성능 문제로 주석 처리
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            xmlReader.setContentHandler(handler);

            xmlReader.parse(inputSource);

            pagedTSV.setRowCount(tsvWriter.getLineCount());
            pagedTSV.saveManifest();
            
            return pagedTSV;
        } catch (Exception e) {
            throw new IOException("Error processing XML file", e);
        } finally {
            tsvWriter.close();
        }
    }
	
	/**
	 * 주어진 엑셀 파일에 대한 페이징된 TSV 캐시를 만들고 캐시 아이디를 반환 합니다.
	 * 
	 * @param excelFile
	 *            엑셀 파일.
	 * @return 캐시 아이디.
	 * @throws IOException
	 */
	private PagedTSV buildTxtCache(String cacheId, String progressId, File excelFile, String[] columnTypes)
			throws IOException {

		PagedTSV pagedTSV = new PagedTSV(cacheId, columnTypes, 5000);
		pagedTSV.purge();
		
		
		logger.info(MessageFormat.format("신규 캐시 작성: {0}", pagedTSV.getWorkDir().getAbsolutePath()));
		PagedTSVWriter tsvWriter = pagedTSV.openWriter();
		try {
//			PagedTSVSheetHandler contentsHandler = new PagedTSVSheetHandler(tsvWriter, 1);
//			parseExcelSheet(excelFile, progressId, contentsHandler);
			parseTextFile(excelFile, 1, progressId, tsvWriter);
			
			pagedTSV.setRowCount(tsvWriter.getLineCount());
			pagedTSV.saveManifest();

			logger.info(MessageFormat.format("캐시 작성 완료: {0}", cacheId));
			return pagedTSV;
		} finally {
			tsvWriter.close();
		}
	}
	
	public void parseTextFile(File textFile, int headerLines, String progressId, PagedTSVWriter writer) throws IOException {
        BufferedReader reader = null;
        FileReader filereader = null;
        InputStream inputStream = null;
        try {
        	filereader = new FileReader(textFile);
            
            String line;
            int currentLine = 0;
            
            
            StringBuilder sb = new StringBuilder();
            inputStream = new FileInputStream(textFile);
            
            inputStream = ProgressInputStream.wrapWithKey(inputStream, progressId);
            
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = br.readLine()) != null) {
            	  // Skip header lines
                if (currentLine < headerLines) {
                    currentLine++;
                    continue;
                }

                // Split the line into columns (assuming TSV format, using "\t" as delimiter)
                String[] columns = line.split("\t");

                // Begin new line in writer
                writer.beginNewLine();
                for (String value : columns) {
                    writer.writCellValue(value);
                }

                currentLine++;
            }
//            while ((line = reader.readLine()) != null) {
//                // Skip header lines
//                if (currentLine < headerLines) {
//                    currentLine++;
//                    continue;
//                }
//
//                // Split the line into columns (assuming TSV format, using "\t" as delimiter)
//                String[] columns = line.split("\t");
//
//                // Begin new line in writer
//                writer.beginNewLine();
//                for (String value : columns) {
//                    writer.writCellValue(value);
//                }
//
//                currentLine++;
//            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading the text file", e);
        } finally {
            if (reader != null) {
                reader.close();
            }
            
            if(inputStream != null) {
            	inputStream.close();
            }
        }
    }
	
	public LinkedHashMap setHeader() {
		LinkedHashMap mapHeader = new LinkedHashMap();
		mapHeader.put("column1", "고객번호");
		mapHeader.put("column2", "발신전화번호");
		mapHeader.put("column3", "안내메시지 제목");
		mapHeader.put("column4", "고객안내 메시지");
		mapHeader.put("column5", "수신인휴대폰번호");
		mapHeader.put("column6", "안내코드");
		mapHeader.put("column7", "설계사명");
		mapHeader.put("column8", "안내일자");
		mapHeader.put("column9", "안내시간");
		mapHeader.put("column10", "안내채널");
		mapHeader.put("column11", "담당사원번호");
		mapHeader.put("column12", "담당전화번호");
		mapHeader.put("column13", "담당메시지 제목");
		mapHeader.put("column14", "담당자안내 메시지");
		mapHeader.put("column15", "담당자휴대폰번호");
		mapHeader.put("column16", "담당코드");
		mapHeader.put("column17", "담당자명");
		mapHeader.put("column18", "전달일자");
		mapHeader.put("column19", "전달시간");
		mapHeader.put("column20", "전달채널");
		mapHeader.put("column21", "업로드 일시");
		mapHeader.put("column22", "처리자명");
		return mapHeader;
	}
	
	
	@RequestMapping("/export-excelExport.do")
    public void exportExcelData(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Map<String, Object> requestData) throws IOException {

//        List<Map<String, Object>> data = (List<Map<String, Object>>) requestData.get("data");
    	List<List<Object>> dataList = (List<List<Object>>) requestData.get("data");
//        boolean isLastSend = (Boolean) requestData.get("isLastSend");
//        
//        List<List<Object>> headers = new ArrayList<>();        		
//
//        // 첫 번째 요청에서만 초기화
//        if (adapter == null || requestData.get("isFirstSend").equals(true)) {
//            adapter = new StreamExcelResponseAdapter("export", requestData);
//           
//        }
//
//        // 첫 번째 요청에서만 헤더 설정
//        if (!isHeaderSet && requestData.containsKey("headers") && requestData.containsKey("columnWidths")) {
//            headers = adapter.getHeaders2();
//
//            adapter.addHeader2(headers);
//            isHeaderSet = true;
//        }
//        // 데이터를 엑셀에 추가
//        for (List<Object> row : dataList) {
//            adapter.addRow3(row);
//        }
//
//        // 마지막 요청이면 클라이언트에 데이터 전달
//        if (isLastSend) {
////            System.out.println("마지막 요청 끝, 엑셀 다운로드 실행2");
//            adapter.Response2(response);
//            isHeaderSet = false; 
//            adapter = null;
//        }
    }
	
	@RequestMapping("/export-csvExport.do")
    public void exportCsvData(HttpServletRequest request, HttpServletResponse response,
            @RequestBody Map<String, Object> requestData) throws IOException {

//        List<Map<String, Object>> data = (List<Map<String, Object>>) requestData.get("data");
    	List<List<Object>> dataList = (List<List<Object>>) requestData.get("data");
        boolean isLastSend = (Boolean) requestData.get("isLastSend");
        
        List<List<Object>> headers = new ArrayList<>();        		

        // 첫 번째 요청에서만 초기화
        if (csvAdapter == null || requestData.get("isFirstSend").equals(true)) {
        	csvAdapter = new StreamCsvResponseAdapter(requestData);
           
        }

        // 첫 번째 요청에서만 헤더 설정
        if (!isHeaderSet && requestData.containsKey("headers") && requestData.containsKey("columnWidths")) {
            headers = csvAdapter.getHeaders2();
            csvAdapter.addHeader2(headers);
            isHeaderSet = true;
        }
        // 데이터를 엑셀에 추가
        for (List<Object> row : dataList) {
        	csvAdapter.addRow2(row);
        }

        // 마지막 요청이면 클라이언트에 데이터 전달
        if (isLastSend) {
//            System.out.println("마지막 요청 끝, csv 다운로드 실행");
            csvAdapter.endResponse2(response);
            isHeaderSet = false; 
            csvAdapter = null;
        }
    }
	
	
	
}
