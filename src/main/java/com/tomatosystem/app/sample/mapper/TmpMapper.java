package com.tomatosystem.app.sample.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tomatosystem.core.mapper.configurer.Mapper;

/**
 * 
 * TmpMapper.java
 * 
 * @Description 
 * @author tomatosystem
 * @since 2021. 3. 31.
 * @version 1.0
 * @see
 *
 * <pre>
 * << 개정이력(Modification Information) >>
 *   
 *   수정일             수정자             수정내용
 *    -------        ---------------       --------------
 *   2021. 3. 31.        Park. ju wan       최초 생성
 *   Mapper 인터페이스에 대한 marker Annotation(Single-value)으로 MyBatis 적용 방식 중 annotation을 사용한 방식에 대한 기준을 위해 사용
 *   Service에 injection을 위해 Component annotation을 사용
 *   현재 개발환경에선 사용하지 않음 - SqlMapper.java (SqlSessionDaoSupport)를 사용
 * </pre>
 */
@Mapper("tmpMapper")
public interface TmpMapper {

	List selectCmnTmpRegList(Map<String, String> mapParam) throws Exception;


	void insertCmnTmpReg(HashMap param) throws Exception;

	void updateCmnTmpReg(HashMap param) throws Exception;

	void deleteCmnTmpReg(HashMap param) throws Exception;

	
	List selectCmnTmpRegFeeList(HashMap param) throws Exception;
	
	
	void insertCmnTmpRegFee(HashMap param) throws Exception;
	
	void updateCmnTmpRegFee(HashMap param) throws Exception;
	
	void deleteCmnTmpRegFee(HashMap param) throws Exception;
	
}
