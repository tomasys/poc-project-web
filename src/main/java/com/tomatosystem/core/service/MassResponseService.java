package com.tomatosystem.core.service;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataResponse;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.massive.MassiveCSVResultHandler;
import com.tomatosystem.core.massive.MassiveExcelResultHandler;
import com.tomatosystem.core.massive.MassiveResultHandler;
import com.tomatosystem.core.massive.MassiveTSVResultHandler;
import com.tomatosystem.core.massive.OutputResolver;
import com.tomatosystem.core.massive.ProgressExcelStore;
import com.tomatosystem.core.massive.StreamCsvResponseAdapter;
import com.tomatosystem.core.massive.StreamExcelResponseAdapter;
import com.tomatosystem.core.massive.StreamResponseAdapter;

/**
 * 
 * MassResponseService.java
 * 
 * @Description  대용량 데이터 조회시, 응답속도 및 서버 오류 방지를 위해 사용
 *                      응답데이터를 JSON 또는 TSV 포맷의 문자열로 응답스트림에 담아 반환한다.
 * @author tomatosystem
 * @since 2020. 10. 7.
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일             수정자             수정내용
 *    -------        ---------------       --------------
 *   2020. 10. 7.        tomatosystem       최초 생성
 *
 * </pre>
 */
@Service
public class MassResponseService extends AbstractService {
	
	@Autowired
	private SqlSessionTemplate sqlSession;
	
	/**
	 * 대용량 데이터를 화면단에 JSON 문자열 스트림(Stream) 형태로 Export한다.
	 * @param request
	 * @param response
	 * @param strOutDataSetId - 데이터셋명
	 * @param strSqlId - 실행 쿼리ID
	 * @param mapParam - 쿼리실행 아규먼트
	 * @throws Exception
	 */
	public void selectDataToJSONStream(HttpServletRequest request, HttpServletResponse response, String strOutDataSetId,
			String strSqlId, Map mapParam) throws Exception {
		
		StreamResponseAdapter adapter = new StreamResponseAdapter(request, response);
		try {
			//대용량 데이터 처리
			adapter.startNodes(strOutDataSetId);
			dao.selectWithResultHandler(strSqlId, mapParam, new MassiveResultHandler(adapter));
			adapter.endNodes();
			adapter.endResponse();
		} catch (DataAccessException ex){
			adapter.sendError(ex);
		} catch (Exception e){
			adapter.sendError(e);
		}
	}
	
	public List selectData(HttpServletRequest request, HttpServletResponse response, String strSqlId, Map mapParam) throws Exception {
		return dao.selectList(strSqlId, mapParam);
	}
	

	/**
	 * 대용량 데이터를 화면단에 JSON 문자열 스트림(Stream) 형태로 Export한다.
	 * 예시) List arguments = new ArrayList();
		    arguments.add(new Object[]{"dsLevalInputStatic", "ule-base04.selectLevalStatic", mapParam});
		    arguments.add(new Object[]{"dsLevalRsltData", "ule-base04.selectLevalRsltData", mapParam});
	 * @param request
	 * @param response
	 * @param arguments - 실행관련 아규먼트 목록
	 * @throws Exception
	 */
	
	public void selectDataToJSONStream(HttpServletRequest request, HttpServletResponse response, List arguments)
			throws Exception {
		
		StreamResponseAdapter adapter = new StreamResponseAdapter(request, response);
		try {
			//대용량 데이터 처리
			Object[] item;
			for(Object arg : arguments){
				item = (Object[])arg;
				adapter.startNodes((String)item[0]);
				dao.selectWithResultHandler((String)item[1], (Map)item[2], new MassiveResultHandler(adapter));
				adapter.endNodes();
			}
			adapter.endResponse();
		} catch (DataAccessException ex){
			adapter.sendError(ex);
		} catch (Exception e){
			adapter.sendError(e);
		}
	}

	/**
	 * 대용량 데이터를 화면단에 TVS 문자열 스트림(Stream) 형태로 Export한다.
	 * 해당 메소드는 하나의 응답 데이터만을 제공한다. 즉, 여러 데이터셋을 동시에 응답으로 내려받을 수 없다.
	 * @param request
	 * @param response
	 * @param strOutDataSetId - 데이터셋명
	 * @param strSqlId - 실행 쿼리ID
	 * @param mapParam - 쿼리실행 아규먼트
	 * @throws Exception
	 */
	
	public void selectDataToTSVStream(HttpServletRequest request, HttpServletResponse response, String strSqlId,
			Map mapParam) throws Exception {
		
		DataResponse dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
		try {
			//대용량 데이터 처리
			//dao.selectWithResultHandler(strSqlId, mapParam, new MassiveTSVResultHandler(dataResponse));
			dao.selectWithResultHandler(strSqlId, mapParam, new MassiveTSVResultHandler(dataResponse));
			dataResponse.flush();
		} catch (DataAccessException ex){
			this.sendErrorForTSV(response, ex);
		} catch (Exception e){
			this.sendErrorForTSV(response, e);
		}
	}

	/**
	 * 대용량 엑셀 파일을 응답 스트림에 Export하여 다운로드 한다.
	 * @param request
	 * @param response
	 * @param strFileNm
	 * @param mapHeader
	 * @param strSqlId
	 * @param mapParam
	 * @throws Exception
	 */
	
	public void exportExcelToStream(HttpServletRequest request, HttpServletResponse response, String strFileNm,
			LinkedHashMap<String, Object> mapHeader, String strSqlId, Map mapParam) throws Exception {
		
		StreamExcelResponseAdapter adapter = new StreamExcelResponseAdapter(request, response, strFileNm);
		try {
			//대용량 데이터 처리
			adapter.addHeader(mapHeader);
			dao.selectWithResultHandler(strSqlId, mapParam, new MassiveExcelResultHandler(adapter));
			adapter.endReponse();
		} catch (DataAccessException ex){
			logger.debug(ex.getMessage());
		} catch (Exception e){
			logger.debug(e.getMessage());
		}
	}
	
	/**
	 * 대용량 엑셀 파일을 응답 스트림에 Export하여 다운로드 한다.
	 * @param request
	 * @param response
	 * @param strFileNm
	 * @param mapHeader
	 * @param strSqlId
	 * @param mapParam
	 * @throws Exception
	 */
	
	public void exportExcelToStreamCsv(HttpServletRequest request, HttpServletResponse response, String strFileNm,
			LinkedHashMap<String, Object> mapHeader, String strSqlId, Map mapParam) throws Exception {
		
		
		String progressId = (String) mapParam.get("progressId");
		StreamCsvResponseAdapter adapter = new StreamCsvResponseAdapter(request, response, strFileNm);
		
		try {
			//대용량 데이터 처리
			adapter.addHeader(mapHeader);
			this.sqlSession.select(strSqlId, mapParam, new MassiveCSVResultHandler(adapter, progressId));
			adapter.endClose();
			
		} catch (DataAccessException ex){
			logger.debug(ex.getMessage());
		} catch (Exception e){
			logger.debug(e.getMessage());
		}finally {
			ProgressExcelStore.progressMap.remove(progressId);
		}
	}
	
	private void sendErrorForTSV(HttpServletResponse response, Exception e){
		response.reset();
		
		response.setContentType("application/json;charset=UTF-8");
		try{
			OutputResolver resolver = new OutputResolver(response.getWriter());
			resolver.append("{\"ERRMSGINFO\":{");
			if(e instanceof AppWorksException){
				AppWorksException appEx = (AppWorksException)e;
				resolver.append("\"STATUSCODE\":\"").append(appEx.getStatusCode()).append("\"");
				resolver.append(",\"ERRCODE\":\"").append("-10000").append("\"");
				resolver.append(",\"ERRMSG\":\"").append(e.getMessage()).append("\"");
			}else{
				resolver.append("\"STATUSCODE\":\"").append("500").append("\"");
				resolver.append(",\"ERRCODE\":\"").append("-10000").append("\"");
				resolver.append(",\"ERRMSG\":\"").append("요청 작업 처리중 오류가 발생하였습니다.시스템 관리자에게 문의하세요.").append("\"");
			}
			resolver.append("}}");
		}catch (IOException ex) {
			logger.debug(e.getMessage());
		}
	}
}
