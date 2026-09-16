package com.tomatosystem.core.mapper;

import java.util.List;

import javax.annotation.Resource;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;

import egovframework.rte.psl.dataaccess.EgovAbstractMapper;

@Repository("defalut.sqlMapper")
public class SqlMapperImpl extends EgovAbstractMapper implements SqlMapper {
	
	@Resource(name = "sqlSession")
	public void setSqlSessionFactory(SqlSessionFactory sqlSession) {
		super.setSqlSessionFactory(sqlSession);
	}
	
	@Resource(name="sqlSessionTemplate")
    private SqlSessionTemplate batchSqlSession;
	
	/**
     * 추가
     *
     * @param queryId
     * @param param
     * @return
     */
	@Override
    public int insert(String queryId, Object param) {
        return super.insert(queryId, param);
    }

	/**
     * 수정
     *
     * @param queryId
     * @param param
     * @return
     */
	@Override
    public int update(String queryId, Object param) {
        return super.update(queryId, param);
    }

	/**
     * 삭제
     *
     * @param queryId
     * @param param
     * @return
     */
	@Override
    public int delete(String queryId, Object param) {
        return super.delete(queryId, param);
    }

	/**
     * 단건 조회
     *
     * @param queryId
     * @return
     */
	@Override
    public <T> T selectOne(String queryId) {
		return super.selectOne(queryId);
    }
    
	/**
     * 단건 조회
     *
     * @param queryId
     * @param param
     * @return
     */
	@Override
    public <T> T selectOne(String queryId, Object param) {
        return super.selectOne(queryId, param);
    }

	/**
     * 다건 조회
     *
     * @param queryId
     * @return 리스트 반환
     */
	@Override
    public <E> List<E> selectList(String queryId) {
        return super.selectList(queryId);
    }
    
	/**
     * 다건 조회
     *
     * @param queryId
     * @param param
     * @return 리스트 반환
     */
	@Override
    public <E> List<E> selectList(String queryId, Object param) {
        return super.selectList(queryId, param);
    }
	
	/**
     * 추가(배치) 대량의 데이터 추가시 사용할 경우 크게 성능이 향상된다.
     *
     * @param queryId
     * @param list
     * @return
     */
	@Override
	public <T> void batchInsert(String queryId, List<T> list){
		for (T t : list) {
			batchSqlSession.insert(queryId, t);
        }
	}
	
	/**
     * 수정(배치) 대량의 데이터 수정시 사용할 경우 크게 성능이 향상된다.
     *
     * @param queryId
     * @param list
     * @return
     */
	@Override
	public <T> void batchUpdate(String queryId, List<T> list){
		for (T t : list) {
			batchSqlSession.update(queryId, t);
        }
	}
	
	/**
     * 삭제(배치) 대량의 데이터 삭제시 사용할 경우 크게 성능이 향상된다.
     *
     * @param queryId
     * @param list
     * @return
     */
	@Override
	public <T> void batchDelete(String queryId, List<T> list){
		for (T t : list) {
			batchSqlSession.delete(queryId, t);
        }
	}
	
	/**
     * ResultHandler를 통한 다건 조회
     * 조회된 결과값에 대한 처리는 ResultHandler내에서 처리함
     *
     * @param queryId
     * @param param
     * @param handler
     */
	@Override
	public void selectWithResultHandler(String queryId, Object param, ResultHandler<?> handler) {
		super.getSqlSession().select(queryId, param, handler);
	}

	/**
     * 해당 쿼리의 유형을 반환한다.
     * 
     * @param sqlId
     * @return
     */
	@Override
	public SqlCommandType getSqlCommandType(String sqlId){
		MappedStatement statement = this.getSqlSession().getConfiguration().getMappedStatement(sqlId);
		return statement != null ? statement.getSqlCommandType() : SqlCommandType.UNKNOWN;
	}
}
