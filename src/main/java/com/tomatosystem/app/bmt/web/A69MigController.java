package com.tomatosystem.app.bmt.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exbuilder.adaptor.xtm.gen.mp2clx.Mp2ClxGenMain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.resource.AppProperties;


@Controller
@RequestMapping("/A69Mig")
public class A69MigController {
	public A69MigController() {}
	
	@RequestMapping("/list.do")
	public View list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		 String rootPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "Miplatform\\Source\\crm";
		 File rootFile = new File(rootPath);
		 List list  = new ArrayList();
	        
        for(File file : Objects.requireNonNull(rootFile.listFiles())) {
            getFiles(list, file, rootFile, 0);
        }
		dataRequest.setResponse("dsSource", list);
		dataRequest.setResponse("dsTarget", getCompleteClxFiles(request));
		
		return new JSONDataView();
	}
	
	 public static void getFiles(List list, File file, File parentFile, int level) {
	    	Map mapFile = new HashMap();
	        if(file.isDirectory()) {	
	        	mapFile.put("parent", parentFile.getName());
	        	mapFile.put("file", file.getName());
	        	mapFile.put("level", ++level);
	        	list.add(mapFile);
	            for (File childFile : Objects.requireNonNull(file.listFiles())) {
	                getFiles(list, childFile, file, level);
	            }
	        } else {
	        	mapFile.put("parent", parentFile.getName());
	        	mapFile.put("file", file.getName());
	        	mapFile.put("level", ++level);
	        	list.add(mapFile);
	        }
	    }
	 
	 @RequestMapping("/mig.do")
		public View mig(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		 
		 ParameterGroup dsSource = dataRequest.getParameterGroup("dsSource");
		 List<Map<String, String>> uptList = dsSource.getUpdatedRowList();
		 
		    
		 for(Map map : uptList) {
			 String strFile = (String) map.get("file");
			 setProperties(strFile);
			 Mp2ClxGenMain xmain = new Mp2ClxGenMain() ; 
		    xmain.runMkFile(null);
		 }
		 
		 dataRequest.setResponse("dsTarget", getCompleteClxFiles(request));
		 
		 return new JSONDataView();
		 
	 }
	 
	 
	 public List getCompleteClxFiles(HttpServletRequest request) {
		 
		// String rootPath = request.getSession().getServletContext().getRealPath("/") + File.separator + "ui\\app\\mpMig\\Source\\crm";
		 
		 String strWorkspace = AppProperties.getProperty("local.workspace");
		 
		 String strCompPath = strWorkspace + "eXCFrame-poc\\clx-src\\app\\mpMig\\Source\\crm";
		 
		 File rootFile = new File(strCompPath);
		 List list  = new ArrayList();
		 
		 if(rootFile.listFiles() == null) {
			 return list;
		 }
	        
        for(File file : Objects.requireNonNull(rootFile.listFiles())) {
            getFiles(list, file, rootFile, 0);
        }
		 return list;
	}
	 
	public void setProperties(String strFolder) {

		Properties p = new Properties();
		InputStream is = null;
		OutputStream os = null;
		
        String strConfigPropPath = this.getClass().getResource("/config/mp.to.clx.config.properties").getPath();
		try {
			is = new FileInputStream(
					strConfigPropPath);
			p.load(is);
			p.setProperty("M_SORUCE_PATH", "Source\\crm" + File.separator + strFolder);
			os = new FileOutputStream(
					strConfigPropPath);
			p.store(os, null);
		} catch (IOException e) {
		}
	}
 
}


