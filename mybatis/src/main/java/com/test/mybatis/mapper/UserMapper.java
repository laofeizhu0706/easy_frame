package com.test.mybatis.mapper;

import com.test.mybatis.bean.User;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
public interface UserMapper {
    User getUserById(Integer id);
}
