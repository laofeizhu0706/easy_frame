package com.laofeizhu.rpc.center;

import com.alibaba.fastjson.JSONObject;
import com.laofeizhu.rpc.client.NettyClient;
import com.laofeizhu.rpc.client.ReferenceConfig;
import com.laofeizhu.rpc.core.Response;
import com.laofeizhu.rpc.core.Utils;
import com.laofeizhu.rpc.register.Registry;
import com.laofeizhu.rpc.register.RegistryInfo;
import com.laofeizhu.rpc.register.ZkRegistry;
import com.laofeizhu.rpc.server.NettyServer;
import com.laofeizhu.rpc.server.ServiceConfig;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author 老肥猪
 * @since 2019/8/20
 * 上下文
 */
@Slf4j(topic = "application-context")
public class ApplicationContext {

    private static final Integer PROCESSOR_SIZE = 3;
    //服务配置
    private List<ServiceConfig> serviceConfigs;
    //引用配置
    private List<ReferenceConfig> referenceConfigs;
    //注册中心
    private Registry registry;
    //负载均衡
    private LoadBalancer loadBalancer;
    //id转method的map
    private final Map<String, Method> id2MethodMap = new ConcurrentHashMap<>();
    //注册信息
    private final Map<Class, List<RegistryInfo>> registryMap = new ConcurrentHashMap<>();
    //通道信息
    private final Map<RegistryInfo, ChannelHandlerContext> channels = new ConcurrentHashMap<>();
    //返回值
    private final BlockingQueue<Response> responses = new LinkedBlockingQueue<>();
    //id生成器
    private final LongAdder requestIdWorker = new LongAdder();
    //保存invoker的map
    private final Map<String, Invoker> invokerMap = new ConcurrentHashMap<>();

    private NettyServer nettyServer;

    /**
     * @param registryUrl      注册地址
     * @param serviceConfigs   服务配置
     * @param referenceConfigs 引用配置
     * @param port             端口
     * @param loadBalancer     负载方式
     * @throws Exception
     */
    public ApplicationContext(String registryUrl, List<ServiceConfig> serviceConfigs, List<ReferenceConfig> referenceConfigs, Integer port, LoadBalancer loadBalancer) throws Exception {
        //保存暴露的接口
        this.serviceConfigs = serviceConfigs == null ? new ArrayList<>() : serviceConfigs;
        this.referenceConfigs = referenceConfigs == null ? new ArrayList<>() : referenceConfigs;

        //添加负载方式
        this.loadBalancer = loadBalancer;

        //实例化注册中心
        this.initRegistry(registryUrl);

        //将接口注册到注册中心，从注册中心获取接口，初始化服务接口列表
        InetAddress addr = InetAddress.getLocalHost();
        String hostname = addr.getHostName();
        String hostAddress = addr.getHostAddress();
        RegistryInfo registryInfo = new RegistryInfo(hostname, hostAddress, port);
        this.doRegistry(registryInfo);

        //初始化Netty服务器，接受到请求，直接达到服务提供者的service方法中。
        if (!this.serviceConfigs.isEmpty()) {
            nettyServer = new NettyServer(this.serviceConfigs, id2MethodMap);
            nettyServer.init(port);
        }

        //启动线程消费消息
        this.initProcessor();
    }

    /**
     * 默认负载为随机数负载
     *
     * @param registryUrl      注册地址
     * @param serviceConfigs   服务配置
     * @param referenceConfigs 引用配置
     * @param port             端口
     * @throws Exception
     */
    public ApplicationContext(String registryUrl, List<ServiceConfig> serviceConfigs, List<ReferenceConfig> referenceConfigs, Integer port) throws Exception {
        this(registryUrl, serviceConfigs, referenceConfigs, port, new RandomBalancer());
    }

    /**
     * 该方法仅仅支持zk注册中心
     *
     * @param registryUrl
     */
    private void initRegistry(String registryUrl) {
        if (registryUrl.startsWith("zookeeper://")) {
            //将前面的协议剔除
            registry = new ZkRegistry(registryUrl.substring(12));
        }
    }

    /**
     * 进行注册
     *
     * @param registryInfo
     * @throws Exception
     */
    private void doRegistry(RegistryInfo registryInfo) throws Exception {
        //初始化提供者的配置
        for (ServiceConfig config : serviceConfigs) {
            Class type = config.getType();
            registry.register(type, registryInfo);
            Method[] declaredMethods = type.getDeclaredMethods();
            for (Method method : declaredMethods) {
                String identify = Utils.buildIdentify(type, method);
                id2MethodMap.put(identify, method);
            }
        }
        //初始化引用配置
        for (ReferenceConfig config : referenceConfigs) {
            Class type = config.getType();
            List<RegistryInfo> registryInfos = registry.getRegistry(type);
            if (registryInfos != null && registryInfos.size() > 0) {
                //这里是为了快速拿到注册机信息，但是有问题：需加入Watch机制，发现新节点加入的时候，对这个map做刷新
                registryMap.put(type, registryInfos);
                this.initChannel(registryInfos);
            }
        }
    }

    /**
     * 初始化通道
     *
     * @param registryInfos
     */
    private void initChannel(List<RegistryInfo> registryInfos) {
        for (RegistryInfo registryInfo : registryInfos) {
            if (!channels.containsKey(registryInfo)) {
                log.info("开始建立连接：" + registryInfo.getIp() + ":" + registryInfo.getPort());
                NettyClient client = new NettyClient(registryInfo.getIp(), registryInfo.getPort());
                //将
                client.setMessageCallback(msg -> {
                    Response response = JSONObject.parseObject(msg, Response.class);
                    try {
                        responses.put(response);
                    } catch (InterruptedException e) {
                        log.error("放入返回值队列失败：[" + e.getMessage() + "]", e);
                    }
                });
                //获取连接
                ChannelHandlerContext ctx = client.getCtx();
                channels.put(registryInfo, ctx);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{clazz}, (Object proxy, Method method, Object[] args) -> {
            String methodName = method.getName();
            if ("equals".equals(methodName) || "hasCode".equals(methodName)) {
                throw new IllegalAccessException("不能访问" + methodName + "方法");
            }
            if ("toString".equals(methodName)) {
                return clazz.getName() + "#" + methodName;
            }
            //获取负载选择的机器
            List<RegistryInfo> registryInfos = registryMap.get(clazz);
            RegistryInfo registryInfo = loadBalancer.choose(registryInfos);

            //获取到它的上下文
            ChannelHandlerContext channelHandlerContext = channels.get(registryInfo);

            //获取id
            String identify = Utils.buildIdentify(clazz, method);
            requestIdWorker.increment();
            String requestId = String.valueOf(requestIdWorker.longValue());
            Invoker invoker = new DefaultInvoker(identify, requestId, channelHandlerContext, method.getReturnType());
            invokerMap.put(identify + ":" + requestId, invoker);
            return invoker.invoker(args);
        });
    }

    /**
     * 初始化处理者，该处理着处理返回值，并把返回值反到对应的调用者的结果中，并唤醒调用者
     */
    private void initProcessor() {
        int num = PROCESSOR_SIZE;
        ExecutorService executor = Executors.newFixedThreadPool(3);
        for (int i = 0; i < num; i++) {
            executor.submit(() -> {
                for (; ;) {
                    Response response = responses.take();
                    log.info("获取到返回数据：["+response+"]");
                    String identify = response.getIdentify();
                    String requestId = response.getRequestId();
                    String key = identify + ":" + requestId;
                    Invoker invoker = invokerMap.get(key);
                    invoker.setResult(response.getResult());
                }
            });
        }
    }
}
