package com.tomatosystem.core.massive;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.util.DateUtil;

public class StreamResponseAdapter {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	private OutputResolver resolver;
	private int nodeListIdx = 0;
	private int rowIdx = 0;
	private String nodeType = "list";

	private HttpServletResponse response;
	
	private final String DEFAULT_ERROR_MESSAGE = "요청 작업 처리중 오류가 발생하였습니다.시스템 관리자에게 문의하세요.";
	
	public StreamResponseAdapter(HttpServletRequest request, HttpServletResponse response){
		this.response = response;
		
		String charset = (request.getHeader("res-charset") == null) ? "utf-8" : request.getHeader("res-charset");
		charset = charset.replaceAll("\r", "").replaceAll("\n", "");
		response.setContentType("application/json;charset="+charset);
		
		this.nodeListIdx = 0;
		
		try{
			resolver = new OutputResolver(response.getWriter());
			resolver.append("{");
		}catch (IOException e) {
			logger.debug(e.getMessage());
		}
	}
	
	public void startNodes(String nodeName){
		if(nodeListIdx == 0) resolver.append("\"").append(nodeName).append("\":");
		else resolver.append(",\"").append(nodeName).append("\":");
		
		this.nodeListIdx++;
		this.rowIdx = 0;
	}
	
	public void startNodes(String nodeName, String nodeType){
		if(nodeListIdx == 0) resolver.append("\"").append(nodeName).append("\":");
		else resolver.append(",\"").append(nodeName).append("\":");
		
		this.nodeListIdx++;
		this.rowIdx = 0;
		
		this.nodeType = "dm".equals(nodeType) ? "map" : "list";
	}
	
	public void appendNode(Object data){
		if(data instanceof Map){
			if(rowIdx == 0){
				if("list".equals(this.nodeType)) resolver.append("[{");
				else  resolver.append("{");
			}else{
				resolver.append(",{");
			}
			
			Map item = (HashMap)data;
			
			Object key;
			Object value;
			int colIdx = 0;
			Iterator iter = item.keySet().iterator();
			while(iter.hasNext()){
				key = iter.next();
				value = item.get(key);
				value = value != null ? value : "";
				if(value instanceof java.sql.Date ||
						value instanceof java.util.Date ||
						value instanceof java.sql.Time ||
						value instanceof java.sql.Timestamp) {				
					
					value = DateUtil.dateToString(value);
				}
				
				if(colIdx != 0){
					resolver.append(",");
				}
				resolver.append("\"").append(key).append("\":").append(this.toJSONString(value.toString())).append("");
				colIdx++;
			}
			
			resolver.append("}");
			rowIdx++;
		}else{
			resolver.append("{").append("\"").append(data).append("\"}");
		}
	}
	
	public void endNodes(){
		if(this.rowIdx > 0){
			if("list".equals(this.nodeType)) resolver.append("]");
			else  resolver.append("");
		}else{
			if("list".equals(this.nodeType)) resolver.append("[]");
			else  resolver.append("{}");
		}
	}
	
	public void endResponse(){
		resolver.append("}");
		resolver.flush();
		resolver = null;
	}

	public void sendError(Exception e){
		this.response.reset();
		
		response.setContentType("application/json;charset=UTF-8");
		try{
			resolver = new OutputResolver(response.getWriter());
			resolver.append("{\"ERRMSGINFO\":{");
			if(e instanceof AppWorksException){
				AppWorksException appEx = (AppWorksException)e;
				resolver.append("\"STATUSCODE\":\"").append(appEx.getStatusCode()).append("\"");
				resolver.append(",\"ERRCODE\":\"").append("-10000").append("\"");
				resolver.append(",\"ERRMSG\":\"").append(e.getMessage()).append("\"");
			}else{
				resolver.append("\"STATUSCODE\":\"").append("500").append("\"");
				resolver.append(",\"ERRCODE\":\"").append("-10000").append("\"");
				resolver.append(",\"ERRMSG\":\"").append(DEFAULT_ERROR_MESSAGE).append("\"");
			}
			resolver.append("}}");
		}catch (IOException ex) {
			logger.debug(e.getMessage());
		}
	}
	
	private String toJSONString(String s)
    {
        if(s == null || s.length() == 0)
            return "\"\"";
        char c = '\0';
        int k = s.length();
        StringBuffer stringbuffer = new StringBuffer(k + 4);
        stringbuffer.append('"');
        for(int j = 0; j < k; j++)
        {
            int i = c;
            c = s.charAt(j);
            switch(c)
            {
            case 34: // '"'
            case 92: // '\\'
                stringbuffer.append('\\');
                stringbuffer.append(c);
                break;

            case 47: // '/'
                if(i == 60)
                    stringbuffer.append('\\');
                stringbuffer.append(c);
                break;

            case 8: // '\b'
                stringbuffer.append("\\b");
                break;

            case 9: // '\t'
                stringbuffer.append("\\t");
                break;

            case 10: // '\n'
                stringbuffer.append("\\n");
                break;

            case 12: // '\f'
                stringbuffer.append("\\f");
                break;

            case 13: // '\r'
                stringbuffer.append("\\r");
                break;

            default:
                if(c < ' ' || c >= '\200' && c < '\240' || c >= '\u2000' && c < '\u2100')
                {
                    String s1 = "000" + Integer.toHexString(c);
                    stringbuffer.append("\\u" + s1.substring(s1.length() - 4));
                } else
                {
                    stringbuffer.append(c);
                }
                break;
            }
        }

        stringbuffer.append('"');
        return stringbuffer.toString();
    }
}
