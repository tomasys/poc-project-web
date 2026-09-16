package com.tomatosystem.app.A10.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.util.AESCryptUtil;


@Controller
@RequestMapping("/A10")
public class A10Controller {
	public A10Controller() {}
	
	@RequestMapping("/list.do")
	public View list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		ParameterGroup dmParam = dataRequest.getParameterGroup("dmParam");
		String param1 = AESCryptUtil.decrypt(AESCryptUtil.SALT, AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, 
				dmParam.getValue("param1"), AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE);
		String param2 =  AESCryptUtil.decrypt(AESCryptUtil.SALT, AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, 
				dmParam.getValue("param2"), AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE);
		
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("클라이언트단에서 암호화한 dmParam의 데이터를 서버단에서 복호화한 데이터");
		System.out.println("param1 = "+ param1 + ", param2 = " + param2);
		System.out.println("----------------------------------------------------------------------------------------------------");
		
		//서버단에서 임의의 데이터(조회 데이터 가정)를 클라이언트로 전달
		List<Map<String, String>> list = setData();
		for (Map<String, String> map : list) {
			for (String key : map.keySet()) {
				//넘버, 성별, 이름을 제외 한 데이터 암호화
				if(!"SQNO".equals(key) && !"SEX_DIS".equals(key) && !"FNM".equals(key)) {
					map.put(key, AESCryptUtil.encrypt(AESCryptUtil.SALT, AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, 
							map.get(key), AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE));
				}
			}
		}
		
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("서버단에서 암호화 전 dsList의 데이터 목록");
		System.out.println(list);
		System.out.println("----------------------------------------------------------------------------------------------------");
		dataRequest.setResponse("dsList", list);
		return new JSONDataView();
	}
	
	public List<Map<String, String>> setData() throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>(); 
        Map<String, String> map1 = new HashMap<String, String>();
        map1.put("SEX_DIS", "1");
        map1.put("FNM", "정유령");
        map1.put("FRNR_REGC_NO", "8508096666666");
        map1.put("BZNO", "4850200328");
        map1.put("RRNO", "0606124444444");
        map1.put("PSPNO", "M35580327");
        map1.put("DRVG_LCN_NO", "2416321391");
        map1.put("ACNO", "514625240368");
        map1.put("EMAIL", "MqtVhv@nate.com");
        map1.put("CDNO", "4972057055016477");
        map1.put("CNAD_NO", "01076738507");
        map1.put("ADR", "서울특별시 강남구 압구정로 301");
        map1.put("ENG_NM", "NELSON BELL");
        map1.put("CAD_RRNO", "4972057055016477");
        list.add(map1);
        
        Map<String, String> map2 = new HashMap<String, String>();
        map2.put("SEX_DIS", "1");
        map2.put("FNM", "춘주소");
        map2.put("FRNR_REGC_NO", "5103026666666");
        map2.put("BZNO", "1924236257");
        map2.put("RRNO", "6504181111111");
        map2.put("PSPNO", "T79475027");
        map2.put("DRVG_LCN_NO", "2644624634");
        map2.put("ACNO", "192272480782");
        map2.put("EMAIL", "wtjawS@gmail.com");
        map2.put("CDNO", "9865322920270436");
        map2.put("CNAD_NO", "01053196754");
        map2.put("ADR", "서울특별시 강남구 봉은사로 72");
        map2.put("ENG_NM", "DECLAN GRIFFIN");
        map2.put("CAD_RRNO", "1924236257");
        list.add(map2);
        
        Map<String, String> map3 = new HashMap<String, String>();
        map3.put("SEX_DIS", "2");
        map3.put("FNM", "공근당");
        map3.put("FRNR_REGC_NO", "9212305555555");
        map3.put("BZNO", "0717356083");
        map3.put("RRNO", "9712071111111");
        map3.put("PSPNO", "T99941385");
        map3.put("DRVG_LCN_NO", "0542022298");
        map3.put("ACNO", "176004353824");
        map3.put("EMAIL", "wdAJmp@nate.com");
        map3.put("CDNO", "4697054936351133");
        map3.put("CNAD_NO", "01033159193");
        map3.put("ADR", "서울특별시 강남구 선릉로 148");
        map3.put("ENG_NM", "HAYDEN WALKER");
        map3.put("CAD_RRNO", "4697054936351133");
        list.add(map3);
		return list;
	}
	@RequestMapping("/csrf.do")
	public View csrfCheck(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		String token = request.getHeader("X-Csrf-Token");
		String sessionToken = "";
		HttpSession session = request.getSession(false);
		if(session != null) {
			sessionToken = session.getAttribute("CSRF_TOKEN").toString();
		}
		
		if("".equals(sessionToken)) {
			response.setStatus(500);
		}
		else if(!token.equals(sessionToken)) {
			
			response.setStatus(500);
		}
		return new JSONDataView();
	}
}
