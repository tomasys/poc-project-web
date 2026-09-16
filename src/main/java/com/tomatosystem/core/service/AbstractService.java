package com.tomatosystem.core.service;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tomatosystem.core.mapper.SqlMapper;

public abstract class AbstractService {
	
	protected Logger logger = LoggerFactory.getLogger(AbstractService.class);
	
	@Resource(name = "defalut.sqlMapper")
    protected SqlMapper dao;
}
