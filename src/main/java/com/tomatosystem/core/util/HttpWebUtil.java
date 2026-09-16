package com.tomatosystem.core.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;

public class HttpWebUtil {
	
	/**
	 * 웹 어플리케이션 파일 다운로드를 위한 UrlEncoded된 파일명을 반환한다.
	 * @param request
	 * @param fileName
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getUrlEncodedFileName(HttpServletRequest request, String fileName) throws UnsupportedEncodingException{
		String userAgent = request.getHeader("user-agent");
		String resCharset = request.getHeader("res-charset");
		if ((resCharset == null) || (resCharset.equalsIgnoreCase(""))) {
			resCharset = "UTF-8";
		}
		
		//IE 브라우저인 경우
    	if ((userAgent.indexOf("MSIE") > -1 
    			|| userAgent.indexOf("Edge") > -1 
    			|| userAgent.indexOf("Trident") > -1) 
    			|| (userAgent.indexOf("Java") > -1)){
    		fileName = URLEncoder.encode(fileName, resCharset);
    		fileName = fileName.replace("+", "%20");
    	
    	//크롬/firefox 브라이저인 경우
    	}else if(userAgent.indexOf("Chrome") > -1){
    		StringBuffer sb = new StringBuffer();
  			for(int i=0; i<fileName.length(); i++) {
  				char c = fileName.charAt(i);
  				if(c>'~') {
  					sb.append(URLEncoder.encode(""+c, "UTF-8"));
  				}else {
  					sb.append(c);
  				}
  			}
  			fileName = sb.toString();
  			
    	}else if(userAgent.indexOf("Firefox") > -1){
    		fileName = new String(fileName.getBytes(), "8859_1");
    	//기타 브라우저인 경우
    	} else {
    		fileName = new String(fileName.getBytes(), "8859_1");
		}
    	
    	return fileName.replaceAll("[\\r\\n]", "");
	}
}
