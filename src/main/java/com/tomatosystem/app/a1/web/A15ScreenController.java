package com.tomatosystem.app.a1.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.cleopatra.protocol.data.DataRequest;

@Controller
@RequestMapping("/a15Scrren")
public class A15ScreenController {
	public A15ScreenController() {}
	
	@RequestMapping("/json.do")
	public void json(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
	}
	
	@RequestMapping("/jsonToPayload.do")
	public void jsonToPayload(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
	}
}
