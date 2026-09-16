package com.tomatosystem.core.massive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomatosystem.core.util.DateUtil;
import com.tomatosystem.core.util.HttpWebUtil;

public class StreamCsvResponseAdapter {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private HttpServletResponse response;
	private Workbook workbook;
	private CellStyle numberCellStyle;
	private CellStyle numberCellStyle2;
	private int rowNum = 1;
	private OutputStream out;
	private String[] header = new String[]{};

	private PrintWriter out1 = null;
	private String csvText ;
	private List<List<Object>> headers;	 
	private List<Map<String,Object>> headers2;	 
	private File tempFile;
    private FileOutputStream fileOutputStream;
    private StringBuilder csvBuilder;

	
	public StreamCsvResponseAdapter(HttpServletRequest request, HttpServletResponse response, String strFileName) throws IOException{
		this.response = response;
		String strFileNm = strFileName;
		strFileNm = HttpWebUtil.getUrlEncodedFileName(request, strFileNm);
		
		String resCharset = request.getHeader("res-charset");
		if ((resCharset == null) || (resCharset.equalsIgnoreCase(""))) {
			resCharset = "UTF-8";
		}
		this.out = response.getOutputStream();
		this.out1 = new PrintWriter(new OutputStreamWriter(out, "MS949"));
		csvText = "";
		response.setContentType("text/csv;charset=utf-8");
		response.setHeader("Content-Disposition", "attachment; filename=\"" + strFileNm + ".csv" + "\"");

	}
	
	public StreamCsvResponseAdapter(Map<String, Object> requestData) throws IOException {
	    logger.info("Creating temporary CSV file");
	    this.headers = (List<List<Object>>) requestData.get("headers");
	    this.tempFile = File.createTempFile("export-", ".csv");
	    this.fileOutputStream = new FileOutputStream(tempFile); // append 모드 new FileOutputStream(tempFile, true);
	    // UTF-8 BOM 추가
	    this.fileOutputStream.write(new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF});
	    this.out1 = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.fileOutputStream, StandardCharsets.UTF_8)));
	    this.csvBuilder = new StringBuilder();
	}

	
	/**
	 * 헤더 추가
	 * @param mapHeader
	 */
	public void addHeader(LinkedHashMap<String, Object> mapHeader){
		//셀 스타일
		
		//헤더(Header) 출력
		Object key;
		int idx = 0;
		Iterator<String> iterHeader = mapHeader.keySet().iterator();
		header = new String[mapHeader.keySet().size()];
		while (iterHeader.hasNext()) {
			key = iterHeader.next();
			
			header[idx] = (String)key;
			csvText += mapHeader.get(key) + ",";
			idx++;
		}
		csvText += "\n";
		out1.write(csvText);
		csvText = "";
	}	 
	
	public void endClose() {
		try {
			if (this.out1 != null) {
				this.out1.close();
				this.out1 = null;
			}
		}catch (Exception e) {
			// TODO: handle exception
			logger.debug(e.getMessage());
		}
	}
	
	/**
	 * Row 추가
	 * @param data
	 */
	public void addRow(Object data){
		if(!(data instanceof Map)) return;
		
		Map mapData = (Map)data;
		Object value = null;
		double dvalue;
		for(int i=0, len=header.length; i<len; i++){
			value = mapData.get(header[i]);			
			if(value instanceof Integer){
				csvText += ((Integer)value).intValue() + ",";
			}else if(value instanceof BigDecimal){
				dvalue = ((BigDecimal)value).doubleValue();
				csvText += DateUtil.dateToString(dvalue) +",";
							}else if (value instanceof java.sql.Timestamp) {
				csvText += DateUtil.dateToString(value) +",";
			}else if(value == null || value.equals("")) {
				csvText += "" + ",";
			}							
			else{
				csvText += (String)value + ",";
			}			
		}
		csvText += "\n";
		out1.write(csvText);
		csvText = "";
	}
	
	public void addRow(Object data, String progressId){
		if(!(data instanceof Map)) return;
		
		Map mapData = (Map)data;
		Object value = null;
		double dvalue;
		for(int i=0, len=header.length; i<len; i++){
			value = mapData.get(header[i]);			
			if(value instanceof Integer){
				csvText += ((Integer)value).intValue() + ",";
			}else if(value instanceof BigDecimal){
				dvalue = ((BigDecimal)value).doubleValue();
				csvText += DateUtil.dateToString(dvalue) +",";
							}else if (value instanceof java.sql.Timestamp) {
				csvText += DateUtil.dateToString(value) +",";
			}else if(value == null || value.equals("")) {
				csvText += "" + ",";
			}							
			else{
				csvText += (String)value + ",";
			}			
		}
		csvText += "\n";
		ProgressExcelStore.progressMap.put(progressId, this.rowNum);
		this.rowNum++;
		out1.write(csvText);
		csvText = "";
	}
	
	public void addHeader2(List<List<Object>> headerList) {
	    for (List<Object> headers : headerList) {
	        for (int i = 0; i < headers.size(); i++) {
	            csvBuilder.append(escapeCsv(headers.get(i)));
	            if (i < headers.size() - 1) {
	                csvBuilder.append(",");
	            }
	        }
	        csvBuilder.append("\n");
	    }
	    out1.write(csvBuilder.toString());
	    csvBuilder.setLength(0);
	}
	
	public void addRow2(List<Object> row) {
		for (int i = 0; i < row.size(); i++) {
		    csvBuilder.append(escapeCsv(row.get(i))); // 필드 이스케이프
		    if (i < row.size() - 1) {
		        csvBuilder.append(",");
		    }
		}
		csvBuilder.append(System.lineSeparator()); // 

		out1.write(csvBuilder.toString());
		csvBuilder.setLength(0); // 초기화
	}

	
	public void endResponse2(HttpServletResponse response) {
	    try {
	        out1.flush();
	        out1.close();

	        response.setContentType("text/csv;charset=UTF-8");
	        response.setHeader("Content-Disposition", "attachment; filename=\"export.csv\"");
	        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
	        response.setHeader("Pragma", "no-cache");
	        response.setHeader("Expires", "0");

	        OutputStream out = response.getOutputStream();
	        try (FileInputStream fis = new FileInputStream(tempFile)) {
	            byte[] buffer = new byte[1024];
	            int bytesRead;
	            while ((bytesRead = fis.read(buffer)) != -1) {
	                out.write(buffer, 0, bytesRead);
	            }
	        }
	        out.flush();
	    } catch (IOException e) {
	    } finally {
	        if (tempFile.exists()) {
	            tempFile.delete();
	        }
	    }
	}

	
	 public List<List<Object>> getHeaders2() {
	        return headers;
	    }
	
	 private String escapeCsv(Object value) {
	        if (value == null) {
	            return "";
	        }
	        String str = value.toString();
	        // 이스케이프 문자 (\n)를 실제 줄바꿈으로 변환
	        str = str.replace("\\n", "\n");

	        // 쉼표, 따옴표, 줄바꿈이 있는 경우 이스케이프 처리
	        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
	            str = str.replace("\"", "\"\""); // 따옴표 이스케이프
	            return "\"" + str + "\""; // 필드를 따옴표로 감싸기
	        }
	        return str;
	    }

}
