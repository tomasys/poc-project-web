package com.tomatosystem.core.error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.spring.UIView;

@Controller
@RequestMapping("/error")
public class ErrorController {
	
	
	@RequestMapping("/404.do")
	public View error404(HttpServletRequest request, HttpServletResponse response) throws Exception {
		return new UIView("/ui/app/com/error/Error404.clx");
	}
}
