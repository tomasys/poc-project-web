package com.tomatosystem.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class mpGenTest2 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub4.
		
//		setProperties("bo");
//		Mp2ClxGenMain xmain = new Mp2ClxGenMain() ; 
//    	xmain.runMkFile(null);
		
		
		 Path relativePath = Paths.get("");
	      String path = relativePath.toAbsolutePath().toString();
	      System.out.println("Working Directory = " + path);
	      
	      
	      String currentPaht = Paths.get("").toAbsolutePath().toString();
	      System.out.println("currentPaht Directory = " + currentPaht);
	      String currentPath = new File("").getAbsolutePath();
	      System.out.println("currentPaht Directory = " + currentPath);
	}

	
	public static void setProperties(String strFolder) {
		 
		 String rootPath = "D:\\workspace\\poc\\eXCFrame-poc-web\\Miplatform\\Source\\crm";
		 
			Properties p = new Properties();
			InputStream is = null;
			OutputStream os = null;
				try {
					is = new FileInputStream("D:\\workspace\\poc\\eXCFrame-poc-web\\src\\main\\webapp\\WEB-INF\\lib\\config\\mp.to.clx.config.properties");
					p.load(is);
					p.setProperty("M_SORUCE_PATH", "Source\\crm" + File.separator + strFolder);
					os = new FileOutputStream("D:\\workspace\\poc\\eXCFrame-poc-web\\src\\main\\webapp\\WEB-INF\\lib\\config\\mp.to.clx.config.properties");
					p.store(os, null);
				} catch (IOException e) {
				}
			}
 
}
