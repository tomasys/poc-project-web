package com.tomatosystem.app.com.web;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.stream.events.Characters;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.XBConfig;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.spring.UIView;
import com.cleopatra.ui.PageConfig;
import com.tomatosystem.core.resource.AppProperties;


@Controller
public class IndexController {
	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	private static final SecureRandom RANDOM = new SecureRandom();
	
	@RequestMapping("/index.do")
	public View index(HttpServletRequest request, HttpServletResponse response, 
			DataRequest reqData) throws Exception {
		
		HttpSession session = request.getSession(false);
		Map<String,Object> initParam = new HashMap<>();
		
		if(session != null) {
			initParam.put("userType", session.getAttribute("userType"));
			initParam.put("userNm", session.getAttribute("userNm"));
			initParam.put("userId", session.getAttribute("userId"));
			String token = RandomStringGenerate();
			session.setAttribute("CSRF_TOKEN", token);
			initParam.put("CSRF_TOKEN", token);
		} else {
			return new UIView("ui/app/main/Login.clx");
//			session = request.getSession();
//			session.setAttribute("userType",  "admin");
//			session.setAttribute("userNm",  "admin");
		}
		
		List<String> pathList = XBConfig.getInstance().getDeployPath(); //eXbuilder6 deploy path
		
		String deployPath = pathList.get(0);
		
		String mainPageUrl = deployPath+"/";   //메인 페이지 URL
		
		mainPageUrl += AppProperties.getProperty("main.page.appid")+".clx";
		
		UIView uiView = new UIView(mainPageUrl,initParam);
		
		Device device = DeviceUtils.getCurrentDevice(request); 
		PageConfig config = uiView.getPageConfig();
		
		if(device.isMobile()) {
//			config.setMetaTag("viewport", "width=550");
			//<meta name="viewport" content="width=device-width,initial-scale=1.0">
		}
		config.setTitle("Main");
//		config.setMetaTag("description", "eXBuilder6, 엑스빌더6, 각종 기능 예제, 유형별 화면 템플릿, 공통모듈 예제 데모를 확인 할 수 있습니다.");
//		config.setMetaTag("og:title", "eXCFrame");
//		config.setMetaTag("og:url", "http://edu.tomatosystem.co.kr");
//		config.setMetaTag("og:image", "http://edu.tomatosystem.co.kr/theme/images/com/exb6-logo-og-image.png");
//		config.setMetaTag("og:description", "eXBuilder6 각종 기능 예제, 유형별 화면 템플릿, 공통모듈 예제 데모를 확인 할 수 있습니다.");
		return uiView; 
	}
	
	public static String RandomStringGenerate() {
		StringBuilder sb = new StringBuilder(16);
		for(int i = 0; i < 16; i++) {
			sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
		}
		return sb.toString();
	}
//	@RequestMapping("/index.do")
//	public ModelAndView index(HttpServletRequest request, HttpServletResponse response, 
//			DataRequest reqData) throws Exception {
//
//		ModelAndView mv = new ModelAndView();
//		mv.setViewName("redirect:/index_exb6.jsp");
//		mv.setView(new UIView(mainPageUrl));
//		return mv; 
//		
//	}
}
