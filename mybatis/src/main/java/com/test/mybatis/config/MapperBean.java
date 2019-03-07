package com.test.mybatis.config;

import lombok.Data;

import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/3/7
 */
@Data
public class MapperBean {
    private String namespace;
    private List<Function> list;
}
