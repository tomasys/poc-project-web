package com.tomatosystem.core.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.DataResponse;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.service.ExcelRowHandleService;
import com.tomatosystem.core.vo.ExcelVO;



/**
 * eXCampus Framework 1.0
 * 
 * 클래스: 대용량 엑셀 파일을 SAX 방식으로 Cell 별로 읽어서 처리하는 클래스이다.
 * 
 * 제약사항 : 첫번째 Cell의 값이 비어있는 경우에는 무시(SKIP) 처리된다.
 *          최대 1,000건씩 RowHandlerService에게 처리 요청을 보낸다.
 */
public class ExcelSheetHandler implements SheetContentsHandler {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private int currentCol = -1;
    private int currRowNum =  0;
    private int colIndex = 0;
    private int startDataRowIndex = -1;
    private boolean isValidRow = false;
    private ExcelRowHandleService rowHandler = null;
    private Map requestParamMap = null;
    private HttpServletResponse response = null;
    private static DataResponse dataResponse = null;
    private DataRequest dataRequest;
    static JSONObject colInfo = new JSONObject();
    
    private List<String> header = new ArrayList<String>();
    
 // ExcelVO와 Map<String, Object>를 동시에 처리할 수 있도록 제네릭 사용
    private List<Object> rowsList = new ArrayList<>(); 
    private Object row;  // ExcelVO 또는 Map<String, Object> 사용 가능
    
    private Map<String, Object> rowMap = new HashMap<String, Object>();
    
    public ExcelSheetHandler(int startDataRowIndex, ExcelRowHandleService handleService, Map requestParamMap, HttpServletResponse response ,DataRequest dataRequest ) throws ParseException{
    	this.startDataRowIndex = startDataRowIndex;
    	this.rowHandler = handleService;
    	this.requestParamMap = requestParamMap;
    	this.response = response;
    	this.dataRequest = dataRequest;
    	
    	dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, this.response);
    	String strColumnInfo = dataRequest.getParameter("columnInfo");
		JSONObject columnJSON = null;
		if(!"".equals(strColumnInfo) && strColumnInfo != null) {
			JSONParser parser = new JSONParser();
			columnJSON = (JSONObject) parser.parse(strColumnInfo);
		}
    	colInfo = columnJSON;
    	
    }

	public ExcelSheetHandler() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void startRow(int rowNum) {
		this.currentCol = 0;
        this.currRowNum = rowNum;
        this.colIndex = 0;
        isValidRow = false;
        
        
		if (shouldUseExcelVO()) {
			if(rowNum < startDataRowIndex) {
				row = new ArrayList<String>(); 
			}else {
				row = new ExcelVO(); 								
			}
        } else {
        	row = new ArrayList<String>(); 
        }
	}

	@Override
	public void endRow(int rowNum) {    
	    try {
	        if (rowNum < startDataRowIndex) {
	            header = new ArrayList<>((List<String>) row);
	        } else {
	            // 빈 값 채우기: 헤더 개수가 데이터 개수보다 많을 경우
	            if (row instanceof List) {
	                List<String> rowData = (List<String>) row;
	                while (rowData.size() < header.size()) {
	                    rowData.add(""); // 부족한 컬럼 개수만큼 빈 문자열 추가
	                }
	            }

	            if (shouldUseExcelVO()) {
	                rowsList.add(row);
	            } else {
	                rowsList.add(new HashMap<>(rowMap));
	            }

	            if (rowsList.size() % 1000 == 0) {
	                try {
	                    rowHandler.handle(this.requestParamMap, rowsList, dataResponse);
	                    rowsList.clear();
	                } catch (RuntimeException e) {
	                    logger.debug(e.getMessage());
	                } catch (Exception e) {
	                    logger.debug(e.getMessage());
	                }
	            }
	        }
	    } catch (Exception e) {
	    	 throw new AppWorksException("error");
	    }
	}
	
	
	
	@Override
	public void cell(String columnName, String value, XSSFComment comment) {
		
		int iCol = (new CellReference(columnName)).getCol();
        currentCol = iCol;
        try {
        	if (header.size() > startDataRowIndex ) {
        		String headerName = getHeaderName(columnName);
        		 if (colInfo.containsKey(headerName)) {
        			 if (row instanceof ArrayList) {
             			rowMap.put(colInfo.get(header.get(iCol)).toString(), value);
             		} else if (row instanceof ExcelVO) {
             			// ExcelVO에 맞게 데이터 입력 (ExcelVO의 setter 사용 필요)
             			ExcelVOUtil.setCellValue((ExcelVO) row, this.colIndex, (value == null ? "" : value));
             			this.colIndex++;
//             			rowMap.put(colInfo.get(header.get(iCol)).toString(), value);
             		}
        		 }
        	} else {
        		//if (colInfo.containsKey(value)) {
                    ((List<String>) row).add(value);
                //}
//        		((List<String>) row).add(value);
        	}        	
        }catch (Exception e) {
			// TODO: handle exception
        	 throw new AppWorksException("error");
		}
        if (iCol == 0) {
            isValidRow = true;
        }	
	}
	
	/**
	 * 엑셀의 컬럼명을 서버에서 보낸 헤더(`colInfo`)의 키값으로 변환
	 * 예: "A" -> "No", "B" -> "column1", "C" -> "column2", ...
	 */
	private String getHeaderName(String columnName) {
	    int iCol = (new CellReference(columnName)).getCol();
	    if (iCol < header.size()) {
	        return header.get(iCol); // 엑셀의 iCol 위치에 맞는 서버 헤더명 반환
	    }
	    return null; // 잘못된 컬럼이면 null 반환
	}

	
	@Override
	public void headerFooter(String arg0, boolean arg1, String arg2) {
		//미사용
	}
	
	public void flush(){
		if(rowsList.size() > 0){
			try {
				rowHandler.handle(this.requestParamMap, rowsList,dataResponse);
				rowsList.clear();
			} catch (RuntimeException e) {
				logger.debug(e.getMessage());
				throw new AppWorksException("error");
			} catch (Exception e) {
				logger.debug(e.getMessage());
			}
		}
	}
	
	private boolean shouldUseExcelVO() {
        // 특정 조건에 따라 ExcelVO 또는 Map<String, Object>를 선택하도록 구현
		if(requestParamMap != null) {
			return requestParamMap.containsKey("useExcelVO") && (boolean) requestParamMap.get("useExcelVO");
		}else {
			return false;
		}
        
    }
	
	//Cell 인덱스 반환
	private int getCellIndex(String columnName){
		columnName = columnName.replaceAll("[0-9]", ""); 
		
		int colNameLen = columnName.length();
		return ((colNameLen - 1) * 10) + cellPositionToIndex(columnName.charAt(colNameLen -1));
	}
	
	private int cellPositionToIndex(char cellRef){
		switch (cellRef) {
			case 'A':
				return 0;
			case 'B':
				return 1;
			case 'C':
				return 2;
			case 'D':
				return 3;
			case 'E':
				return 4;
			case 'F':
				return 5;
			case 'G':
				return 6;
			case 'H':
				return 7;
			case 'I':
				return 8;
			case 'J':
				return 9;
			case 'K':
				return 10;
			case 'L':
				return 11;
			case 'M':
				return 12;
			case 'N':
				return 13;
			case 'O':
				return 14;
			case 'P':
				return 15;
			case 'Q':
				return 16;
			case 'R':
				return 17;
			case 'S':
				return 18;
			case 'T':
				return 19;
			case 'U':
				return 20;
			case 'V':
				return 21;
			case 'W':
				return 22;
			case 'X':
				return 23;
			case 'Y':
				return 24;
			case 'Z':
				return 25;
			default:
				return -1;
		}
	}
	
	public static ExcelSheetHandler readExcel(File file, JSONObject poColInfo, HttpServletResponse response) throws Exception{
		 
        ExcelSheetHandler sheetHandler = new ExcelSheetHandler();
        
        dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
//        dataResponse.enableCompression("gzip");
        colInfo = poColInfo;
        
        OPCPackage opc = null;
		InputStream inputStream = null;
		
        try{
            
            //org.apache.poi.openxml4j.opc.OPCPackage
            opc = OPCPackage.open(file);
 
            //org.apache.poi.xssf.eventusermodel.XSSFReader
            XSSFReader xssfReader    = new XSSFReader(opc);
 
            //org.apache.poi.xssf.model.StylesTable
            StylesTable styles        = xssfReader.getStylesTable();
 
            //org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
            ReadOnlySharedStringsTable strings    = new ReadOnlySharedStringsTable(opc);
            
            inputStream    = xssfReader.getSheetsData().next();    
 
            //org.xml.sax.InputSource
            InputSource    inputSource = new InputSource(inputStream);
 
            //org.xml.sax.Contenthandler
            ContentHandler handle    = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);
            
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            SAXParser parser    = saxParserFactory.newSAXParser();
            XMLReader xmlReader = parser.getXMLReader();
            
            
            xmlReader.setContentHandler(handle);
            xmlReader.parse(inputSource);
            inputStream.close();
            
            
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
			if(inputStream != null){
				inputStream.close();
			}
			if(opc != null){
				opc.close();
			}
			dataResponse.finish();
		}
        return sheetHandler;
 
    }//readExcel - end
}
