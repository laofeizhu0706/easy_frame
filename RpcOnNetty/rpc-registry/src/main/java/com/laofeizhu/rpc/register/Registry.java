package com.laofeizhu.rpc.register;

import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/8/19
 */
public interface Registry {
    void register(Class clazz,RegistryInfo registryInfo) throws Exception;

    List<RegistryInfo> getRegistry(Class type) throws Exception;
}
