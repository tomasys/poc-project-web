package com.tomatosystem.app.service;

import java.util.Map;

import com.tomatosystem.core.mapper.configurer.Mapper;

/**
 *
 * @author  tomatosystem
 * @since 
 * @version 1.0
 * @see <pre>
 *  == 개정이력(Modification Information) ==
 *
 *          수정일          수정자           수정내용
 *  ----------------    ------------    ---------------------------
 *
 * </pre>
 */

@Mapper("csrScoreMapper")
public interface CsrScoreMapper {
	
    int insertCsrScore(Map<String, Object> param);


}
