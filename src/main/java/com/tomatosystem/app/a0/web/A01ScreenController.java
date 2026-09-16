package com.tomatosystem.app.a0.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.util.AESCryptUtil;

@Controller
@RequestMapping("/a01Scrren")
public class A01ScreenController {
	public A01ScreenController() {}
	
	@RequestMapping("/list.do")
	public View list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		ParameterGroup dmParam = dataRequest.getParameterGroup("DM_PARAM");
				
		String custNo = AESCryptUtil.decrypt(AESCryptUtil.SALT, AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, 
				dmParam.getValue("custNo"), AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE);
						
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("클라이언트단에서 암호화한 DM_PARAM의 데이터를 서버단에서 복호화한 데이터");
		System.out.println("custNo = "+ custNo);
		System.out.println("----------------------------------------------------------------------------------------------------");
        
		//서버단에서 임의의 JSON 데이터를 클라이언트로 전달
		List<Map<String, String>> list = setData(custNo);
		System.out.println("----------------------------------------------------------------------------------------------------");
		System.out.println("서버단에서 암호화 전 dsList의 데이터 목록");
		System.out.println(list);
		System.out.println("----------------------------------------------------------------------------------------------------");
		
		dataRequest.setResponse("DS_CUST", list);
		return new JSONDataView();
	}
	
	public List<Map<String, String>> setData(String custNo) throws Exception {
		List<Map<String, String>> list = new ArrayList<Map<String,String>>();
        Map<String, String> map1 = new HashMap<String, String>();
        if("1".equals(custNo)) {
        	map1.put("custNo", "1");
            map1.put("korNm", "홍길동");
            map1.put("engNm", "Hong Gil dong");
            String jumin = AESCryptUtil.encrypt(AESCryptUtil.SALT, AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, 
            				"8001011234567", AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE);
            map1.put("jumin", jumin);
            map1.put("job", "value2");
            map1.put("age", "32");
            map1.put("drvYn", "Y");
            map1.put("birth", "19800101");
            map1.put("nation", "1");
        } else if("2".equals(custNo)){
        	map1.put("custNo", "2");
            map1.put("korNm", "토마토");
            map1.put("engNm", "Tomato");
            String jumin = AESCryptUtil.encrypt(AESCryptUtil.SALT, AESCryptUtil.IV, AESCryptUtil.PASSPHRASE, 
            		"7001011234567", AESCryptUtil.ITERATION_COUNT, AESCryptUtil.KEYSIZE);
            map1.put("jumin", jumin);
            map1.put("job", "value1");
            map1.put("age", "42");
            map1.put("drvYn", "N");
            map1.put("birth", "19700228");
            map1.put("nation", "1");
		}        
        list.add(map1);

		return list;
	}	
}
