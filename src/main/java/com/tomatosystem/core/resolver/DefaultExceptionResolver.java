package com.tomatosystem.core.resolver;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.exception.AbstractException;
import com.tomatosystem.core.exception.AppWorksException;

import oracle.jdbc.OracleDatabaseException;

/**
 * 
 * DefaultExceptionResolver.java
 * 
 * @Description Exception Resolver
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
public class DefaultExceptionResolver implements HandlerExceptionResolver {
	private final Logger logger = LogManager.getLogger(DefaultExceptionResolver.class);
	
	private final String DEFAULT_ERROR_MESSAGE = "요청 작업 처리중 오류가 발생하였습니다.\n시스템 관리자에게 문의하세요.";
	
	@Autowired                                                               
	private MessageSourceAccessor messageSource;
	
	@Override
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		HandlerMethod handlerMethod = (HandlerMethod)handler;
		AbstractException appException = null;
		String oriErrMsg = "";
		SQLException sqlEx = null;
		String message = null;
		if(ex instanceof AbstractException) {
			appException = (AbstractException)ex;
			
			logger.error("\nError Type: AppWorksException: "
						+"\nError Message: "+ex.getMessage()
						+"\nposition: "+ex.getStackTrace()[0]);
			
			message = ex.getMessage();
		}else if(ex instanceof DataAccessException) {
			
			if(((DataAccessException)ex).getRootCause() instanceof SQLException)
			{
				sqlEx = (SQLException) ((DataAccessException) ex).getRootCause();
				
				appException = new AppWorksException("CMN003.CMN@"+sqlEx.getErrorCode(), 500);
				
			}
			else if(((DataAccessException)ex).getRootCause() instanceof OracleDatabaseException) {
				
				OracleDatabaseException oraEx = (OracleDatabaseException) ((DataAccessException) ex).getRootCause();
				
				appException = new AppWorksException("CMN003.CMN@ORA"+oraEx.getOracleErrorNumber(), 500);
				
			}else{
				appException = new AppWorksException("CMN003.CMN@CMN100", 500);
			}
			
			//Error console Logging
			if(logger.isInfoEnabled()){
				ex.printStackTrace();
			}
		}else{
			oriErrMsg = ex.getMessage();
			// 에러가 발생했습니다. 시스템 관리자에게 문의하여 주십시오.
			appException = new AppWorksException("CMN003.CMN@CMN030", 500);
			
			//Error console Logging
			if(logger.isInfoEnabled()){
				ex.printStackTrace();
			}
		}
		
		int statusCode = appException.getStatusCode();
		
		Map<String, Object> mapMsg = null;
		
		if(message == null){
			message = appException.parseMessage(messageSource.getMessage(appException.getMessage(), DEFAULT_ERROR_MESSAGE));
		}
		
		//eXbuilder6 화면에서 호출한 경우인지 확인
		boolean isExbuilderRequest = false;
		MethodParameter[] params = handlerMethod.getMethodParameters();
		if(params != null && params.length > 0) {
			MethodParameter mparam = null;
			for(int i = 0, len=params.length; i < len; i++) {
				mparam = params[i];
				if(DataRequest.class.isAssignableFrom(mparam.getParameterType())) {
					isExbuilderRequest = true;
					break;
				}
			}
		}
		
		//eXbuilder6 요청에 의한 오류인 경우에는
		//JSON 형태로 에러 정보를 만들어서 reponse에 반환한다.
		if(isExbuilderRequest){
			Map<String, Object> mapErrResult = new HashMap<String, Object>();
			Map<String, Object> errorState = new HashMap<String, Object>();
			errorState.put("STATUSCODE", statusCode);
			errorState.put("ERRCODE",appException.getMessage());
			errorState.put("ERRMSG",message);
			mapErrResult.put("ERRMSGINFO", errorState);
			
			 ModelAndView mv = new ModelAndView();
		     mv.addObject("ERRMSGINFO", errorState);
		     mv.setView(new JSONDataView());
//		     return new ModelAndView(new JSONDataView(mapErrResult));
		     if(500 == statusCode) {
		    	 response.setStatus(statusCode);
		     }
			return mv;
		}else{
			//그외 HTML, JSP에서 요청온 경우이면... HTTP 에러코드를 반환한다.
			try {
				response.sendError(statusCode, message);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	 private ModelAndView _resolveException(String viewName, Exception e) {
         ModelAndView modelAndView = new ModelAndView();
         
         modelAndView.setViewName(viewName);

          if (viewName.equals("jsonView" )) {
                modelAndView.addObject( "errorCode" , 1);
                modelAndView.addObject( "errorMessage" , String.format( "Exception : %s( %s )", e.getClass().getName(), e.getMessage()));
                 return modelAndView;
         } else {
                modelAndView.addObject( "error" , e.getMessage());
                 return modelAndView;
         }
  }
}
