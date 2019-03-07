package com.test.mybatis;

import com.test.mybatis.bean.User;
import com.test.mybatis.mapper.UserMapper;
import com.test.mybatis.sqlSession.SqlSession;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public class Main {

    public static void main(String[] args) {
        SqlSession sqlSession = new SqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        User user = userMapper.getUserById(1);
        System.out.println(user);
    }
}
