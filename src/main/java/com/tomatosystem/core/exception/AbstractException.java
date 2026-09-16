package com.tomatosystem.core.exception;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class AbstractException extends RuntimeException {
	private int statusCode;
	private List<String> param;
	private final Pattern findPattern = Pattern.compile("\\{[0-9]+\\}");
	
	public AbstractException(String message) {
		super(message);
	}

	public AbstractException(String message, Throwable cause) {
		super(message, cause);
	}

	public AbstractException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
	protected void init(int statusCode, List<String> param) {
		this.statusCode = statusCode;
		this.param = param;
	}
	
	public int getStatusCode() {
		return this.statusCode;
	}

	public String getLocalizedMessage(String language) {
		String message = this.getMessage();
		if(this.param == null || this.param.size() == 0) {
			return message;
		}
		
		return message;
	}
	
	
	
	public String parseMessage(String strMsg) {
		if (this.param == null || strMsg.indexOf("{") == -1) {
			return strMsg;
		} else {
			Matcher matcher = findPattern.matcher(strMsg);
			String key = null;
			Object value = null;
			StringBuffer buf = new StringBuffer();
			while(matcher.find()){
				key = (String) matcher.group();
				key = key.replace("{", "").replace("}", "").trim();
				value = param.get(Integer.parseInt(key));
				matcher.appendReplacement(buf, value != null ? value.toString() : ""); 
			}
			matcher.appendTail(buf);
			
			return buf.toString();
		}
	}
}
