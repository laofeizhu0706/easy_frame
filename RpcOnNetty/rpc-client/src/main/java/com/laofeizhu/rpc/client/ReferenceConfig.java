package com.laofeizhu.rpc.client;

import lombok.Data;

/**
 * @author 老肥猪
 * @since 2019/8/20
 */
@Data
public class ReferenceConfig {

    private Class type;

    public ReferenceConfig(Class type) {
        this.type = type;
    }
}
