package com.tomatosystem.core.mapper.configurer;

import org.mybatis.spring.mapper.MapperScannerConfigurer;


public class MapperConfigurer extends MapperScannerConfigurer {

	public MapperConfigurer() {
		super.setAnnotationClass(Mapper.class);
		super.setSqlSessionFactoryBeanName("sqlSession");
	}
}
