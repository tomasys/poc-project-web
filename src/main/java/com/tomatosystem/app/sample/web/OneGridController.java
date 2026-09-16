package com.tomatosystem.app.sample.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.cleopatra.spring.TSVDataView;
import com.tomatosystem.app.sample.service.CmnCodeService;
import com.tomatosystem.app.sample.service.OneGridService;

	
@Controller
@RequestMapping("/OneGrid")
public class OneGridController {
	
	@Autowired
	private OneGridService tstGridService;
	
	@Autowired
	private CmnCodeService cmnCodeService;
	
	@RequestMapping("/onLoad.do")
	public View onLoad(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		//성별코드([TMP001])
		dataRequest.setResponse("dsGenderRcd", cmnCodeService.selectCmnCodeList("TMP001"));
		//학생구분코드([TMP002])
		dataRequest.setResponse("dsStudDivRcd", cmnCodeService.selectCmnCodeList("TMP002"));
		//주야간코드([TMP003])
		dataRequest.setResponse("dsDayNightDivRcd", cmnCodeService.selectCmnCodeList("TMP003"));
		//국가코드([TMP004])
		dataRequest.setResponse("dsNatRcd", cmnCodeService.selectCmnCodeList("TMP004"));
		//은행코드([TMP005])
		dataRequest.setResponse("dsBankRcd", cmnCodeService.selectCmnCodeList("TMP005"));
		return new JSONDataView();
	}
	
	@RequestMapping("/list.do")
	public View list(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("STUD_NO", param.getValue("strStudNo"));
		List<Map<String, Object>> listCmnTmpReg = tstGridService.selectCmnTmpRegList(mapParam);
		dataRequest.setResponse("dsCmnTmpReg", listCmnTmpReg);
		return new JSONDataView();
	}
	
	/**
	 * 
	 * <pre>
	 * 메소드명	: listTsvRh
	 * 설	 명	: TSV + mybatis  ResultHandler(row)
	 * </pre>
	 *
	 * 이력사항
	 * 2021. 8. 12. Park. ju wan 최초작성
	 *
	 * @param request
	 * @param response
	 * @param dataRequest
	 * @throws Exception
	 */
	@RequestMapping("/listTsvRh.do")
	public void listTsvRh(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("STUD_NO", param.getValue("strStudNo"));
		tstGridService.selectCmnTmpRegRowHandler(mapParam, response);
	}
	
	@RequestMapping("/listTsv.do")
	public View listTsv(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("STUD_NO", param.getValue("strStudNo"));
		List<Map<String, Object>> listCmnTmpReg = tstGridService.selectCmnTmpRegList(mapParam);
		List list = new ArrayList();
		for(int i = 0 ; i < 10000 ; i++) {
			list.addAll(listCmnTmpReg);
		}
		dataRequest.setResponse("dsCmnTmpReg", list);
		return new TSVDataView();
	}
	
	@RequestMapping("/listTab.do")
	public View listTab(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		
		Map<String, String> mapParam = new HashMap<>();
		mapParam.put("STUD_NO", param.getValue("strStudNo"));
		List<Map<String, Object>> listCmnTmpReg = tstGridService.selectCmnTmpRegList(mapParam);
		dataRequest.setResponse("dsCmnTmpReg2", listCmnTmpReg);
		return new JSONDataView();
	}
	
	@RequestMapping("/save.do")
	public View save(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		tstGridService.saveCmnTmpReg(dataRequest);
		
		return new JSONDataView();
	}
	
	@RequestMapping("/saveTab.do")
	public View saveTab(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		tstGridService.saveCmnTmpRegTab(dataRequest);
		
		return new JSONDataView();
	}
	
	@RequestMapping("/saveFile.do")
	public View saveFile(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		tstGridService.saveCmnTmpRegWithFile(dataRequest);
		
		return new JSONDataView();
	}
}
