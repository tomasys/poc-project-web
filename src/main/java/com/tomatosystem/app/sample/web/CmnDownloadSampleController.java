package com.tomatosystem.app.sample.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cleopatra.XBConfig;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.tomatosystem.core.util.FileUtil;
import com.tomatosystem.core.util.StringUtil;

@Controller
@RequestMapping("/sampleData")
public class CmnDownloadSampleController {

	
	@RequestMapping("/download.do")
	public void download(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws IOException {
		ParameterGroup parameter = dataRequest.getParameterGroup("dmParam");
		String strFilePath = StringUtil.fixNull(parameter.getValue("filePath"));//다운로드받을 파일의 경로(deploy context ui 제외)
		String strFileNm = StringUtil.fixNull(parameter.getValue("fileNm")); //실제 파일명(확장자 포함);
		ServletContext servlet = request.getServletContext();
		String realPath = servlet.getRealPath("ui/"+strFilePath+strFileNm);
		try {
			FileUtil.fileDownloadWrapper(realPath, request, response, strFileNm);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
