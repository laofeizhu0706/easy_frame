package com.laofeizhu.rpc.center;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
public interface Invoker<T> {
    T invoker(Object[] args);
    void setResult(String result);
}
