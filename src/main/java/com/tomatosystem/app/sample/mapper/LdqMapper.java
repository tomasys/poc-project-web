package com.tomatosystem.app.sample.mapper;

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

@Mapper("ldqMapper")
public interface LdqMapper {
	
    int insertLdg(Map<String, Object> param);


}
