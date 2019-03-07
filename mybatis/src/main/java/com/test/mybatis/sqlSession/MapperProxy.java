package com.test.mybatis.sqlSession;

import com.test.mybatis.config.Configuration;
import com.test.mybatis.config.Function;
import com.test.mybatis.config.MapperBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class MapperProxy implements InvocationHandler {

    private SqlSession sqlSession;

    public MapperProxy(SqlSession sqlSession) {
        this.sqlSession = sqlSession;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MapperBean bean = Configuration.readMapper(sqlSession.getMapperName());
        if(method.getDeclaringClass().getName().equals(bean.getNamespace()) ) {
            List<Function> list = bean.getList();
            if(list!=null && list.size()>0) {
                for (Function function: list) {
                    /**
                     * 如果方法名匹配上就执行
                     */
                    if(method.getName().equals(function.getFuncName())) {
                        return sqlSession.selectOne(function.getSql(),args[0],function.getResultType());
                    }
                }
            }
        }
        return null;
    }
}
