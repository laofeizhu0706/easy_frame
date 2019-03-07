package com.test.mytomcat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
public class ServletMappingConfig {
    private static List<ServletMapping> mappingList=new ArrayList<>();

    static {
        mappingList.add(new ServletMapping("/test/eat","com.test.mytomcat.EatServlet","eat-mapping"));
        mappingList.add(new ServletMapping("/test/hello","com.test.mytomcat.HelloServlet","hello-mapping"));
    }

    public static List<ServletMapping> getMappingList() {
        return mappingList;
    }
}
