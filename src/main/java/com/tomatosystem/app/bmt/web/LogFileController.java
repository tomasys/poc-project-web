package com.tomatosystem.app.bmt.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cleopatra.protocol.data.DataRequest;

@Controller
@RequestMapping("/bmt")
public class LogFileController {
	public LogFileController() {
	}

	@RequestMapping("/log.do")
	public void list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		BufferedWriter bw = null;
		FileWriter objfile = null;

		try {
			String strLogPath = "C:/tomatosystem/log/";
			File Folder = new File(strLogPath);
			if (!Folder.exists()) {
				try {
					Folder.mkdir(); // 폴더 생성합니다.
				} catch (Exception e) {
//					e.getStackTrace();
				}
			}

			String strFileName = dataRequest.getParameter("fileName");
			String fileName = strLogPath+strFileName+".log";
			
			File file  = new File(fileName);
			if(file.exists() && file.isFile()) {
				file.delete();
			}
			
			objfile = new FileWriter(fileName, true);

			bw = new BufferedWriter(objfile);
			bw.write(dataRequest.getParameter("log"));
			bw.newLine();
		} catch (Exception e) {
		} finally {
			if (bw != null) {
				bw.close();
			}
			if (objfile != null) {
				objfile.close();
			}
		}
	}
	
	@RequestMapping("/logServer.do")
	public void logServer(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		BufferedWriter bw = null;
		FileWriter objfile = null;

		try {
			String strLogPath = "C:/tomatosystem/log/server/";
			File Folder = new File(strLogPath);
			if (!Folder.exists()) {
				try {
					Folder.mkdir(); // 폴더 생성합니다.
				} catch (Exception e) {
//					e.getStackTrace();
				}
			}

			String strFileName = dataRequest.getParameter("fileName");
			String fileName = strLogPath+strFileName+".log";
			
			File file = new File(fileName);
			
			if(file.exists()) {
				file.delete();
			}
			
			objfile = new FileWriter(fileName, true);

			bw = new BufferedWriter(objfile);
			bw.write(dataRequest.getParameter("log"));
			bw.newLine();
		} catch (Exception e) {
		} finally {
			if (bw != null) {
				bw.close();
			}
			if (objfile != null) {
				objfile.close();
			}
		}
	}
}
