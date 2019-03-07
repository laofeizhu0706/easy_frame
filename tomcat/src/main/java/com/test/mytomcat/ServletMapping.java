package com.test.mytomcat;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
@Getter
@Setter
public class  ServletMapping {
    private String url;
    private String clazz;
    private String name;
    public ServletMapping(String url, String clazz, String name) {
        this.url = url;
        this.clazz = clazz;
        this.name = name;
    }

}
