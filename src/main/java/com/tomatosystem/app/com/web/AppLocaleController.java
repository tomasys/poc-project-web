package com.tomatosystem.app.com.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cleopatra.i18n.I18N;
import com.cleopatra.protocol.data.DataRequest;
//import com.tomatosystem.core.context.ExtReloadableResourceBundleMessageSource;
//import com.tomatosystem.core.context.StaticSpringApplicationContext;

/**
 * 
 * AppLocaleController.java
 * 
 * @Description  language.json 형식의 javascript를 UI로 전달, spa구조에서 최초 루트앱 로드시 사용 
 * @author tomatosystem
 * @since 2020. 10. 7.
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일             수정자             수정내용
 *    -------        ---------------       --------------
 *   2020. 10. 7.        tomatosystem       최초 생성
 *
 * </pre>
 */
@Controller
public class AppLocaleController {
	
//	@Autowired                                                               
//	private MessageSourceAccessor message;

	@RequestMapping("/i18n/locale.do")
	public void locale(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws SQLException, IOException {
		
		String locale = getAppLocale(dataRequest);
		locale = locale.toLowerCase();
		
		Map<String, String> glossary = new HashMap<String, String>();
		
		/** ReloadableResourceBundleMessageSource에 정의된 message-common.properties 파일 내용을 클라이언트 메시지로 사용하기 위함 
		ExtReloadableResourceBundleMessageSource messageSource = (ExtReloadableResourceBundleMessageSource) StaticSpringApplicationContext.getBean("messageSource");
		Properties allMessages = messageSource.getMessages(LocaleContextHolder.getLocale());

	    Set<Map.Entry<Object, Object>> entries = allMessages.entrySet();
	    for(Map.Entry<Object, Object> entry : entries) {
	        String key = (String)entry.getKey();
	        String value = (String)entry.getValue();
	        glossary.put((String) key, value);
	    }
	    */
		I18N i18n = new I18N();
		i18n.setLanguage(locale);
		i18n.set(locale, glossary);
		
		response.setContentType("application/javascript; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		ServletOutputStream out = response.getOutputStream();
		i18n.writeScript(out);
	}
	
	public String getAppLocale(DataRequest dataRequest) {
//		String strLocale = dataRequest.getParameter(ProcessConstants.DEFAULT_LOCALE);
//		return strLocale != null ? strLocale : AppProperties.getProperty("default.locale").toUpperCase();
		return "ko";
	}
}
