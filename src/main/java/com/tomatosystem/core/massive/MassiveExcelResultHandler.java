package com.tomatosystem.core.massive;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

public class MassiveExcelResultHandler implements ResultHandler {
	
	private StreamExcelResponseAdapter adapter = null;
	private String progressId = "";
	
	public MassiveExcelResultHandler(StreamExcelResponseAdapter adapter){
		this.adapter = adapter;
	}
	
	public MassiveExcelResultHandler(StreamExcelResponseAdapter adapter, String progressId){
		this.adapter = adapter;
		this.progressId = progressId;
	}
	@Override
	public void handleResult(ResultContext context) {
		if (this.adapter != null) {
			this.adapter.addRow(context.getResultObject(), this.progressId);
//			this.adapter.endReponse(this.progressId);
		}
	}
	
	
}
