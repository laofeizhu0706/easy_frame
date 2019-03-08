package com.test.mybatis.sqlSession;

import com.test.mybatis.pool.DatabasePool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public abstract class Excutor {
    /**
     * 查询
     * @param sql
     * @param parameter
     * @param <T>
     * @return
     */
    public abstract <T> T queryOne(String sql,Object object,Object parameter);
    protected Connection connection() {
        try {
            return DatabasePool.getDatabasePool().getConnection();
        } catch (InterruptedException e) {
            throw new RuntimeException("get conn is error");
        }
    }

    protected String letFirstLetter2Up(String string) {
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
    protected void closeAll(PreparedStatement pre, Connection conn, ResultSet resultSet) {
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
