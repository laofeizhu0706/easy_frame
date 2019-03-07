package com.test.mybatis.sqlSession;

import com.test.mybatis.pool.DatabasePool;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class BaseExcutor<T> implements Excutor {
    @Override
    public <T> T queryOne(String sql, Object object, Object parameter) {
        Field[] fields = object.getClass().getDeclaredFields();
        PreparedStatement pre = null;
        ResultSet set = null;
        Connection conn = connection();
        try {
            pre = conn.prepareCall(sql);
            String id = parameter.toString();
            pre.setString(1, id);
            set = pre.executeQuery();
//            set.next();
            if (set.next()) {
                ResultSetMetaData metaData = set.getMetaData();
                int columnCount = metaData.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    Object o = set.getObject(i + 1);
                    String columnName = metaData.getColumnName(i + 1);
                    for (int j = 0; j < fields.length; j++) {
                        if (fields[j].getName().equals(columnName)) {
                            Method method;
                            if(fields[j].getType().getSimpleName().equals("String")) {
                                method = object.getClass().getMethod("set" + letFirstLetter2Up(columnName),String.class);
                                method.invoke(object,(String)o);
                            } else if(fields[j].getType().getSimpleName().equals("Integer")
                                    || fields[j].getType().getSimpleName().equals("int")) {
                                method = object.getClass().getMethod("set" + letFirstLetter2Up(columnName),Integer.class);
                                method.invoke(object, Integer.parseInt(o.toString()));
                            } else if(fields[j].getType().getSimpleName().equals("Double")
                                    || fields[j].getType().getSimpleName().equals("double")) {
                                method = object.getClass().getMethod("set" + letFirstLetter2Up(columnName),Double.class);
                                method.invoke(object, Double.parseDouble(o.toString()));
                            } else {
                                throw new RuntimeException("this type conversion is not supported");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            closeAll(pre, conn, set);
        }
        return (T) object;
    }

    private Connection connection() {
        try {
            return DatabasePool.getDatabasePool().getConnection();
        } catch (InterruptedException e) {
            throw new RuntimeException("get conn is error");
        }
    }

    public String letFirstLetter2Up(String string) {
        if (string != null && !"".equals(string)) {
            String up = string.substring(0, 1);
            if (string.length() == 1) {
                return up;
            } else {
                String latter = string.substring(1);
                return up.toUpperCase() + latter;
            }
        } else {
            throw new RuntimeException("string is not be null");
        }
    }

    /**
     * 回收
     *
     * @param pre
     * @param conn
     * @param resultSet
     */
    public void closeAll(PreparedStatement pre, Connection conn, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (pre != null) {
                pre.close();
            }
            if (conn != null) {
                DatabasePool.getDatabasePool().recycle(conn);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
