 package com.tomatosystem.app.sample.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cleopatra.protocol.builder.TSVResponseBuilder;
import com.cleopatra.protocol.data.DataRequest;
import com.cleopatra.protocol.data.DataResponse;
import com.cleopatra.protocol.data.ParameterGroup;
import com.cleopatra.protocol.data.ParameterRow;
import com.tomatosystem.core.exception.AppWorksException;
import com.tomatosystem.core.service.AbstractService;

@Service
public class OneGridService extends AbstractService  {
	
//	@Resource(name="tmpMapper")
//	private TmpMapper exampleMapper;
	
	@Autowired
	private CmnFileService cmnFileService;
	
	public List<Map<String, Object>> selectCmnTmpRegList(Map<String, String> mapParam) throws Exception {
//		return exampleMapper.selectCmnTmpRegList(mapParam);
		return dao.selectList("grid-tmp01.selectCmnTmpRegList", mapParam);
	}
	
	public void selectCmnTmpRegRowHandler(Map<String, String> mapParam, HttpServletResponse response) throws Exception {
		
		final DataResponse dataResponse = DataResponse.getInstance(TSVResponseBuilder.CONTENT_TYPE, response);
		
		dao.selectWithResultHandler("grid-tmp01.selectCmnTmpRegList", mapParam, new ResultHandler<HashMap<String, Object>>() {
			
			public void handleResult(ResultContext<? extends HashMap<String, Object>> resultContext) {

	               HashMap<String, Object> rowData = resultContext.getResultObject();
	               
                   try {
                          dataResponse.send(rowData);
                   } catch (IOException e) {
                          throw new AppWorksException("error");
                   }
	               dataResponse.flush();
			}
		});
	}
	
	
	public void saveCmnTmpReg(DataRequest dataRequest) throws Exception {
		ParameterGroup dsMessage = dataRequest.getParameterGroup("dsCmnTmpReg");
		Iterator<ParameterRow> insertedRows = dsMessage.getInsertedRows();
		Iterator<ParameterRow> updatedRows = dsMessage.getUpdatedRows();
		Iterator<ParameterRow> deletedRows = dsMessage.getDeletedRows();
		
		while(deletedRows.hasNext()){
			dao.delete("grid-tmp01.deleteCmnTmpReg", deletedRows.next().toMap());
		}
		
		while(insertedRows.hasNext()){
			dao.insert("grid-tmp01.insertCmnTmpReg", insertedRows.next().toMap());
		}
		
		while(updatedRows.hasNext()){
			dao.update("grid-tmp01.updateCmnTmpReg", updatedRows.next().toMap());
		}
	}
	
	public void saveCmnTmpRegTab(DataRequest dataRequest) throws Exception {
		ParameterGroup dsMessage = dataRequest.getParameterGroup("dsCmnTmpReg2");
		Iterator<ParameterRow> insertedRows = dsMessage.getInsertedRows();
		Iterator<ParameterRow> updatedRows = dsMessage.getUpdatedRows();
		Iterator<ParameterRow> deletedRows = dsMessage.getDeletedRows();
		
		while(deletedRows.hasNext()){
			dao.delete("grid-tmp01.deleteCmnTmpReg", deletedRows.next().toMap());
		}
		
		while(insertedRows.hasNext()){
			dao.insert("grid-tmp01.insertCmnTmpReg", insertedRows.next().toMap());
		}
		
		while(updatedRows.hasNext()){
			dao.update("grid-tmp01.updateCmnTmpReg", updatedRows.next().toMap());
		}
	}
	
	public void saveCmnTmpRegWithFile(DataRequest dataRequest) throws Exception {
		ParameterGroup dsMessage = dataRequest.getParameterGroup("dsCmnTmpReg");
		Iterator<ParameterRow> insertedRows = dsMessage.getInsertedRows();
		Iterator<ParameterRow> updatedRows = dsMessage.getUpdatedRows();
		Iterator<ParameterRow> deletedRows = dsMessage.getDeletedRows();
		
		Map<String, String> mapFile = new HashMap<String, String>();
		Map<String, String> param;
		while(deletedRows.hasNext()){
			
			param = deletedRows.next().toMap();
			dao.delete("grid-tmp01.deleteCmnTmpReg", param);
			
			//첨부파일 삭제
			mapFile.clear();
			mapFile.put("FILE_SERIAL_NO", param.get("FILE_SERIAL_NO"));
			cmnFileService.deleteCmnFileByAttcFileNo(mapFile);
			
		}
		
		while(insertedRows.hasNext()){
			param = deletedRows.next().toMap();
			dao.insert("grid-tmp01.insertCmnTmpReg", param);
			cmnFileService.commitCmnFile(param.get("FILE_SERIAL_NO"), param.get("ORI_FILE_SERIAL_NO"));
		}
		
		while(updatedRows.hasNext()){
			param = updatedRows.next().toMap();
			dao.update("grid-tmp01.updateCmnTmpReg", param);
			cmnFileService.commitCmnFile(param.get("FILE_SERIAL_NO"), param.get("ORI_FILE_SERIAL_NO"));
		}
	}
	
	
}