 package com.tomatosystem.app.sample.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.tomatosystem.core.service.AbstractService;

@Service
public class CmnCodeService extends AbstractService {	
	public List<Map<String, Object>> selectCmnCodeList(String strCdCls ) throws Exception {
		Map<String, Object> mapParam = new HashMap<String, Object>();
		mapParam.put("CD_CLS", strCdCls);
		mapParam.put("USE_YN", "Y");
		return dao.selectList("cmn-base01.selectCmnCodeList", mapParam);
	}
	
}