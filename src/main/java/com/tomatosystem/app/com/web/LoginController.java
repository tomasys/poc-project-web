package com.tomatosystem.app.com.web;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DeviceUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.XBConfig;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.cleopatra.spring.UIView;
import com.cleopatra.ui.PageConfig;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.resource.AppProperties;

@Controller
@RequestMapping("/Login")
public class LoginController {
	
	@RequestMapping("/login.do")
	public View login(HttpServletRequest request, HttpServletResponse resp, DataRequest dataRequest) throws Exception {
		
		Map<String, Object> message = new HashMap<String, Object>();
		
		ParameterGroup pgParam = dataRequest.getParameterGroup("dmReq");
		 
//		String pwd = AESCryptUtil.decrypt(AESCryptUtil.SALT,
//				AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, pgParam.getValue("strUserPw")	, 
//				AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE);
		
		HttpSession session = null;
		
		session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
		session = request.getSession(true);
		String paramStr = request.getParameter("idParam");
		String userId = pgParam.getValue("strUserNm");
		session.setAttribute("userType",  paramStr);
		session.setAttribute("userId",  userId);
		String strName = "홍길동";
		if("admin".equals(userId)) {
			strName = "관리자";
		}
		session.setAttribute("userNm",  strName);
		
		return new JSONDataView();
	}
	
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: logout
	 * 설	 명	: 로그아웃을 한다.
	 * </pre>
	 *
	 * @param req
	 * @param resp
	 * @param dataView
	 * @param sqlClientAssists
	 * @param reqData
	 * @return
	 * @throws IOException
	 * @throws SQLException
	 * @throws StdServiceException
	 * @throws AppWorksException
	 */
	@RequestMapping("/logout.do")
	public View logout(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		Map<String, Object> message = new HashMap<String, Object>();
		HttpSession session = request.getSession(false);
		if(session != null) {
			session.invalidate();
		}
//		message.put("uri", AppProperties.getProperty("login.page.appid")); //로그인 AppId
//		
//		dataRequest.setMetadata(true, message);
		
		return new JSONDataView();
	}
	
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: kakaoLoginForNative
	 * 설	 명	: http://211.109.22.33/eXCFrame/로 리다이렉트 
	 * </pre>
	 */
	@RequestMapping("/kakaoLoginForNative.do")
	public void kakaoLoginForNative(HttpServletResponse httpServletResponse) throws Exception {
		httpServletResponse.sendRedirect(AppProperties.getProperty("url.home"));

	}
	

	
	@RequestMapping("/kakaoLoginIndex.do")
	public View kakaoLoginIndex(HttpServletRequest request, HttpServletResponse response, 
			DataRequest reqData) throws Exception {
		
		
		List<String> pathList = XBConfig.getInstance().getDeployPath(); //eXbuilder6 deploy path
		
		String deployPath = pathList.get(0);
		
		String mainPageUrl = deployPath+"/";   //메인 페이지 URL
		
		mainPageUrl += AppProperties.getProperty("hybrid.page.appid")+".clx";
		
		UIView uiView = new UIView(mainPageUrl);
		
		Device device = DeviceUtils.getCurrentDevice(request); 
		
		if(device.isMobile()) {
			PageConfig config = uiView.getPageConfig();
			config.setMetaTag("viewport", "width=550");
			//<meta name="viewport" content="width=device-width,initial-scale=1.0">
		}
		
		return uiView; 
	}
	
	
	
}
