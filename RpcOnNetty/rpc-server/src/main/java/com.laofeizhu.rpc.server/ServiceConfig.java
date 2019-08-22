package com.laofeizhu.rpc.server;

import lombok.Data;

/**
 * @author 老肥猪
 * @since 2019/8/20
 */
@Data
public class ServiceConfig<T> {
    /**
     * 类型
     */
    private Class type;
    /**
     * 实例化对象
     */
    private T instance;

    public ServiceConfig(Class type, T instance) {
        this.type = type;
        this.instance = instance;
    }
}
