package com.test.mybatis.sqlSession;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public interface Excutor {
    /**
     * 查询
     * @param sql
     * @param parameter
     * @param <T>
     * @return
     */
    <T> T queryOne(String sql,Object object,Object parameter);
}
