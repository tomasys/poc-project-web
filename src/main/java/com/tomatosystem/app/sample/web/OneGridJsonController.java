package com.tomatosystem.app.sample.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;

/**
 * 
 * eXbuilder6 UI Adaptor 사용 없이 json 통신 예시
 * @RequestBody Map형식의 json 데이터 취득
 * eXbuilder6에서 미디어타입을 application/json으로 변경해야함.
 * 
 * @author Park. Ju wan
 *
 */
@Controller
@RequestMapping("/OneGridJson")
public class OneGridJsonController {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
		
	@RequestMapping("/jsonlist.do")
	public ModelAndView jsonlist(@RequestBody Map<String, Object> dataMap, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {
		// 요청 파라메터 셋팅
		Map<String, Object> mapParam = (Map) dataMap.get("dmParam");
		int num = Integer.parseInt((String) mapParam.get("grdCount"));
		
		List<Map<String, Object>> listData = new ArrayList();	
		for (int i = 0; i < num; i++) {
			Map<String, Object> row = new HashMap();
			row.put("column1", "홍길동" + i);
			row.put("column2", "010" + RandomStringUtils.randomNumeric(8));
			row.put("column3", RandomStringUtils.randomAlphabetic(10));
			row.put("column4", randomBirth());
			row.put("column5", "토마토시스템" + i);
			listData.add(row);
		}
		ModelAndView mv = new ModelAndView("jsonView");
		mv.addObject("dsList", listData);
		return mv;
	}
	
	@RequestMapping("/defaultlist.do")
	public View defaultlist(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws Exception {
		// 요청 파라메터 셋팅
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		int num = Integer.parseInt(param.getValue("grdCount"));
		
		List<Map<String, Object>> listData = new ArrayList();	
		for (int i = 0; i < num; i++) {
			Map<String, Object> row = new HashMap();
			row.put("column1", "홍길동" + i);
			row.put("column2", "010" + RandomStringUtils.randomNumeric(8));
			row.put("column3", RandomStringUtils.randomAlphabetic(10));
			row.put("column4", randomBirth());
			row.put("column5", "토마토시스템" + i);
			listData.add(row);
		}
		dataRequest.setResponse("dsList", listData);
		return new JSONDataView();
	}
	
	@RequestMapping("/save.do")
	public ModelAndView save(@RequestBody Map<String, Object> dataMap, HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		
		// 그리드 파라미터
		List<Map<String, Object>> gridData = (List) dataMap.get("dsCmnTmpReg");

		for (int i = 0; i < gridData.size(); i++) {

			Map map = gridData.get(i);
			logger.debug("GRID-[" + i + "] 학번: " + map.get("column1"));
			logger.debug("GRID-[" + i + "] 학번: " + map.get("column2"));
			logger.debug("GRID-[" + i + "] 학번: " + map.get("column3"));
			logger.debug("GRID-[" + i + "] 학번: " + map.get("column4"));
			logger.debug("GRID-[" + i + "] 학번: " + map.get("column5"));
		}
		
		return new ModelAndView("jsonView");
	}
	
	public static String randomBirth() {
		Random random = new Random();
		int minDay = (int) LocalDate.of(1970, 1, 1).toEpochDay();
		int maxDay = (int) LocalDate.of(2022, 10, 1).toEpochDay();
		long randomDay = minDay + random.nextInt(maxDay - minDay);

		LocalDate randomBirthDate = LocalDate.ofEpochDay(randomDay);
		return randomBirthDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
