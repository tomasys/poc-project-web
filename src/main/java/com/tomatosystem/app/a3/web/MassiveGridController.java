package com.tomatosystem.app.a3.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.exbuilder.engine.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.JSONDataView;
import com.tomatosystem.core.profiler.UtilProfilingStack;
import com.tomatosystem.core.service.MassResponseService;


@Controller
@RequestMapping("/massive")
public class MassiveGridController {

	@Autowired
	private MassResponseService massService;
	
	
	public MassiveGridController() {
	}
	
	@RequestMapping("/massiveGridStream.do")
	public void massiveGridStream(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		
		ParameterGroup dmReq = dataRequest.getParameterGroup("dmReq");
		
		String returnType = StringUtil.nullToEmpty(dmReq.getValue("type"));
		
		if(returnType != null) {
			if(returnType.equals("tsv")) {
				// TSV 방식
				massService.selectDataToTSVStream(request, response, "grid-tmp01.getDataList", dmReq.get(0).toMap());
			}else if(returnType.equals("json_rowHandler")) {
				// JSON 방식
				massService.selectDataToJSONStream(request, response, "dsGrid", "grid-tmp01.getDataList", dmReq.get(0).toMap()); 
			}
		}else {
			massService.selectDataToTSVStream(request, response, "grid-tmp01.getDataList", dmReq.get(0).toMap());
		}
	}
	
	@RequestMapping("/listGrid.do")
	public View massiveGrid(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		ParameterGroup dmReq = dataRequest.getParameterGroup("dmReq");
		List listData = massService.selectData(request, response, "grid-tmp01.getDataList", dmReq.get(0).toMap());
		dataRequest.setResponse("dsGrid", listData);
		return new JSONDataView();
	}
	@RequestMapping("/massiveGridStreamExport.do")
	public void massiveGridStreamExport(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
		
		ParameterGroup dmReq = dataRequest.getParameterGroup("dmReq");
		
		String returnType = dmReq.getValue("type");
		
		UtilProfilingStack.push("massive" + " : " + returnType);
		
		if(returnType.contentEquals("tsv")) {
			massService.exportExcelToStream(request, response, "excelExport", getHeader(), "tgrd.getDataList", dmReq.get(0).toMap());
		}else if(returnType.contentEquals("csv")) {
			massService.exportExcelToStreamCsv(request, response, "excelExport", getHeader(), "tgrd.getDataList", dmReq.get(0).toMap());
		}
		
		UtilProfilingStack.pop("massive-export");
		
	}
	
	
	public List<Map<String,Object>> getData(int count){
		List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();
		
		for(int i = 0; i < count; i++) {
			Map<String, Object> rowData = new HashMap<String, Object>();
			for(int j= 1; j <= 200; j++) {
				rowData.put("column"+j, getRandomNum(5));
			}
			listData.add(rowData);
		}
		
		
		return listData;
	}
	
	/**
	 * 
	 * @param length
	 * @return
	 */
	private int getRandomNum(int length) {
		
		String gValue = RandomStringUtils.randomNumeric(length);
		
		return Integer.valueOf(gValue);
	}
	
	
	public LinkedHashMap<String, Object> getHeader() {
		LinkedHashMap<String, Object> mapHeader = new LinkedHashMap<>();
		mapHeader.put("PROJ_CD","PROJ_CD");
		mapHeader.put("SYSTEM_CD","SYSTEM_CD");
		mapHeader.put("DOC_CATEGORY","DOC_CATEGORY");
		mapHeader.put("DOC_TYPE","DOC_TYPE");
		mapHeader.put("DOC_NO","DOC_NO");
		mapHeader.put("SHEET_NO","SHEET_NO");
		mapHeader.put("REV_NO","REV_NO");
		mapHeader.put("ISSUE_SEQ_NO","ISSUE_SEQ_NO");
		mapHeader.put("FILE_SEQ","FILE_SEQ");
		mapHeader.put("DOWN_FILE_NAME","DOWN_FILE_NAME");
		mapHeader.put("RESP_DIV","RESP_DIV");
		mapHeader.put("RESP_GROUP","RESP_GROUP");
		mapHeader.put("DOWN_LOADER","DOWN_LOADER");
		mapHeader.put("DOWN_LOADER_NAME","DOWN_LOADER_NAME");
		mapHeader.put("DOWN_LOAD_DATE","DOWN_LOAD_DATE");
		mapHeader.put("DOWN_PC_NAME","DOWN_PC_NAME");
		mapHeader.put("DOWN_PC_IP","DOWN_PC_IP");
		mapHeader.put("DOWN_PC_MAC","DOWN_PC_MAC");
		mapHeader.put("DOWN_PC_FILE_NAME","DOWN_PC_FILE_NAME");
		mapHeader.put("REMARK","REMARK");
		mapHeader.put("DOC_GRADE","DOC_GRADE");
		mapHeader.put("FILE_SIZE","FILE_SIZE");
		mapHeader.put("PC_FILE_NAME","PC_FILE_NAME");
		mapHeader.put("SECURITY_YN","SECURITY_YN");
		mapHeader.put("PROJ_CD1","PROJ_CD1");
		mapHeader.put("SYSTEM_CD1","SYSTEM_CD1");
		mapHeader.put("DOC_CATEGORY1","DOC_CATEGORY1");
		mapHeader.put("DOC_TYPE1","DOC_TYPE1");
		mapHeader.put("DOC_NO1","DOC_NO1");
		mapHeader.put("SHEET_NO1","SHEET_NO1");
		mapHeader.put("REV_NO1","REV_NO1");
		mapHeader.put("ISSUE_SEQ_NO1","ISSUE_SEQ_NO1");
		mapHeader.put("FILE_SEQ1","FILE_SEQ1");
		mapHeader.put("DOWN_FILE_NAME1","DOWN_FILE_NAME1");
		mapHeader.put("RESP_DIV1","RESP_DIV1");
		mapHeader.put("RESP_GROUP1","RESP_GROUP1");
		mapHeader.put("DOWN_LOADER1","DOWN_LOADER1");
		mapHeader.put("DOWN_LOADER_NAME1","DOWN_LOADER_NAME1");
		mapHeader.put("DOWN_LOAD_DATE1","DOWN_LOAD_DATE1");
		mapHeader.put("DOWN_PC_NAME1","DOWN_PC_NAME1");
		mapHeader.put("DOWN_PC_IP1","DOWN_PC_IP1");
		mapHeader.put("DOWN_PC_MAC1","DOWN_PC_MAC1");
		mapHeader.put("DOWN_PC_FILE_NAME1","DOWN_PC_FILE_NAME1");
		mapHeader.put("REMARK1","REMARK1");
		mapHeader.put("DOC_GRADE1","DOC_GRADE1");
		mapHeader.put("FILE_SIZE1","FILE_SIZE1");
		mapHeader.put("PC_FILE_NAME1","PC_FILE_NAME1");
		mapHeader.put("SECURITY_YN1","SECURITY_YN1");
		mapHeader.put("PROJ_CD2","PROJ_CD2");
		mapHeader.put("SYSTEM_CD2","SYSTEM_CD2");
		mapHeader.put("DOC_CATEGORY2","DOC_CATEGORY2");
		mapHeader.put("DOC_TYPE2","DOC_TYPE2");
		mapHeader.put("DOC_NO2","DOC_NO2");
		mapHeader.put("SHEET_NO2","SHEET_NO2");
		mapHeader.put("REV_NO2","REV_NO2");
		mapHeader.put("ISSUE_SEQ_NO2","ISSUE_SEQ_NO2");
		mapHeader.put("FILE_SEQ2","FILE_SEQ2");
		mapHeader.put("DOWN_FILE_NAME2","DOWN_FILE_NAME2");
		mapHeader.put("RESP_DIV2","RESP_DIV2");
		mapHeader.put("RESP_GROUP2","RESP_GROUP2");
		mapHeader.put("DOWN_LOADER2","DOWN_LOADER2");
		mapHeader.put("DOWN_LOADER_NAME2","DOWN_LOADER_NAME2");
		mapHeader.put("DOWN_LOAD_DATE2","DOWN_LOAD_DATE2");
		mapHeader.put("DOWN_PC_NAME2","DOWN_PC_NAME2");
		mapHeader.put("DOWN_PC_IP2","DOWN_PC_IP2");
		mapHeader.put("DOWN_PC_MAC2","DOWN_PC_MAC2");
		mapHeader.put("DOWN_PC_FILE_NAME2","DOWN_PC_FILE_NAME2");
		mapHeader.put("REMARK2","REMARK2");
		mapHeader.put("DOC_GRADE2","DOC_GRADE2");
		mapHeader.put("FILE_SIZE2","FILE_SIZE2");
		mapHeader.put("PC_FILE_NAME2","PC_FILE_NAME2");
		mapHeader.put("SECURITY_YN2","SECURITY_YN2");
		mapHeader.put("PROJ_CD3","PROJ_CD3");
		mapHeader.put("SYSTEM_CD3","SYSTEM_CD3");
		mapHeader.put("DOC_CATEGORY3","DOC_CATEGORY3");
		mapHeader.put("DOC_TYPE3","DOC_TYPE3");
		mapHeader.put("DOC_NO3","DOC_NO3");
		mapHeader.put("SHEET_NO3","SHEET_NO3");
		mapHeader.put("REV_NO3","REV_NO3");
		mapHeader.put("ISSUE_SEQ_NO3","ISSUE_SEQ_NO3");
		mapHeader.put("FILE_SEQ3","FILE_SEQ3");
		mapHeader.put("DOWN_FILE_NAME3","DOWN_FILE_NAME3");
		mapHeader.put("RESP_DIV3","RESP_DIV3");
		mapHeader.put("RESP_GROUP3","RESP_GROUP3");
		mapHeader.put("DOWN_LOADER3","DOWN_LOADER3");
		mapHeader.put("DOWN_LOADER_NAME3","DOWN_LOADER_NAME3");
		mapHeader.put("DOWN_LOAD_DATE3","DOWN_LOAD_DATE3");
		mapHeader.put("DOWN_PC_NAME3","DOWN_PC_NAME3");
		mapHeader.put("DOWN_PC_IP3","DOWN_PC_IP3");
		mapHeader.put("DOWN_PC_MAC3","DOWN_PC_MAC3");
		mapHeader.put("DOWN_PC_FILE_NAME3","DOWN_PC_FILE_NAME3");
		mapHeader.put("REMARK3","REMARK3");
		mapHeader.put("DOC_GRADE3","DOC_GRADE3");
		mapHeader.put("FILE_SIZE3","FILE_SIZE3");
		mapHeader.put("PC_FILE_NAME3","PC_FILE_NAME3");
		mapHeader.put("SECURITY_YN3","SECURITY_YN3");
		mapHeader.put("PROJ_CD4","PROJ_CD4");
		mapHeader.put("SYSTEM_CD4","SYSTEM_CD4");
		mapHeader.put("DOC_CATEGORY4","DOC_CATEGORY4");
		mapHeader.put("DOC_TYPE4","DOC_TYPE4");
		mapHeader.put("DOC_NO4","DOC_NO4");
		mapHeader.put("SHEET_NO4","SHEET_NO4");
		mapHeader.put("REV_NO4","REV_NO4");
		mapHeader.put("ISSUE_SEQ_NO4","ISSUE_SEQ_NO4");
		mapHeader.put("FILE_SEQ4","FILE_SEQ4");
		mapHeader.put("DOWN_FILE_NAME4","DOWN_FILE_NAME4");
		mapHeader.put("RESP_DIV4","RESP_DIV4");
		mapHeader.put("RESP_GROUP4","RESP_GROUP4");
		mapHeader.put("DOWN_LOADER4","DOWN_LOADER4");
		mapHeader.put("DOWN_LOADER_NAME4","DOWN_LOADER_NAME4");
		mapHeader.put("DOWN_LOAD_DATE4","DOWN_LOAD_DATE4");
		mapHeader.put("DOWN_PC_NAME4","DOWN_PC_NAME4");
		mapHeader.put("DOWN_PC_IP4","DOWN_PC_IP4");
		mapHeader.put("DOWN_PC_MAC4","DOWN_PC_MAC4");
		mapHeader.put("DOWN_PC_FILE_NAME4","DOWN_PC_FILE_NAME4");
		mapHeader.put("REMARK4","REMARK4");
		mapHeader.put("DOC_GRADE4","DOC_GRADE4");
		mapHeader.put("FILE_SIZE4","FILE_SIZE4");
		mapHeader.put("PC_FILE_NAME4","PC_FILE_NAME4");
		mapHeader.put("SECURITY_YN4","SECURITY_YN4");
		mapHeader.put("PROJ_CD5","PROJ_CD5");
		mapHeader.put("SYSTEM_CD5","SYSTEM_CD5");
		mapHeader.put("DOC_CATEGORY5","DOC_CATEGORY5");
		mapHeader.put("DOC_TYPE5","DOC_TYPE5");
		mapHeader.put("DOC_NO5","DOC_NO5");
		mapHeader.put("SHEET_NO5","SHEET_NO5");
		mapHeader.put("REV_NO5","REV_NO5");
		mapHeader.put("ISSUE_SEQ_NO5","ISSUE_SEQ_NO5");
		mapHeader.put("FILE_SEQ5","FILE_SEQ5");
		mapHeader.put("DOWN_FILE_NAME5","DOWN_FILE_NAME5");
		mapHeader.put("RESP_DIV5","RESP_DIV5");
		mapHeader.put("RESP_GROUP5","RESP_GROUP5");
		mapHeader.put("DOWN_LOADER5","DOWN_LOADER5");
		mapHeader.put("DOWN_LOADER_NAME5","DOWN_LOADER_NAME5");
		mapHeader.put("DOWN_LOAD_DATE5","DOWN_LOAD_DATE5");
		mapHeader.put("DOWN_PC_NAME5","DOWN_PC_NAME5");
		mapHeader.put("DOWN_PC_IP5","DOWN_PC_IP5");
		mapHeader.put("DOWN_PC_MAC5","DOWN_PC_MAC5");
		mapHeader.put("DOWN_PC_FILE_NAME5","DOWN_PC_FILE_NAME5");
		mapHeader.put("REMARK5","REMARK5");
		mapHeader.put("DOC_GRADE5","DOC_GRADE5");
		mapHeader.put("FILE_SIZE5","FILE_SIZE5");
		mapHeader.put("PC_FILE_NAME5","PC_FILE_NAME5");
		mapHeader.put("SECURITY_YN5","SECURITY_YN5");
		mapHeader.put("PROJ_CD6","PROJ_CD6");
		mapHeader.put("SYSTEM_CD6","SYSTEM_CD6");
		mapHeader.put("DOC_CATEGORY6","DOC_CATEGORY6");
		mapHeader.put("DOC_TYPE6","DOC_TYPE6");
		mapHeader.put("DOC_NO6","DOC_NO6");
		mapHeader.put("SHEET_NO6","SHEET_NO6");
		mapHeader.put("REV_NO6","REV_NO6");
		mapHeader.put("ISSUE_SEQ_NO6","ISSUE_SEQ_NO6");
		mapHeader.put("FILE_SEQ6","FILE_SEQ6");
		mapHeader.put("DOWN_FILE_NAME6","DOWN_FILE_NAME6");
		mapHeader.put("RESP_DIV6","RESP_DIV6");
		mapHeader.put("RESP_GROUP6","RESP_GROUP6");
		mapHeader.put("DOWN_LOADER6","DOWN_LOADER6");
		mapHeader.put("DOWN_LOADER_NAME6","DOWN_LOADER_NAME6");
		mapHeader.put("DOWN_LOAD_DATE6","DOWN_LOAD_DATE6");
		mapHeader.put("DOWN_PC_NAME6","DOWN_PC_NAME6");
		mapHeader.put("DOWN_PC_IP6","DOWN_PC_IP6");
		mapHeader.put("DOWN_PC_MAC6","DOWN_PC_MAC6");
		mapHeader.put("DOWN_PC_FILE_NAME6","DOWN_PC_FILE_NAME6");
		mapHeader.put("REMARK6","REMARK6");
		mapHeader.put("DOC_GRADE6","DOC_GRADE6");
		mapHeader.put("FILE_SIZE6","FILE_SIZE6");
		mapHeader.put("PC_FILE_NAME6","PC_FILE_NAME6");
		mapHeader.put("SECURITY_YN6","SECURITY_YN6");
		mapHeader.put("PROJ_CD7","PROJ_CD7");
		mapHeader.put("SYSTEM_CD7","SYSTEM_CD7");
		mapHeader.put("DOC_CATEGORY7","DOC_CATEGORY7");
		mapHeader.put("DOC_TYPE7","DOC_TYPE7");
		mapHeader.put("DOC_NO7","DOC_NO7");
		mapHeader.put("SHEET_NO7","SHEET_NO7");
		mapHeader.put("REV_NO7","REV_NO7");
		mapHeader.put("ISSUE_SEQ_NO7","ISSUE_SEQ_NO7");
		mapHeader.put("FILE_SEQ7","FILE_SEQ7");
		mapHeader.put("DOWN_FILE_NAME7","DOWN_FILE_NAME7");
		mapHeader.put("RESP_DIV7","RESP_DIV7");
		mapHeader.put("RESP_GROUP7","RESP_GROUP7");
		mapHeader.put("DOWN_LOADER7","DOWN_LOADER7");
		mapHeader.put("DOWN_LOADER_NAME7","DOWN_LOADER_NAME7");
		mapHeader.put("DOWN_LOAD_DATE7","DOWN_LOAD_DATE7");
		mapHeader.put("DOWN_PC_NAME7","DOWN_PC_NAME7");
		mapHeader.put("DOWN_PC_IP7","DOWN_PC_IP7");
		mapHeader.put("DOWN_PC_MAC7","DOWN_PC_MAC7");
		mapHeader.put("DOWN_PC_FILE_NAME7","DOWN_PC_FILE_NAME7");
		mapHeader.put("REMARK7","REMARK7");
		mapHeader.put("DOC_GRADE7","DOC_GRADE7");
		mapHeader.put("FILE_SIZE7","FILE_SIZE7");
		mapHeader.put("PC_FILE_NAME7","PC_FILE_NAME7");
		mapHeader.put("SECURITY_YN7","SECURITY_YN7");
		mapHeader.put("PROJ_CD8","PROJ_CD8");
		mapHeader.put("SYSTEM_CD8","SYSTEM_CD8");
		mapHeader.put("DOC_CATEGORY8","DOC_CATEGORY8");
		mapHeader.put("DOC_TYPE8","DOC_TYPE8");
		mapHeader.put("DOC_NO8","DOC_NO8");
		mapHeader.put("SHEET_NO8","SHEET_NO8");
		mapHeader.put("REV_NO8","REV_NO8");
		mapHeader.put("ISSUE_SEQ_NO8","ISSUE_SEQ_NO8");
		
		return mapHeader;
		
	}
	public static void main(String[] arg) {
		
		String strHeader = "PROJ_CD		SYSTEM_CD	DOC_CATEGORY	DOC_TYPE	DOC_NO	SHEET_NO	REV_NO	ISSUE_SEQ_NO	FILE_SEQ	DOWN_FILE_NAME	RESP_DIV	RESP_GROUP	DOWN_LOADER	DOWN_LOADER_NAME	DOWN_LOAD_DATE	DOWN_PC_NAME	DOWN_PC_IP	DOWN_PC_MAC	DOWN_PC_FILE_NAME	REMARK	DOC_GRADE	FILE_SIZE	PC_FILE_NAME	SECURITY_YN	PROJ_CD1	SYSTEM_CD1	DOC_CATEGORY1	DOC_TYPE1	DOC_NO1	SHEET_NO1	REV_NO1	ISSUE_SEQ_NO1	FILE_SEQ1	DOWN_FILE_NAME1	RESP_DIV1	RESP_GROUP1	DOWN_LOADER1	DOWN_LOADER_NAME1	DOWN_LOAD_DATE1	DOWN_PC_NAME1	DOWN_PC_IP1	DOWN_PC_MAC1	DOWN_PC_FILE_NAME1	REMARK1	DOC_GRADE1	FILE_SIZE1	PC_FILE_NAME1	SECURITY_YN1	PROJ_CD2	SYSTEM_CD2	DOC_CATEGORY2	DOC_TYPE2	DOC_NO2	SHEET_NO2	REV_NO2	ISSUE_SEQ_NO2	FILE_SEQ2	DOWN_FILE_NAME2	RESP_DIV2	RESP_GROUP2	DOWN_LOADER2	DOWN_LOADER_NAME2	DOWN_LOAD_DATE2	DOWN_PC_NAME2	DOWN_PC_IP2	DOWN_PC_MAC2	DOWN_PC_FILE_NAME2	REMARK2	DOC_GRADE2	FILE_SIZE2	PC_FILE_NAME2	SECURITY_YN2	PROJ_CD3	SYSTEM_CD3	DOC_CATEGORY3	DOC_TYPE3	DOC_NO3	SHEET_NO3	REV_NO3	ISSUE_SEQ_NO3	FILE_SEQ3	DOWN_FILE_NAME3	RESP_DIV3	RESP_GROUP3	DOWN_LOADER3	DOWN_LOADER_NAME3	DOWN_LOAD_DATE3	DOWN_PC_NAME3	DOWN_PC_IP3	DOWN_PC_MAC3	DOWN_PC_FILE_NAME3	REMARK3	DOC_GRADE3	FILE_SIZE3	PC_FILE_NAME3	SECURITY_YN3	PROJ_CD4	SYSTEM_CD4	DOC_CATEGORY4	DOC_TYPE4	DOC_NO4	SHEET_NO4	REV_NO4	ISSUE_SEQ_NO4	FILE_SEQ4	DOWN_FILE_NAME4	RESP_DIV4	RESP_GROUP4	DOWN_LOADER4	DOWN_LOADER_NAME4	DOWN_LOAD_DATE4	DOWN_PC_NAME4	DOWN_PC_IP4	DOWN_PC_MAC4	DOWN_PC_FILE_NAME4	REMARK4	DOC_GRADE4	FILE_SIZE4	PC_FILE_NAME4	SECURITY_YN4	PROJ_CD5	SYSTEM_CD5	DOC_CATEGORY5	DOC_TYPE5	DOC_NO5	SHEET_NO5	REV_NO5	ISSUE_SEQ_NO5	FILE_SEQ5	DOWN_FILE_NAME5	RESP_DIV5	RESP_GROUP5	DOWN_LOADER5	DOWN_LOADER_NAME5	DOWN_LOAD_DATE5	DOWN_PC_NAME5	DOWN_PC_IP5	DOWN_PC_MAC5	DOWN_PC_FILE_NAME5	REMARK5	DOC_GRADE5	FILE_SIZE5	PC_FILE_NAME5	SECURITY_YN5	PROJ_CD6	SYSTEM_CD6	DOC_CATEGORY6	DOC_TYPE6	DOC_NO6	SHEET_NO6	REV_NO6	ISSUE_SEQ_NO6	FILE_SEQ6	DOWN_FILE_NAME6	RESP_DIV6	RESP_GROUP6	DOWN_LOADER6	DOWN_LOADER_NAME6	DOWN_LOAD_DATE6	DOWN_PC_NAME6	DOWN_PC_IP6	DOWN_PC_MAC6	DOWN_PC_FILE_NAME6	REMARK6	DOC_GRADE6	FILE_SIZE6	PC_FILE_NAME6	SECURITY_YN6	PROJ_CD7	SYSTEM_CD7	DOC_CATEGORY7	DOC_TYPE7	DOC_NO7	SHEET_NO7	REV_NO7	ISSUE_SEQ_NO7	FILE_SEQ7	DOWN_FILE_NAME7	RESP_DIV7	RESP_GROUP7	DOWN_LOADER7	DOWN_LOADER_NAME7	DOWN_LOAD_DATE7	DOWN_PC_NAME7	DOWN_PC_IP7	DOWN_PC_MAC7	DOWN_PC_FILE_NAME7	REMARK7	DOC_GRADE7	FILE_SIZE7	PC_FILE_NAME7	SECURITY_YN7	PROJ_CD8	SYSTEM_CD8	DOC_CATEGORY8	DOC_TYPE8	DOC_NO8	SHEET_NO8	REV_NO8	ISSUE_SEQ_NO8";
		
		String [] arr = strHeader.split("\t");
		
		StringBuffer sb = new StringBuffer();
		
		
		
		for (String h : arr) {
			
			sb.append("mapHeader.put(")
			.append("\"")
			.append(h)
			.append("\"")
			.append(",")
			.append("\"")
			.append(h)
			.append("\"")
			.append(");")
			.append("\n");
		}
		
		System.out.println(sb.toString());
	}
}
