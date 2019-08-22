package com.laofeizhu.rpc.center;

import com.alibaba.fastjson.JSONObject;
import com.laofeizhu.rpc.core.MessageConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
@Slf4j(topic = "invoker")
public class DefaultInvoker<T> implements Invoker {

    private String identify;
    private String requestId;
    private ChannelHandlerContext ctx;
    private Class resturnType;
    private CountDownLatch downLatch=new CountDownLatch(1);
    private T result;

    public DefaultInvoker(String identify, String requestId, ChannelHandlerContext ctx, Class resturnType) {
        this.identify = identify;
        this.requestId = requestId;
        this.ctx = ctx;
        this.resturnType = resturnType;
    }

    @Override
    public T invoker(Object[] args) {
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
         *    "requestId":"1",
         * }
         */
        JSONObject json=new JSONObject();
        json.put(MessageConstant.IDENTITY,this.identify) ;
        json.put(MessageConstant.REQUEST_ID,this.requestId) ;
        JSONObject param=new JSONObject();
        if(args!=null) {
            for (Object arg : args) {
                param.put(arg.getClass().getName(),arg);
            }
        }
        json.put(MessageConstant.PARAMS,param);
        log.info("发送一条信息给服务端，信息内容为：["+json.toString()+"]");
        String msg = json.toString()+"$$";//$$是我们定义的消息分隔符
        ByteBuf byteBuf = Unpooled.buffer(msg.getBytes().length);
        byteBuf.writeBytes(msg.getBytes());
        this.ctx.writeAndFlush(byteBuf);
        this.waitResult();
        return this.result;
    }

    @Override
    public void setResult(String result) {
        Object o = JSONObject.parseObject(result, this.resturnType);
        this.result = (T)o;
        downLatch.countDown();
    }

    private void waitResult() {
        try {
            downLatch.await();
        } catch (InterruptedException e) {
            log.error("等待返回结果异常：["+e.getMessage()+"]",e);
        }
    }
}
