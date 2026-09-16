package com.tomatosystem.core.exception;

import java.util.ArrayList;
import java.util.List;

import com.tomatosystem.core.constants.Alert;

/**
 * 
 * AppWorksException.java
 * 
 * @Description 어플리케이션 exception
 * @author tomatosystem
 * @since 2020. 10. 8.
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일             수정자             수정내용
 *    -------        ---------------       --------------
 *   2020. 10. 8.        tomatosystem       최초 생성
 *
 * </pre>
 */
public class AppWorksException extends AbstractException {

	private static final long serialVersionUID = 924510472390021999L;

	public AppWorksException(String message) {
		super(message);
		super.init(Alert.WARN, null);
	}
	
	public AppWorksException(String message, String param) {
		super(message);
		List<String> listParam = new ArrayList<String>();
		listParam.add(param);
		super.init(Alert.WARN, listParam);
	}
	
	public AppWorksException(String message, int statusCode) {
		super(message);
		super.init(statusCode, null);
	}
	
	public AppWorksException(String message, List<String> param) {
		super(message);
		super.init(Alert.WARN, param);
	}
	
	public AppWorksException(String message, int statusCode, String param) {
		super(message);
		List<String> listParam = new ArrayList<String>();
		listParam.add(param);
		super.init(statusCode, listParam);
	}
	
	public AppWorksException(String message, int statusCode, List<String> param) {
		super(message);
		super.init(statusCode, param);
	}

	public AppWorksException(String message, Throwable cause) {
		super(message, cause);
		super.init(Alert.WARN, null);
	}
	
	public AppWorksException(String message, int statusCode, Throwable cause) {
		super(message, cause);
		super.init(statusCode, null);
	}
	
	public AppWorksException(String message, int statusCode, List<String> param, Throwable cause) {
		super(message, cause);
		super.init(statusCode, param);
	}

	public AppWorksException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		super.init(Alert.WARN, null);
	}
	
	public AppWorksException(String message, int statusCode, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		super.init(statusCode, null);
	}
	
	public AppWorksException(String message, int statusCode, List<String> param, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		super.init(statusCode, param);
	}
}
