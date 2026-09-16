package com.tomatosystem.app.sample.web;

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
import com.tomatosystem.app.sample.service.CmnCodeService;
import com.tomatosystem.app.sample.service.TwoGridService;

	
@Controller
@RequestMapping("/TwoGrid")
public class TwoGridController {
	
	@Autowired
	private TwoGridService tstGridGridService;
	
	@Autowired
	private CmnCodeService cmnCodeService;
	/**
	 * 메소드명	: onLoad
	 * 설	 명	: 공통코드 및 화면초기화에 필요한 데이터 반환
	 *
	 * 이력사항
	 * 2018. 8. 16. 
	 *
	 * @param request
	 * @param response
	 * @param requestData
	 * @param authentication
	 * @return
	 * @throws Exception
	 */
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
		//등록분류([TMP006])
		dataRequest.setResponse("dsRegClsRcd", cmnCodeService.selectCmnCodeList("TMP006"));
		//분납상태([TMP007])
		dataRequest.setResponse("dsDivPayStatRcd", cmnCodeService.selectCmnCodeList("TMP007"));
		//학기코드([TMP008])
		dataRequest.setResponse("dsSmtRcd", cmnCodeService.selectCmnCodeList("TMP008"));
		return new JSONDataView();
	}
	
	@RequestMapping("/listMst.do")
	public View listMst(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		//요청 파라메터 셋팅
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		Map<String, String> mapParam = new HashMap<String, String>();
		mapParam.put("STUD_NO", param.getValue("strStudNo"));
		List<Map<String, Object>> listCmnTmpReg = tstGridGridService.selectCmnTmpRegList(mapParam);
		dataRequest.setResponse("dsMst", listCmnTmpReg);
		return new JSONDataView();
	}
	
	@RequestMapping("/listDtl.do")
	public View listDtl(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		//요청 파라메터 셋팅
		ParameterGroup param = dataRequest.getParameterGroup("dmParam");
		Map<String, String> mapParam = new HashMap<String, String>();
		mapParam.put("STUD_NO", param.getValue("strMstStudNo"));
		List<Map<String, Object>> listCmnTmpReg = tstGridGridService.selectCmnTmpRegFeeList(mapParam);
		dataRequest.setResponse("dsDetail", listCmnTmpReg);
		return new JSONDataView();
	}
	
	@RequestMapping("/saveMst.do")
	public View saveMst(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		tstGridGridService.saveCmnTmpReg(dataRequest);
		
		return new JSONDataView();
	}
	
	@RequestMapping("/saveDtl.do")
	public View saveDtl(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		tstGridGridService.saveCmnTmpRegFee(dataRequest);
		
		return new JSONDataView();
	}
}
