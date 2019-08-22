package com.laofeizhu.rpc.center;

import com.laofeizhu.rpc.register.RegistryInfo;

import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
public interface LoadBalancer {
    RegistryInfo choose(List<RegistryInfo> registryInfoList);
}
