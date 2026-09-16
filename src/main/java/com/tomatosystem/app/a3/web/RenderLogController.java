package com.tomatosystem.app.a3.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;

@Controller
@RequestMapping("/a36")
public class RenderLogController {
	public RenderLogController() {
	}

	@RequestMapping("/log.do")
	public void list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		BufferedWriter bw = null;
		
		try {
			String strLogPath = "C:/Tmp/";
			File Folder = new File(strLogPath);
			if (!Folder.exists()) {
				try {
					Folder.mkdir(); // 폴더 생성합니다.
				} catch (Exception e) {
					e.getStackTrace();
				}
			}
			
			ParameterGroup dmLog = dataRequest.getParameterGroup("dmLog");
			String strBrowser = dmLog.getValue("BROWSER");
			
			SimpleDateFormat today = new SimpleDateFormat("yyyyMMdd");
			String fileName = strLogPath + "TOMATOSYSTEM_RanderLog_" + strBrowser + "_" + today.format(new Date())+ ".log";
			FileWriter objfile = new FileWriter(fileName, true);
			
			String strMdiNo = dmLog.getValue("MDI_NO");
			String strTabNo = dmLog.getValue("TAB_NO");
			String strStartTime = dmLog.getValue("START_TM");
			String strEndTime = dmLog.getValue("END_TM");
			String strTotalTime = dmLog.getValue("TIME");
			
			bw = new BufferedWriter(objfile);
			bw.write("MDI번호 : " + strMdiNo);
			bw.newLine();
			bw.write("탭번호 : " + strTabNo);
			bw.newLine();
			bw.write("시작시각(시분초) : " + strStartTime);
			bw.newLine();
			bw.write("종료시각(시분초) : " + strEndTime);
			bw.newLine();
			bw.write("소요시간(초) : " + strTotalTime);
			bw.newLine();
			bw.write("=================================");
			bw.newLine();
		} catch (Exception e) {
		} finally {
			if(bw != null) {
				bw.close();
			}
		}
	}
}
