package com.laofeizhu.rpc.server;

import com.alibaba.fastjson.JSONObject;
import com.laofeizhu.rpc.core.MessageConstant;
import com.laofeizhu.rpc.core.Request;
import com.laofeizhu.rpc.core.Response;
import com.laofeizhu.rpc.core.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 老肥猪
 * @since 2019/8/20
 */
@Slf4j(topic = "rpc-handler")
@ChannelHandler.Sharable
public class RpcInvokeHandler extends ChannelInboundHandlerAdapter {

    /**
     * id对应的方法
     */
    private Map<String, Method> id2MethodMap;

    /**
     * 接口对应的实现类
     */
    private Map<Class, Object> interfaces2Instance=new HashMap<>();

    /**
     * 线程池
     */
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 50, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new ThreadFactory() {
        AtomicInteger m = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "RPC-Handler-" + m.incrementAndGet());
        }
    });

    /**
     * 构造函数
     *
     * @param serviceConfigs
     * @param id2MethodMap
     */
    public RpcInvokeHandler(List<ServiceConfig> serviceConfigs, Map<String, Method> id2MethodMap) {
        this.id2MethodMap = id2MethodMap;
        for (ServiceConfig config : serviceConfigs) {
            interfaces2Instance.put(config.getType(), config.getInstance());
        }
    }

    /**
     * 读取信息
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            String message = (String) msg;
            // 这里拿到的是一串JSON数据，解析为Request对象，
            log.info("接收到消息：" + msg);
            Request request = Request.parse(message, ctx);
            threadPoolExecutor.execute(new RpcInvokeTask(request));
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("服务端发生异常：" + cause.getMessage());
        ctx.close();
    }

    class RpcInvokeTask implements Runnable {
        private Request request;

        RpcInvokeTask(Request request) {
            this.request = request;
        }

        @Override
        public void run() {
            String identity = request.getIdentity();
            //方法名称
            Method method = id2MethodMap.get(identity);
            Map<String, String> map = Utils.string2Map(identity);
            //接口名称
            String interfacesName = map.get(MessageConstant.INTERFACES);
            //根据接口名称找到对应实现类
            Class interfacesClass;
            try {
                interfacesClass = Class.forName(interfacesName);
            } catch (ClassNotFoundException e) {
                log.error("找不到对应类：[" + e.getMessage() + "]", e);
                return;
            }
            //获取实例对象
            Object o = interfaces2Instance.get(interfacesClass);
            //获取方法的参数
            String paramsString = map.get(MessageConstant.PARAMS);
            //保存响应结果
            Object result;
            if (paramsString != null && !"".equals(paramsString)) {
                //参数拆分
                String[] paramTypeClasses = paramsString.split(",");
                Map<String, Object> paramsMap = request.getParamsMap();
                //参数实例化保存数组
                Object[] paramInstances = new Object[paramTypeClasses.length];
                for (int i = 0; i < paramTypeClasses.length; i++) {
                    String paramTypeClass = paramTypeClasses[i];
                    paramInstances[i] = paramsMap.get(paramTypeClass);
                }
                try {
                    result = method.invoke(o, paramInstances);
                } catch (Exception e) {
                    log.error("调用方法时发送异常：[" + e.getMessage() + "]", e);
                    return;
                }
            } else {
                try {
                    result = method.invoke(o);
                } catch (Exception e) {
                    log.error("调用方法时发送异常：[" + e.getMessage() + "]", e);
                    return;
                }
            }
            ChannelHandlerContext ctx = request.getCtx();
            String requestId = request.getRequestId();
            Response response = Response.create(JSONObject.toJSONString(result), identity, requestId);
            String msg = JSONObject.toJSONString(response) + "$$";
            ByteBuf byteBuf = Unpooled.copiedBuffer(msg.getBytes());
            //发送给客户端并刷新
            ctx.writeAndFlush(byteBuf);
            log.info("响应给客户端："+msg);
        }

    }


}
