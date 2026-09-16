package com.tomatosystem.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.binary.XSSFBSheetHandler.SheetContentsHandler;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.json.simple.JSONObject;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataResponse;

public class ExcelImportSheetHandler implements SheetContentsHandler {

    private int currentCol = -1;
    private int currRowNum = 0;
    
    // 재사용을 위한 필드 (매번 생성 방지)
    private final Map<String, Object> rowMap = new HashMap<>();
    private String[] headerNames; // 인덱스 기반의 헤더 배열
    
    private static DataResponse dataResponse = null;
    private static JSONObject colInfo = null;
    
    public static ExcelImportSheetHandler readExcel(File file, JSONObject poColInfo, HttpServletResponse response) throws Exception {
        ExcelImportSheetHandler sheetHandler = new ExcelImportSheetHandler();
        
        // 정적 변수 할당 최적화
        dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
        colInfo = poColInfo;
        
        try (OPCPackage opc = OPCPackage.open(file);
             InputStream inputStream = new XSSFReader(opc).getSheetsData().next()) {
            
            XSSFReader xssfReader = new XSSFReader(opc);
            StylesTable styles = xssfReader.getStylesTable();
            ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(opc);
            
            ContentHandler handle = new XSSFSheetXMLHandler(styles, strings, sheetHandler, false);
            
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            saxParserFactory.setNamespaceAware(true);
            XMLReader xmlReader = saxParserFactory.newSAXParser().getXMLReader();
            
            xmlReader.setContentHandler(handle);
            xmlReader.parse(new InputSource(inputStream));
            
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (dataResponse != null) dataResponse.finish();
        }
        return sheetHandler;
    }

    @Override
    public void startRow(int rowNum) {
        this.currentCol = -1;
        this.currRowNum = rowNum;
        this.rowMap.clear(); // 기존 맵 재사용 (객체 생성 비용 절감)
    }

    @Override
    public void cell(String columnName, String value, XSSFComment var3) {
        // CellReference 객체 생성을 피하기 위해 직접 인덱스 계산 (성능 향상)
        int iCol = 0;
        for (int i = 0; i < columnName.length(); i++) {
            char c = columnName.charAt(i);
            if (Character.isDigit(c)) break;
            iCol = iCol * 26 + (c - 'A' + 1);
        }
        iCol -= 1; // 0-based index
        
        this.currentCol = iCol;

        // 헤더 행이 아니고, 헤더 매핑 배열이 존재할 때만 데이터 담기
        if (currRowNum > 0 && headerNames != null && iCol < headerNames.length) {
            String key = headerNames[iCol];
            if (key != null) {
                rowMap.put(key, value);
            }
        } else if (currRowNum == 0) {
            // 헤더 행일 때는 임시로 리스트나 맵에 저장하지 않고 endRow에서 처리하거나
            // 임시 저장소(예: 임시 배열)를 활용
            processHeaderCell(iCol, value);
        }
    }

    // 헤더 정보를 임시로 담을 리스트 (첫 줄만 사용)
    private List<String> tempHeaderList = new ArrayList<>();

    private void processHeaderCell(int col, String value) {
        // JSON 설정 정보와 대조하여 헤더 위치 파악
        String matchedKey = null;
        for (Object keyObj : colInfo.keySet()) {
            String key = (String) keyObj;
            
        	if (value.equals(colInfo.get(key))) {
        		matchedKey = key;
        		break;
        	}
        }
        
        // 리스트 크기 확보 후 저장
        while (tempHeaderList.size() <= col) tempHeaderList.add(null);
        tempHeaderList.set(col, matchedKey);
    }

    @Override
    public void endRow(int rowNum) {
        if (rowNum == 0) {
            // 헤더 배열 확정 (이후 로직에서 빠른 인덱스 접근을 위함)
            headerNames = tempHeaderList.toArray(new String[0]);
            tempHeaderList = null; // GC 대상
        } else {
            if (!rowMap.isEmpty()) {
                try {
                    dataResponse.send(rowMap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override public void headerFooter(String arg0, boolean arg1, String arg2) {}
    @Override public void hyperlinkCell(String arg0, String arg1, String arg2, String arg3, XSSFComment arg4) {}
}