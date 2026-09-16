package com.tomatosystem.exbuilder.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cleopatra.XBConfig;
import com.cleopatra.export.CSVExporter;
import com.cleopatra.export.Exporter;
import com.cleopatra.export.ExporterFactory;
import com.cleopatra.export.ExporterFactory.EXPORTTYPE;
import com.cleopatra.export.PDFExporter;
import com.cleopatra.export.source.DataSource;
import com.cleopatra.export.source.JSONDataSourceBuilder;
import com.cleopatra.export.target.HttpResponseOutputTarget;
import com.cleopatra.export.target.OutputTarget;
import com.tomatosystem.core.constants.Alert;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.util.StringUtil;


@Controller
@RequestMapping("/")
public class CleopatraFileExportController {
	
	@PostConstruct
	private void init() {
		// PDF Export를 사용할 경우 PDF에서 사용할 폰트파일을 등록(공개된 폰트만 사용)
		PDFExporter.setDefaultTruTypeFont(new java.io.File(XBConfig.getServletContext().getRealPath("/") + "WEB-INF/config/docs/malgun.ttf"));
		// 제목글의 폰트 사이즈(단위는 point)
		PDFExporter.setDefaultTitleFontSize(8);
		// 목록글의  폰트 사이즈(단위는 point)
		PDFExporter.setDefaultTableFontSize(6);
	}
	
	@RequestMapping("/{fileName}.csv")
	public void exportCSV(HttpServletRequest request, HttpServletResponse response, @PathVariable("fileName") String fileName) throws IOException {
		String downloadFileName = fileName + ".csv";
		downloadFileName = this.encodingDownloadFileName(request, downloadFileName);
		
		//사용자 세션이 없으면.. 엑셀 다운로드 불가토록...
		HttpSession session = request.getSession(false);
		if(session == null) {
			//사용자 세션이 존재 하지 않습니다.
			throw new AppWorksException("CMN003.CMN@CMN003", Alert.ERROR);
		}
				
		response.setContentType("text/csv;charset=utf-8");
		response.addHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
		
		this.export(request, response, fileName, EXPORTTYPE.CSV);
	}
	
	@RequestMapping("/{fileName}.xls")
	public void exportXLS(HttpServletRequest request, HttpServletResponse response, @PathVariable("fileName") String fileName) throws IOException {
		String downloadFileName = fileName + ".xls";
		downloadFileName = this.encodingDownloadFileName(request, downloadFileName);
		
		//사용자 세션이 없으면.. 엑셀 다운로드 불가토록...
		HttpSession session = request.getSession(false);
//		if(session == null) {
			//사용자 세션이 존재 하지 않습니다.
//			throw new AppWorksException("CMN003.CMN@CMN003", Alert.ERROR);
//		}
				
		response.setContentType("application/vnd.ms-excel");
		response.addHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
		
		this.export(request, response, fileName, EXPORTTYPE.XLS);
	}
	
	@RequestMapping("/{fileName}.xlsx")
	public void exportXLSX(HttpServletRequest request, HttpServletResponse response, @PathVariable("fileName") String fileName) throws IOException {
		String downloadFileName = fileName + ".xlsx";
		downloadFileName = this.encodingDownloadFileName(request, downloadFileName);
		
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.addHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
		
//		HttpSession session = request.getSession(false);
		//사용자 세션이 없으면.. 엑셀 다운로드 불가토록...
//		if(session == null) {
			//사용자 세션이 존재 하지 않습니다.
//			throw new AppWorksException("CMN003.CMN@CMN003", Alert.ERROR);
//		}
		
		String applySuppress = StringUtil.fixNull(request.getHeader("applySuppress"));
		
		if(applySuppress.equals("true")) {
			this.export(request, response, fileName, EXPORTTYPE.XLSX);
		}else {
			this.export(request, response, fileName, EXPORTTYPE.SXLSX);
		}
	}
	
	@RequestMapping("/{fileName}.pdf")
	public void exportPDF(HttpServletRequest request, HttpServletResponse response, @PathVariable("fileName") String fileName) throws IOException {
		String downloadFileName = fileName + ".pdf";
		downloadFileName = this.encodingDownloadFileName(request, downloadFileName);
		
		response.setCharacterEncoding("utf-8");
		response.setContentType("application/pdf");
		response.addHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
		
		this.export(request, response, fileName, EXPORTTYPE.PDF);
		
	}
	
	private void export(HttpServletRequest request, HttpServletResponse response, String fileName, EXPORTTYPE type) throws IOException {
		response.setCharacterEncoding("utf-8");
		String newFileName = URLDecoder.decode(fileName, "utf-8");
		DataSource dataSource = JSONDataSourceBuilder.build(request, newFileName);
		OutputTarget outputTarget = new HttpResponseOutputTarget(response);
		
		ExporterFactory exporterFactory = ExporterFactory.getInstance();
		Exporter exporter = exporterFactory.getExporter(type);
		
		if(type == EXPORTTYPE.CSV){
			((CSVExporter)exporter).setAutoWrap(false);
		}
		
		exporter.export(dataSource, outputTarget);
		
		response.flushBuffer();
	}
	
	private String encodingDownloadFileName(HttpServletRequest request, String psDownloadFileName) throws UnsupportedEncodingException {
		
		String downloadFileName = psDownloadFileName;
		String userAgent = request.getHeader("User-Agent");
		
		if(userAgent.contains("MSIE") || userAgent.contains("Chrome") || (userAgent.contains("Windows") && userAgent.contains("Trident"))){
			downloadFileName = URLEncoder.encode(downloadFileName, "utf-8");
			downloadFileName = downloadFileName.replaceAll("\\+","%20");
        }
		
		return downloadFileName;
	}
}
