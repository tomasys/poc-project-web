package com.tomatosystem.core.massive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.UploadFile;
import com.tomatosystem.core.service.ExcelRowHandleService;
import com.tomatosystem.core.util.ExcelSheetHandler;



/**
 * 클래스: 대용량 엑셀파일을 SAX 방식으로 읽어들이는 클래스이다. 
 * 예시)
 * MassiveExcelReader reader = new MassiveExcelReader(1, (ExcelRowHandleService)cmnExcelImportTmpService, mapParam);
   reader.readExcel(requestData);
 */
public class MassiveExcelReader {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private int startRowIndex = -1;
	private ExcelRowHandleService rowHandler = null;
	private Map requestParamMap = null;
	private HttpServletResponse response = null;

	private DataRequest dataRequest= null;
	
	public MassiveExcelReader(int startRowIndex, ExcelRowHandleService handleService, Map paramMap){
		this.startRowIndex = startRowIndex;
		this.rowHandler = handleService;
		this.requestParamMap = paramMap;
	}
	
	public MassiveExcelReader(int startRowIndex, ExcelRowHandleService handleService, Map paramMap,HttpServletResponse response, DataRequest dataRequest ){
		this.startRowIndex = startRowIndex;
		this.rowHandler = handleService;
		this.requestParamMap = paramMap;
		this.response = response;
		this.dataRequest = dataRequest;
	}
	
	public void readExcel(DataRequest requestData) throws Exception {
		File file = null;
		Map<String, UploadFile[]> uploadFiles = requestData.getUploadFiles();
		if(uploadFiles != null && uploadFiles.size() > 0) {
			Set<Entry<String, UploadFile[]>> entries = uploadFiles.entrySet();
			for(Entry<String, UploadFile[]> entry : entries) {
				UploadFile[] uFiles = entry.getValue();
				if(uFiles.length > 0){
					file = uFiles[0].getFile();
					break;
				}
			}
		}
		if(file == null) return;
		
		OPCPackage opc = null;
		InputStream inputStream = null;
		
		ExcelSheetHandler sheetHandler = new ExcelSheetHandler(this.startRowIndex, this.rowHandler, this.requestParamMap,this.response , this.dataRequest);
		
		
		try {
            opc = OPCPackage.open(file);
 
            XSSFReader xssfReader = new XSSFReader(opc);
            StylesTable styles = xssfReader.getStylesTable();
 
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
            
            //엑셀의 시트를 하나만 가져오기입니다.
            //여러개일경우 while문으로 추출하셔야 됩니다.
            inputStream = xssfReader.getSheetsData().next();    
            InputSource inputSource = new InputSource(inputStream);
 
            //org.xml.sax.Contenthandler
            ContentHandler handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);
            
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            SAXParser parser    = saxParserFactory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            
//            XMLReader xmlReader = SAXHelper.newXMLReader();
            xmlReader.setContentHandler(handle);
 
            xmlReader.parse(inputSource);
            inputStream.close();
            
            sheetHandler.flush();
		} catch (IOException e){
			logger.debug(e.getMessage());
		} catch (Exception e){
			logger.debug(e.getMessage());
		} finally {
			if(inputStream != null){
				inputStream.close();
			}
			if(opc != null){
				opc.close();
			}
		}
	}
}
