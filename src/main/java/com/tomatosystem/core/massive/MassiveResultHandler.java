package com.tomatosystem.core.massive;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

public class MassiveResultHandler implements ResultHandler {
	
	private StreamResponseAdapter adapter = null;
	
	public MassiveResultHandler(StreamResponseAdapter adapter){
		this.adapter = adapter;
	}

	@Override
	public void handleResult(ResultContext context) {
		if(this.adapter != null){
			this.adapter.appendNode(context.getResultObject());
		}
	}
}
