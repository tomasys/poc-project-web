package com.tomatosystem.exbuilder.web;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.XBConfig;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.spring.JSONDataView;
import com.cleopatra.spring.UIView;
import com.tomatosystem.core.util.StringUtil;

@Controller
@RequestMapping("/Error")
public class ErrorPageUIController {
	
	private final Logger logger = LogManager.getLogger(ErrorPageUIController.class);
	
	
	@RequestMapping(value="/error{error_code}.do")
    public View error(HttpServletRequest request, @PathVariable String error_code, HttpServletResponse resp, DataRequest dataRequest) {
		
        String msg = (String) request.getAttribute("javax.servlet.error.message");
        int statusCode = (int) request.getAttribute("javax.servlet.error.status_code");
        String reqUri = (String) request.getAttribute("javax.servlet.error.request_uri");
//        String exceptionTp = (String) request.getAttribute("javax.servlet.error.exception_type");
        Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");
        String servletNm = (String) request.getAttribute("javax.servlet.error.servlet_name");
         
        
        Map<String, Object> errorState = new HashMap<String, Object>();
        
        errorState.put("STATUSCODE", statusCode);
  		errorState.put("ERRCODE", exception != null ? exception.toString() : Integer.toString(statusCode));
  		errorState.put("REQUESTURI",reqUri);
  		errorState.put("SERVLETNAME",servletNm);
  		
  		int status_code;
        try {
            status_code = Integer.parseInt(error_code);
            switch (status_code) {
            case 400: msg = "잘못된 요청입니다."; break;
            case 403: msg = "접근이 금지되었습니다."; break;
            case 404: msg = "페이지를 찾을 수 없습니다."; break;
            case 405: msg = "요청된 메소드가 허용되지 않습니다."; break;
            case 500: msg = "시스템 내부 장애가 발생하였습니다."; break;
            case 503: msg = "서비스를 사용할 수 없습니다."; break;
            default: msg = "알 수 없는 오류가 발생하였습니다."; break;
            }
        } catch(Exception e) {
            msg = "기타 오류가 발생하였습니다.";
            status_code = 500;
        } finally {
        	errorState.put("ERRMSG",msg);
        }
      		
        if(errorState.isEmpty() == false ) {
            Iterator<Entry<String,Object>> iterator = errorState.entrySet().iterator();
            Entry<String,Object> entry = null;
            while(iterator.hasNext()) {
                entry = iterator.next();
                logger.info("key : "+entry.getKey()+", value : "+entry.getValue());
            }
        }
        
        if(StringUtil.isNotNullEmpty(request.getHeader("referer"))) {
        	dataRequest.setResponse("ERRMSGINFO", errorState);
     	    dataRequest.setMetadata(true, errorState);
     	    return new JSONDataView();  
        }else {
        	String strErrorPage = "";
        	List<String> pathList = XBConfig.getInstance().getDeployPath(); // eXbuilder6
    		String deployPath = pathList.get(0);
    		
        	if(status_code == 404) {
        		
        		strErrorPage = deployPath + "/" + "app/com/comError404.clx" ;
        	}else {
        		strErrorPage = deployPath + "/" + "app/com/comError500.clx" ;
        	}
        	return new UIView(strErrorPage);
        	
        }
        
	   
    }
}
