package com.laofeizhu.rpc.register;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.laofeizhu.rpc.core.RegistryConfig;
import com.laofeizhu.rpc.core.Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/8/19
 * zookeeper的注册中心
 */
@Slf4j(topic = "Zk-Registry")
public class ZkRegistry implements Registry {

    private CuratorFramework client;

    /**
     * 构造函数
     * @param connectString 连接字符串信息
     */
    public ZkRegistry(String connectString) {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        this.client = CuratorFrameworkFactory.newClient(connectString,retryPolicy);
        client.start();
        try {
            /**
             * 查看节点是否存在
             */
            Stat rpc = client.checkExists().forPath(RegistryConfig.RPC_PATH);
            //如果不存在就创建
            if(rpc==null) {
                client.create().creatingParentsIfNeeded().forPath(RegistryConfig.RPC_PATH);
            }
        } catch (Exception e) {
            log.error("zookeeper启动失败:["+e.getMessage()+"]",e);
        }
    }

    /**
     * 注册
     * @param clazz
     * @param registryInfo
     * @throws Exception
     */
    @Override
    public void register(Class clazz, RegistryInfo registryInfo) throws Exception {
        /**
         * 获取所有公共方法
         */
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            String key = Utils.buildIdentify(clazz,method);
            String path = RegistryConfig.RPC_PATH+"/"+key;
            Stat stat = client.checkExists().forPath(path);
            List<RegistryInfo> registryInfos;
            if(stat!=null) {
                byte[] bytes = client.getData().forPath(path);
                String data=new String(bytes,StandardCharsets.UTF_8);
                registryInfos = JSONArray.parseArray(data,RegistryInfo.class);
                if(registryInfos.contains(registryInfo)) {
                    //正常情况，在关闭连接的时候临时节点会自动删除，但是有时候重启后还是存在
                    log.info("注册中心，地址已经存在，路径为："+path);
                } else {
                    registryInfos.add(registryInfo);
                    client.setData().forPath(path, JSONObject.toJSONString(registryInfos).getBytes());
                }
            } else {
                registryInfos=new ArrayList<>();
                registryInfos.add(registryInfo);
                //创建临时节点
                client.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(path,JSONObject.toJSONString(registryInfos).getBytes());
            }
            log.info("注册到注册中心，路径为：["+path+"]信息为:"+registryInfo);
        }
    }

    /**
     * 获得注册信息
     * @param type
     * @return
     */
    @Override
    public List<RegistryInfo> getRegistry(Class type) throws Exception {
        Method[] methods = type.getDeclaredMethods();
        List<RegistryInfo> registryInfos = null;
        for (Method method : methods) {
            String key = Utils.buildIdentify(type,method);
            String path = RegistryConfig.RPC_PATH+"/"+key;
            Stat stat = client.checkExists().forPath(path);
            if(stat==null) {
                log.warn("找不到注册信息，路径为：["+path+"]");
                continue;
            }
            if(null == registryInfos) {
                byte[] bytes = client.getData().forPath(path);
                String data = new String(bytes,StandardCharsets.UTF_8);
                registryInfos = JSONArray.parseArray(data,RegistryInfo.class);
                if(registryInfos!=null && registryInfos.size()>0) {
                    break;
                }
            }
        }
        return registryInfos;
    }
}
