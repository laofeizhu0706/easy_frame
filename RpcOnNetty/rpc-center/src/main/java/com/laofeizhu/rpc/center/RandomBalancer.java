package com.laofeizhu.rpc.center;

import cn.hutool.core.util.RandomUtil;
import com.laofeizhu.rpc.register.RegistryInfo;

import java.util.List;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
public class RandomBalancer implements LoadBalancer {
    @Override
    public RegistryInfo choose(List<RegistryInfo> registryInfoList) {
        return registryInfoList.get(RandomUtil.randomInt(0,registryInfoList.size()));
    }
}
