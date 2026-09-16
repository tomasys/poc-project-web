package com.tomatosystem.core.service;

import java.util.List;
import java.util.Map;

import com.cleopatra.protocol.data.DataResponse;

/**
 * eXCampus Framework 1.0
 * 
 * 클래스: 대용량 엑셀 업로드를 처리하는 업무단 서비스 구현체는 반드시 해당 인터페이스를 implement해야 한다.
 */
public interface ExcelRowHandleService {
//	public int handle(Map paramMap, List<ExcelVO> rowList, DataResponse res) throws Exception;
	public <T> int handle(Map paramMap, List<T> rowList, DataResponse res) throws Exception;
}
																									