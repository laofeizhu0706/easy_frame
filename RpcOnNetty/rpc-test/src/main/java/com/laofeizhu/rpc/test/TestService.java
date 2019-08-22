package com.laofeizhu.rpc.test;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
public class TestService implements ITestService {
    @Override
    public String test(TestBean testBean, String msg) {
        return testBean.toString()+":"+msg;
    }
}
