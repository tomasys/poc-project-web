package com.tomatosystem.exbuilder.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.cleopatra.json.JSONObject;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.spring.TSVDataView;
import com.tomatosystem.core.massive.MassiveExcelReader;
import com.tomatosystem.core.massive.StreamExcelResponseAdapter;
import com.tomatosystem.core.massive.StreamExcelResponseAdapter2;
import com.tomatosystem.core.service.CmnExcelImportTmpService;
import com.tomatosystem.core.util.StringUtil;
import com.tomatosystem.core.util.UtilUuidMgr;



@Controller
@RequestMapping("/Massive")
public class CleopatraMassiveFileController {
	
	public CleopatraMassiveFileController() {
	}

	@RequestMapping("/massiveFileTsv.do")
	public void importMassiveFileTsv(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest) throws Exception {
				
		// 헤더 라인 수 (default:1)
		String headerLine = dataRequest.getParameter("headerLine");
		if(headerLine == null || "".equals(headerLine)) {
			headerLine = "1";
		}
		int numHeaderLine = Integer.parseInt(headerLine);
		
		//
		Map<String, Object> requestParamMap = new HashMap<>();
		
	    // ExcelVO 사용 (true) 또는 Map 사용 (false)
		requestParamMap.put("useExcelVO", false); 

//		// 업로드 할 데이터셋 ID
//		String strDataset = dataRequest.getParameter("datasetId");
//		if(strDataset == null || "".equals(strDataset)) {
//			strDataset = "dsExcel";
//			requestParamMap.put("strDataset", "dsExcel");  
//		}
		
		MassiveExcelReader massiveExcel = new MassiveExcelReader(numHeaderLine, new CmnExcelImportTmpService(), requestParamMap, response, dataRequest );
		massiveExcel.readExcel(dataRequest);
		
	}
	
	@RequestMapping("/massiveList.do")
	public View getListMassiveData(HttpServletRequest request, HttpServletResponse response, DataRequest dataRequest)
			throws IOException {

		ParameterGroup param = dataRequest.getParameterGroup("dmParam");

		String rowCount = param.getValue("rowCount");

		List<Map<String, Object>> listData = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < Integer.parseInt(rowCount); i++) {

			Map<String, Object> rowData = new HashMap<String, Object>();

			for (int j = 0; j < 10; j++) {
				if (j == 2) {
					rowData.put("column" + (j + 1), (i+1));
				} else {
					rowData.put("column" + (j + 1), RandomStringUtils.randomAlphabetic(5));
				}
			}
			listData.add(rowData);
		}
		dataRequest.setResponse("dsList", listData);
//		 final DataResponse dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
//		 ResponseModel responseModel = new ResponseModel();
//		 responseModel.setResponse("dsList", listData);
//		 dataResponse.send(responseModel);
//		 dataResponse.flush();		 
		// TSVDataView를 생성해서 내려보냅니다. 일반적으로는 JSONDataView()
		return new TSVDataView();
	}

	
	
	/**
	 * 스트림으로 엑셀 export ( 기존 export + 셀에 데이터 하는거 추가)
	 * @param request
	 * @param response
	 * @param requestData
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@RequestMapping("/excelExport.do")
	public void exportExcelData(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws IOException, NoSuchMethodException, SecurityException {

		JSONObject data = dataRequest.getRequestObject();
		
		boolean isLastSend = (boolean) data.get("isLastSend");

		HttpSession session = request.getSession();
		
		String randomUUID = data.getString("excelKey");
		
		if(StringUtil.isEmpty(randomUUID)) {
			randomUUID = UtilUuidMgr.randomUUID().toString();
		}
		// randomUUID로 관리
		StreamExcelResponseAdapter adapter = (StreamExcelResponseAdapter) session.getAttribute("export" + randomUUID);
	
		// 첫 번째 요청에서만 초기화
		if (adapter == null || data.get("isFirstSend").equals(true)) {
			//최초 처음요청에만 랜덤키를 만들어서 세팅
			adapter = new StreamExcelResponseAdapter("export" + randomUUID, data);
			session.setAttribute("export" + randomUUID, adapter);
			session.setAttribute("isHeaderSet", true);
		}
		
		adapter.addRowMassive((JSONObject) data.get("data"), "data");
		
		// 푸터가 있으면 별도로 추가 처리
		if (data.has("footer")) {
			adapter.addRowMassive((JSONObject) data.get("footer"), "footer");
		}

		// 마지막 요청이면 클라이언트에 데이터 전달
		if (isLastSend) {
			adapter.endResponseMassive(response);
			session.setAttribute("isHeaderSet", false);
			session.setAttribute("export" + randomUUID, null);
		}else {
			response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        String jsonResponse = "{\"excelKey\":\"" + randomUUID + "\", \"message\":\"데이터 처리 완료\"}";
	        response.getWriter().write(jsonResponse);
		}
	}

	/**
	 * 서버 고정 스타일 기반 스트림 엑셀 export
	 * - Adapter : StreamExcelResponseAdapter2
	 * - 경로    : /Massive/excelExportWithRowGroup.do
	 * @param request
	 * @param response
	 * @param dataRequest
	 * @throws IOException
	 */
	@RequestMapping("/excelExportWithRowGroup.do")
	public void excelExportWithRowGroup(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws IOException {

		JSONObject data = dataRequest.getRequestObject();

		boolean isLastSend  = (boolean) data.get("isLastSend");
		boolean isFirstSend = data.get("isFirstSend").equals(true);

		HttpSession session = request.getSession();

		String excelKey = data.getString("excelKey");
		if (StringUtil.isEmpty(excelKey)) {
			excelKey = UtilUuidMgr.randomUUID().toString();
		}

		StreamExcelResponseAdapter2 adapter =
			(StreamExcelResponseAdapter2) session.getAttribute("export" + excelKey);

		if (adapter == null || isFirstSend) {
			adapter = new StreamExcelResponseAdapter2("export" + excelKey, data);
			session.setAttribute("export" + excelKey, adapter);
		}

		try {
		    adapter.processData((JSONObject) data.get("data"));
		} catch (IOException e) {
		    session.removeAttribute("export" + excelKey); // ← 추가
		    throw e;
		}

		if (data.has("footer")) {
			adapter.processFooter((JSONObject) data.get("footer"));
		}

		if (isLastSend) {
			adapter.endResponseMassive(response);
			session.removeAttribute("export" + excelKey);
		} else {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(
				"{\"excelKey\":\"" + excelKey + "\", \"message\":\"데이터 처리 완료\"}"
			);
		}
	}

	/**
	 * 스트림으로 엑셀 export ( 기존 export + 셀에 데이터 하는거 추가)
	 * @param request
	 * @param response
	 * @param requestData
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	@RequestMapping("/test.do")
	public void test(HttpServletRequest request, HttpServletResponse response,
			DataRequest dataRequest) throws IOException, NoSuchMethodException, SecurityException {

		
         System.out.println("확인");
	}
	
	
}