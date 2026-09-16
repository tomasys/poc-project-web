package com.tomatosystem.core.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.util.StreamUtils;

import com.cleopatra.protocol.data.DataRequest;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.resource.AppProperties;


public class UtilExcel {

	private static final Logger logger = LogManager.getLogger(UtilExcel.class);
	
	


   /**
    * 엑셀파일을 생성하여 브라우져에 전송
    * Method Name : createExcelSendBrowser<BR/>
    * Description :	eXria res protocol 미지원으로 사용불가	  <BR/>	
    *
    * @author      : Park. ju wan <BR/>
    * History <BR/>     
    * 2013. 10. 7. Park. ju wan 최초작성 <BR/>
    *
    * @param req		HttpServletRequest
    * @param resp		HttpServletResponse
    * @param strFileNm  생성할 파일명
    * @param mapHeader	엑셀 헤더 정보
    * @param listData	엑셀 로우 데이터
    * @param reqData	DataRequest
    * @throws Exception
    */
	public static void createExcelSendBrowser(HttpServletRequest req,
			HttpServletResponse resp, String strFileNm, String strSheetNm, 
			LinkedHashMap mapHeader, List listData, DataRequest reqData,
			CellStyle cellHeaderStyle,	CellStyle cellRowStyle)
			throws Exception {

		Workbook wb = UtilExcel.makeWorkBookToList(mapHeader, listData,
				strFileNm, strSheetNm, null, null);

		OutputStream out = new ByteArrayOutputStream();
		wb.write(out);
		out.close();

		resp.setContentType("application/octet-stream");
		resp.setHeader("Content-Transfer-Encoding", "binary");
		resp.setHeader("Content-Disposition", "attachment;fileName=\""
				+ URLEncoder.encode(strFileNm, "UTF-8") + "\"");
		resp.setHeader("Pragma", "no-cache;");
		resp.setHeader("Expires", "-1;");
		
		
		ByteArrayInputStream bis = new ByteArrayInputStream(
				((ByteArrayOutputStream) out).toByteArray());
		OutputStream outResp = resp.getOutputStream();

		try {
			StreamUtils.copy(bis, outResp);
		} catch (IOException e){
			logger.debug(e.getMessage());
		} finally {
			if( outResp != null) {
				outResp.close();
			}
//			outResp.flush();
			if (bis != null) {
				bis.close();
			}
		}
	}
    
	/**
	 * 
	 * Method Name : addSheetByWorkbook<BR/>
	 * Description : 생성된 Workbook에 Sheet 추가
	 * ex) Workbook wbResult = UtilExcel.makeWorkBookToList(title, listExcelResultError,
				strFileNameLog, "에러목록",  null, null);
		   UtilExcel.addSheetByWorkbook(wbResult, title, listExcelResultLog, "로그목록", null, null);		  <BR/>	
	 * @author      : Park. ju wan <BR/>
	 * History <BR/>     
	 * 2014. 1. 10. Park. ju wan 최초작성 <BR/>
	 *
	 * @param wb
	 * @param mapHeader
	 * @param listData
	 * @param strSheetNm
	 * @param cellHeaderStyle
	 * @param cellRowStyle
	 * @return
	 * @throws AppWorksException
	 */
	public static Workbook addSheetByWorkbook(Workbook wb, LinkedHashMap mapHeader,
			List listData, String strSheetNm, CellStyle cellHeaderStyle,
			CellStyle cellRowStyle) throws AppWorksException {


			Sheet s = wb.createSheet(strSheetNm);
			if(s == null) return wb;
			if(cellHeaderStyle == null){
				cellHeaderStyle = wb.createCellStyle();
				cellHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
				cellHeaderStyle.setWrapText(true);
				cellHeaderStyle.setBorderRight(BorderStyle.THIN);
				cellHeaderStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setBorderLeft(BorderStyle.THIN);
				cellHeaderStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setBorderTop(BorderStyle.THIN);
				cellHeaderStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setBorderBottom(BorderStyle.THIN);
				cellHeaderStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); 
				cellHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			}

			int rownum = 0;
			Row rowHeader = s.createRow(rownum++);
			
			if(listData.size()  < 1){
				Iterator iterHeader = mapHeader.keySet().iterator();
				int iHeaderRow = 0;
				while (iterHeader.hasNext()) {
					String strHeaderkey = (String) iterHeader.next();
					Cell cellHeader = rowHeader
							.createCell(iHeaderRow++);
					cellHeader.setCellValue((String) mapHeader
							.get(strHeaderkey));
					cellHeader.setCellStyle(cellHeaderStyle);
				}
			}
			for (int j = 0; j < listData.size(); j++) {

				Row row = s.createRow(j + 1);

				Map mapData = (Map) listData.get(j);

				int iRow = 0;
				int iHeaderRow = 0;

				Iterator iterHeader = mapHeader.keySet().iterator();

				while (iterHeader.hasNext()) {
					String strHeaderkey = (String) iterHeader.next();

					Iterator iterator = mapData.keySet().iterator();

					while (iterator.hasNext()) {

						if (strHeaderkey.equals((String) iterator.next())) {
							// iRow++;

							if (j == 0) {
								Cell cellHeader = rowHeader
										.createCell(iHeaderRow++);
								cellHeader.setCellValue((String) mapHeader
										.get(strHeaderkey));
								cellHeader.setCellStyle(cellHeaderStyle);
							}
							Cell cell = row.createCell(iRow++);
							cell.setCellValue((String) mapData
									.get(strHeaderkey));
						}
					}

				}

			}
		return wb;
	}
    
   /**
    * 
    * Method Name : makeWorkBookToList<BR/>
    * Description :	엑셀 Workbook 생성	  <BR/>	
    *
    * @author      : Park. ju wan <BR/>
    * History <BR/>     
    * 2014. 1. 10. Park. ju wan 최초작성 <BR/>
    *
    * @param mapHeader
    * @param listData
    * @param strFileNm
    * @param strSheetNm
    * @param cellHeaderStyle
    * @param cellRowStyle
    * @return
    * @throws AppWorksException
    */
	public static Workbook makeWorkBookToList(LinkedHashMap mapHeader,
			List listData, String strFileNm, String strSheetNm, CellStyle cellHeaderStyle,
			CellStyle cellRowStyle) throws AppWorksException {

		Workbook wb = null;
		int indexDot = strFileNm.lastIndexOf(".");
		if (indexDot == -1)
			indexDot = 0;
		String fileExtention = strFileNm.substring(indexDot).toLowerCase();
		if (".xlsx".equals(fileExtention)) {
			wb = new XSSFWorkbook();
		} else {
			wb = new HSSFWorkbook();
		}
			Sheet s = null;
			if(StringUtil.isNotNullEmpty(strSheetNm)) s = wb.createSheet(strSheetNm);
			else s = wb.createSheet();
			// declare a row object reference
			// Row r = null;
			// declare a cell object reference
			// Cell c = null;
			// create 2 cell styles
//			CellStyle cs = wb.createCellStyle();
//			CellStyle cs2 = wb.createCellStyle();
			if(cellHeaderStyle == null){
				
				cellHeaderStyle = wb.createCellStyle();
				cellHeaderStyle.setAlignment(HorizontalAlignment.CENTER);
				cellHeaderStyle.setWrapText(true);
				cellHeaderStyle.setBorderRight(BorderStyle.THIN);
				cellHeaderStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setBorderLeft(BorderStyle.THIN);
				cellHeaderStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setBorderTop(BorderStyle.THIN);
				cellHeaderStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setBorderBottom(BorderStyle.THIN);
				cellHeaderStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
				cellHeaderStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); 
				cellHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				
			}
//			DataFormat df = wb.createDataFormat();
//			// create 2 fonts objects
//			Font f = wb.createFont();
//			Font f2 = wb.createFont();
//			// Set font 1 to 12 point type, blue and bold
//			f.setFontHeightInPoints((short) 12);
//			f.setColor(IndexedColors.RED.getIndex());
//			f.setBoldweight(Font.BOLDWEIGHT_BOLD);
//			// Set font 2 to 10 point type, red and bold
//			f2.setFontHeightInPoints((short) 10);
//			f2.setColor(IndexedColors.RED.getIndex());
//			f2.setBoldweight(Font.BOLDWEIGHT_BOLD);
			// Set cell style and formatting
//			cs.setFont(f);
//			cs.setDataFormat(df.getFormat("#,##0.0"));
//			// Set the other cell style and formatting
//			cs2.setBorderBottom(CellStyle.BORDER_THIN);
//			cs2.setDataFormat(df.getFormat("text"));
//			cs2.setFont(f2);
			
			if( s == null) return wb;
			s.setDefaultColumnWidth(15);
			int rownum = 0;
			Row rowHeader = s.createRow(rownum++);
			
			if(listData.size()  < 1){
				Iterator iterHeader = mapHeader.keySet().iterator();
				int iHeaderRow = 0;
				while (iterHeader.hasNext()) {
					String strHeaderkey = (String) iterHeader.next();
					Cell cellHeader = rowHeader
							.createCell(iHeaderRow++);
					cellHeader.setCellValue((String) mapHeader
							.get(strHeaderkey));
					cellHeader.setCellStyle(cellHeaderStyle);
				}
			}
			
			for (int j = 0; j < listData.size(); j++) {

				Row row = s.createRow(j + 1);
				Map mapData = (Map) listData.get(j);

				int iRow = 0;
				int iHeaderRow = 0;

				Iterator iterHeader = mapHeader.keySet().iterator();

				while (iterHeader.hasNext()) {
					String strHeaderkey = (String) iterHeader.next();

					Iterator iterator = mapData.keySet().iterator();

					while (iterator.hasNext()) {

						if (strHeaderkey.equals((String) iterator.next())) {
							// iRow++;

							if (j == 0) {
								Cell cellHeader = rowHeader
										.createCell(iHeaderRow++);
								cellHeader.setCellValue((String) mapHeader
										.get(strHeaderkey));
								cellHeader.setCellStyle(cellHeaderStyle);
								
							}
							Cell cell = row.createCell(iRow++);
							cell.setCellValue(convertToString(mapData
									.get(strHeaderkey)) );
							
							continue;
						}
					}

				}

			}
			
		return wb;
	}
	
	
	
	/**
	 * 
	 * Method Name : createExcel<BR/>
	 * Description : 엑셀파일을 temp 폴더에 생성후 파일정보 리턴			  <BR/>	
	 *
	 * @author      : Park. ju wan <BR/>
	 * History <BR/>     
	 * 2014. 1. 10. Park. ju wan 최초작성 <BR/>
	 *
	 * @param req
	 * @param resp
	 * @param reqData
	 * @param wb
	 * @param strFileNm
	 * @return
	 * @throws Exception
	 */
	public static Map createExcel(HttpServletRequest req,
			HttpServletResponse resp, DataRequest reqData, Workbook wb, String strFileNm ) throws Exception {
		
				String strExportDir = getTempFileDir(req);

				File exportDir = new File(strExportDir);
				if (!exportDir.exists()) {
					exportDir.mkdir();
				}

				// 파일명변환
//				String strFileChgNm = UtilFile.getEncryptFileNm(req, strFileNm);
				String strFileChgNm = strFileNm;

				// 저장될 엑셀파일경로.
				String strExcelPath = strExportDir + File.separator + strFileChgNm;

				if (strExcelPath.indexOf("../") != -1) {
					throw new AppWorksException("정상적인 접근 방식이 아닙니다.");
				}
				try {
					// 파일생성경로
					FileOutputStream fout = null;
					try {
						fout = new FileOutputStream(new File(strExcelPath));
						wb.write(fout);
//						wb.releaseLock();
					} finally {
//						wb.releaseLock();
						wb = null;
						if (fout != null) {
							fout.close();
						}
					}

				} catch (IOException e){
					throw new AppWorksException("파일 업로드시 오류가 발생했습니다. \\n파일이 존재하지 않거나 네트워크가 불안정합니다.\\n관리자에게 문의바랍니다.");
				}
				Map<String, Object> mapFileInfo = new HashMap<String, Object>();
				mapFileInfo.put("strFileDir", strExportDir); // 파일경로
				mapFileInfo.put("strFileNm", strFileNm); // 파일명
				mapFileInfo.put("strFileChgNm", strFileChgNm); // 실제 서버에 저장된 파일명
				mapFileInfo.put("strTmpFilePath", strExportDir + File.separator + strFileChgNm); //템프파일 절대 경로

				return mapFileInfo;
		
	}
	
	/**
	 * 
	 * Method Name : getTmpFileDir<BR/>
	 * Description : 파일업로드시 템프파일 경로 <BR/>
	 *
	 * @author : Park. ju wan <BR/>
	 *         History <BR/>
	 *         2013. 8. 27. Park. ju wan 최초작성 <BR/>
	 *
	 * @param req
	 * @return
	 * @throws IOException
	 */
	public static String getTempFileDir(HttpServletRequest req)
			throws IOException {
		
		String tempFilePath = req.getServletContext().getRealPath("/")+AppProperties.getProperty("app.export.filePath");
		
		File tempDir = new File(tempFilePath);
		if(!tempDir.exists()) {
			tempDir.mkdirs();
		}
		
		return tempDir.getPath();
	}
	
	/**
	 * 
	 * Method Name : createExcelByPwd<BR/>
	 * Description : 엑셀파일을 입력받은 비밀번호로 암화하하여 폴더에 생성후 파일정보 리턴			  <BR/>	
	 *
	 * @author      : wslee <BR/>
	 * History <BR/>     
	 * 2022. 2. 23. wslee 최초작성 <BR/>
	 *
	 * @param req
	 * @param resp
	 * @param reqData
	 * @param wb
	 * @param strFileNm
	 * @param strPwd
	 * @return
	 * @throws Exception
	 */
	public static Map createExcelByPwd(HttpServletRequest req,
			HttpServletResponse resp, DataRequest reqData, Workbook wb, String strFileNm, String strPwd) throws Exception {
		
		
				String strExportDir = getTempFileDir(req);

				File exportDir = new File(strExportDir);
				if (!exportDir.exists()) {
					exportDir.mkdir();
				}
				
				// 파일명변환
				String strFileChgNm = FileUtil.getEncryptFileNm();

				// 저장될 엑셀파일경로.
				String strExcelPath = strExportDir + File.separator + strFileChgNm;

				if (strExcelPath.indexOf("../") != -1) {
					throw new AppWorksException("정상적인 접근 방식이 아닙니다.");
				}
				try {
					
					Biff8EncryptionKey.setCurrentUserPassword(new String(strPwd));
					// 파일생성경로
					FileOutputStream fout = null;
					try {
						fout = new FileOutputStream(new File(strExcelPath));
						wb.write(fout);
//						wb.releaseLock();
					} finally {
//						wb.releaseLock();
						wb = null;
						if (fout != null) {
							fout.close();
						}
						
						Biff8EncryptionKey.setCurrentUserPassword(null);
					}

				} catch (IOException e){
					throw new AppWorksException("파일 업로드시 오류가 발생했습니다. \\n파일이 존재하지 않거나 네트워크가 불안정합니다.\\n관리자에게 문의바랍니다.");
				}
				Map<String, Object> mapFileInfo = new HashMap<String, Object>();
				mapFileInfo.put("strFileDir", strExportDir); // 파일경로
				mapFileInfo.put("strFileNm", strFileNm); // 파일명
				mapFileInfo.put("strFileChgNm", strFileChgNm); // 실제 서버에 저장된 파일명
				mapFileInfo.put("strTmpFilePath", strExportDir + File.separator + strFileChgNm); //템프파일 절대 경로

				return mapFileInfo;
		
	}
	
	/**
	 * 
	 * Method Name : createExcel<BR/>
	 * Description : 엑셀파일을 temp 폴더에 생성후 파일정보 리턴			  <BR/>	
	 *
	 * @author      : Park. ju wan <BR/>
	 * History <BR/>     
	 * 2013. 10. 7. Park. ju wan 최초작성 <BR/>
	 *
	 * @param req			HttpServletRequest
	 * @param resp			HttpServletResponse	
	 * @param strFileNm		생성 할 파일명
	 * @param mapHeader		엑셀 헤더 정보
	 * @param listData		엑셀 로우 데이터
	 * @param reqData		DataRequest
	 * @param cellHeaderStyle	엑셀 헤더 스타일 (null가능)
	 * @param cellRowStyle		엑셀 로우 스타일 (null가능)
	 * @return mapFileInfo 
	 * 			- strFileDir :  파일경로
	 *	        - strFileNm  :  파일명
	 *          - strFileChgNm : 실제 서버에 저장된 파일명
	 *          
	 * @throws Exception
	 */
	public static Map createExcel(HttpServletRequest req,
			HttpServletResponse resp, String strFileNm, LinkedHashMap mapHeader,
			List listData, DataRequest reqData, String strSheetNm, CellStyle cellHeaderStyle,
			CellStyle cellRowStyle) throws Exception {
		
		
		Workbook wb = UtilExcel.makeWorkBookToList(mapHeader, listData,
				strFileNm, strSheetNm, null, null);
		
		String strExportDir = getTempFileDir(req);

		File exportDir = new File(strExportDir);
		if (!exportDir.exists()) {
			exportDir.mkdir();
		}

		// 파일명변환
		String strFileChgNm = FileUtil.getEncryptFileNm();

		// 저장될 엑셀파일경로.
		String strExcelPath = strExportDir + File.separator + strFileChgNm;

		if (strExcelPath.indexOf("../") != -1) {
			throw new AppWorksException("정상적인 접근 방식이 아닙니다.");
		}
		try {
			// 파일생성경로
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(new File(strExcelPath));
				wb.write(fout);
//				wb.releaseLock();
			} finally {
//				wb.releaseLock();
				wb = null;
				if (fout != null) {
					fout.close();
				}
			}

		} catch (IOException e){
			throw new AppWorksException("파일 업로드시 오류가 발생했습니다. \\\\n파일이 존재하지 않거나 네트워크가 불안정합니다.\\\\n관리자에게 문의바랍니다.");
		}
		
		Map<String, Object> mapFileInfo = new HashMap<String, Object>();
		mapFileInfo.put("strFileDir", strExportDir); // 파일경로
		mapFileInfo.put("strFileNm", strFileNm); // 파일명
		mapFileInfo.put("strFileChgNm", strFileChgNm); // 실제 서버에 저장된 파일명
		mapFileInfo.put("strTmpFilePath", strExportDir + File.separator + strFileChgNm); //템프파일 절대 경로

		return mapFileInfo;
	}
	
	/**
	 * 
	 * <pre>
	 * 메소드명	 : convertToString
	 * 설     명	 : String으로 변환
	 * </pre>
	 *
	 * @author	: Administrator
	 *
	 * 이력사항
	 *
	 * @param value
	 * @return
	 */
	private static String convertToString(Object value) {
		
		String converted = null;
		if (value == null) {
			// 빈문자열로 변경하지 않음.
			converted = null;
		} else if (value instanceof String) {
			converted = (String) value;
		} else if (value instanceof Date) {
			// -> YYYYMMDDHHMMSS
			converted = CalendarUtil.dateToString((Date) value);
		} else if (value instanceof java.util.Calendar) {
			converted = convertToString(convertToDate(value));
		} else {
			converted = value.toString();
		}
		
		return converted;
	}

	/**
	 * 
	 * <pre>
	 * 메소드명	 : convertToDate
	 * 설     명	 : Date로 변환
	 * </pre>
	 *
	 * @author	: Administrator
	 *
	 * 이력사항
	 *
	 * @param value
	 * @return Date
	 */
	private static Date convertToDate(Object value) {
		Date converted = null;
		try {
			if (value == null) {
				converted = null;
			} else if (value instanceof String) {
				converted = CalendarUtil.stringToDate(value);
			} else if (value instanceof Date) {
				converted = (Date) value;
			} else if (value instanceof Calendar) {
				converted = new Date(((Calendar) value).getTimeInMillis());
			} else {
				converted = convertToDate(value.toString());
			}
		} catch (Exception e) {
			throw new RuntimeException("convertToType", e);
		}
		return converted;
	}
   
    }

