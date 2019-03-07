package com.test.mybatis.config;

import lombok.Data;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
@Data
public class Function {
    private String sqlType;
    private String funcName;
    private String sql;
    private Object resultType;
    private String parameterType;
}
