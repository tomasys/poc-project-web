package com.tomatosystem.core.context;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;

@Configuration
public class ExCFrameWebApplicationInitializer implements WebApplicationInitializer {

	    @Override
	    public void onStartup(ServletContext servletContext) throws ServletException {
	    	
	    	String strSystemProfiles = System.getProperty("spring.profiles.active");
	    	String strContextProfiles = servletContext.getInitParameter("spring.profiles.active");
	    	
	    	if(strSystemProfiles == null && strContextProfiles == null) {
	    		servletContext.setInitParameter("spring.profiles.active", "local");
	    	}
	    }
	}
