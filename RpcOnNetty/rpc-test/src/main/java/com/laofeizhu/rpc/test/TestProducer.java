package com.laofeizhu.rpc.test;

import com.laofeizhu.rpc.center.ApplicationContext;
import com.laofeizhu.rpc.server.ServiceConfig;

import java.util.Collections;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
public class TestProducer {
    public static void  main (String[] args) throws Exception {
        TestService testService = new TestService();
        ServiceConfig serviceConfig=new ServiceConfig<ITestService>(ITestService.class,testService);
        String registerUrl="zookeeper://127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        ApplicationContext ctx=new ApplicationContext(registerUrl,Collections.singletonList(serviceConfig),null,51555);
    }
}
