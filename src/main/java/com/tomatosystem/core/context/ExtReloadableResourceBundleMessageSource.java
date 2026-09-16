package com.tomatosystem.core.context;

import java.util.Locale;
import java.util.Properties;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * 어플리케이션에서 message resource의 모든 프러퍼티를 가져오기 위해 ReloadableResourceBundleMessageSource 재정의
 *
 */
public class ExtReloadableResourceBundleMessageSource extends ReloadableResourceBundleMessageSource{
	
	  public Properties getMessages(Locale locale) {
	        return getMergedProperties(locale).getProperties();
	    }
	  
}
