package com.laofeizhu.rpc.core;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author 老肥猪
 * @since 2019/8/19
 */
@Data
public class Request {

    /**
     * id
     */
    private String identity;

    /**
     * 参数
     */
    private Map<String, Object> paramsMap;

    /**
     * 上下文
     */
    private ChannelHandlerContext ctx;

    /**
     * 请求id
     */
    private String requestId;

    /**
     * 获取Request
     * @param message 信息
     * @param ctx 上下文
     * @return
     * @throws ClassNotFoundException
     */
    public static Request parse(String message, ChannelHandlerContext ctx) throws ClassNotFoundException {
        /*
         * {
         *   "identity":"interfaces=com.laofeizhu.rpc.test.producer.TestService&method=test&params=java.lang.String,com.laofeizhu.rpc.test.producer.TestBean",
         *   "params":{
         *      "java.lang.String":"test",
         *      "com.study.rpc.test.producer.TestBean":{
         *              "name":"老肥猪",
         *              "age":21
         *        }
         *    },
         *    "requestId":"1"
         * }
         */
        JSONObject json = JSONObject.parseObject(message);
        String identity = json.getString(MessageConstant.IDENTITY);
        JSONObject params = json.getJSONObject(MessageConstant.PARAMS);
        Set<String> strings = params.keySet();
        Map<String,Object> map=new HashMap<>();
        for (String key : strings) {
            //对常见的类型指定类型值
            switch (key) {
                case "java.lang.String":
                    map.put(key, params.getString(key));
                    break;
                case "java.lang.Integer":
                    map.put(key, params.getIntValue(key));
                    break;
                case "java.lang.Long":
                    map.put(key, params.getLongValue(key));
                    break;
                default:
                    Class clazz = Class.forName(key);
                    Object obj = params.getObject(key, clazz);
                    map.put(key, obj);
                    break;
            }
        }
        Request request=new Request();
        request.setIdentity(identity);
        request.setCtx(ctx);
        request.setRequestId(json.getString(MessageConstant.REQUEST_ID));
        request.setParamsMap(map);
        return request;
    }

}
