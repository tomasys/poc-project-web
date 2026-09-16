package com.tomatosystem.core.massive;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

public class MassiveCSVResultHandler implements ResultHandler {
	
	private StreamCsvResponseAdapter adapter = null;
	private String progressId = "";
	public MassiveCSVResultHandler(StreamCsvResponseAdapter adapter){
		this.adapter = adapter;
	}
	
	public MassiveCSVResultHandler(StreamCsvResponseAdapter adapter, String progressId){
		this.adapter = adapter;
		this.progressId = progressId;
	}
	@Override
	public void handleResult(ResultContext context) {
		if(this.adapter != null){
			this.adapter.addRow(context.getResultObject(), this.progressId);
		}
	}
}
