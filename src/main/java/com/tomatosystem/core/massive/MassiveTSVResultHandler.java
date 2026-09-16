package com.tomatosystem.core.massive;

import java.io.IOException;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import com.cleopatra.protocol.data.DataResponse;
import com.tomatosystem.core.exception.AppWorksException;

public class MassiveTSVResultHandler implements ResultHandler {
	
	private DataResponse dataResponse = null;
	
	public MassiveTSVResultHandler(DataResponse response){
		this.dataResponse = response;
	}

	@Override
	public void handleResult(ResultContext context) {
		try {
			this.dataResponse.send(context.getResultObject());
			this.dataResponse.flush();
		} catch (IOException e) {
			throw new AppWorksException(e.getMessage());
		}
	}
}
