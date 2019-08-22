package com.laofeizhu.rpc.test;

import com.laofeizhu.rpc.center.ApplicationContext;
import com.laofeizhu.rpc.client.ReferenceConfig;
import com.laofeizhu.rpc.server.ServiceConfig;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
public class TestConsumer {

    public static void  main (String[] args) throws Exception {
        ReferenceConfig serviceConfig=new ReferenceConfig(ITestService.class);
        String registerUrl="zookeeper://127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
        ApplicationContext ctx=new ApplicationContext(registerUrl,null,Collections.singletonList(serviceConfig),51555);
        ITestService service = ctx.getService(ITestService.class);
        String test = service.test(new TestBean("laofeizhu", 18), "你好啊");
        System.out.println(test);
        Runtime.getRuntime().exit(0);
    }
}
