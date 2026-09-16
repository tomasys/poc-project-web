package com.tomatosystem.core.mapper;

import java.util.List;

import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;

public interface SqlMapper {
	/**
     * 추가
     *
     * @param queryId
     * @param param
     * @return
     */
	public int insert(String queryId, Object param);
	
	/**
     * 수정
     *
     * @param queryId
     * @param param
     * @return
     */
    public int update(String queryId, Object param);
    
    /**
     * 삭제
     *
     * @param queryId
     * @param param
     * @return
     */
    public int delete(String queryId, Object param);
    
    /**
     * 단건 조회
     *
     * @param queryId
     * @return
     */
    public <T> T selectOne(String queryId);
    
    /**
     * 단건 조회
     *
     * @param queryId
     * @param param
     * @return
     */
    public <T> T selectOne(String queryId, Object param);
    
    /**
     * 다건 조회
     *
     * @param queryId
     * @return 리스트 반환
     */
    public <E> List<E> selectList(String queryId);
    
    /**
     * 다건 조회
     *
     * @param queryId
     * @param param
     * @return 리스트 반환
     */
    public <E> List<E> selectList(String queryId, Object param);
    
    /**
     * ResultHandler를 통한 다건 조회
     * 조회된 결과값에 대한 처리는 ResultHandler내에서 처리함
     *
     * @param queryId
     * @param param
     * @param handler
     */
    public void selectWithResultHandler(String queryId, Object param, ResultHandler<?> handler);
    
    /**
     * 추가(배치) 대량의 데이터 추가시 사용할 경우 크게 성능이 향상된다.
     *
     * @param queryId
     * @param list
     * @return
     */
    public <T> void batchInsert(String queryId, List<T> list);
    
    /**
     * 수정(배치) 대량의 데이터 수정시 사용할 경우 크게 성능이 향상된다.
     *
     * @param queryId
     * @param list
     * @return
     */
    public <T> void batchUpdate(String queryId, List<T> list);
    
    /**
     * 삭제(배치) 대량의 데이터 삭제시 사용할 경우 크게 성능이 향상된다.
     *
     * @param queryId
     * @param list
     * @return
     */
	public <T> void batchDelete(String queryId, List<T> list);
    
    /**
     * 해당 쿼리의 유형을 반환한다.
     * 
     * @param sqlId
     * @return
     */
    public SqlCommandType getSqlCommandType(String sqlId);
}
