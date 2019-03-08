package com.test.mybatis.sqlSession;

import java.lang.reflect.Proxy;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class SqlSession {

    private Excutor excutor= new BaseExcutor();
    private String mapperName=null;
    public <T> T selectOne(String sql, Object parameter , Object object) {
        return excutor.queryOne(sql,object,parameter);
    }

    public <T> T getMapper(Class<T> clazz) {
        this.mapperName=clazz.getSimpleName()+".xml";
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new MapperProxy(this));
    }

    public String getMapperName() {
        return mapperName;
    }
}
