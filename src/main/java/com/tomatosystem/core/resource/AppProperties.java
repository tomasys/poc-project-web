package com.tomatosystem.core.resource;

import java.util.Properties;

import com.tomatosystem.core.context.StaticSpringApplicationContext;

public class AppProperties {
	
	public static String getProperty(String key) {
		Properties props = (Properties) StaticSpringApplicationContext.getBean("appProperties", Properties.class);
		return props == null ? "" : props.getProperty(key);
	}

	public static String getProperty(String propertiesHolder, String key) {
		Properties props = (Properties) StaticSpringApplicationContext.getBean(propertiesHolder, Properties.class);
		return props == null ? "" : props.getProperty(key);
	}
	
}
