package com.laofeizhu.rpc.core;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author 老肥猪
 * @since 2019/8/20
 */
public class Utils {

    /**
     * 构建唯一标识
     * @param clazz 类
     * @param method 方法
     * @return
     */
    public static String buildIdentify(Class clazz, Method method) {
        Map<String,String> map =new HashMap<>();
        map.put(MessageConstant.INTERFACES,clazz.getName());
        map.put(MessageConstant.METHOD,method.getName());
        Optional.ofNullable(method.getParameters()).ifPresent(parameters -> {
            if(parameters.length!=0) {
                StringBuilder sb=new StringBuilder();
                for (int i = 0; i < parameters.length; i++) {
                    sb.append(parameters[i].getType().getName());
                    if(i!=parameters.length-1) {
                        sb.append(",");
                    }
                }
                map.put(MessageConstant.PARAMS,sb.toString());
            }
        });
        return map2String(map);
    }

    /**
     * map转String
     * @param map
     * @return
     */
    public static String map2String(Map<String,String> map) {
        StringBuilder sb=new StringBuilder();
        map.forEach((k, v) -> {
            sb.append(k).append("=").append(v).append("&");
        });
        if(map.size()>0) {
            //删掉最后一个&
            sb.deleteCharAt(sb.length()-1);
        }
        return sb.toString();
    }

    /**
     * string转成map
     * @param str
     * @return
     */
    public static Map<String, String> string2Map(String str) {
        String[] split = str.split("&");
        Map<String, String> map = new HashMap<>(16);
        for (String s : split) {
            String[] split1 = s.split("=");
            map.put(split1[0], split1[1]);
        }
        return map;
    }
}
