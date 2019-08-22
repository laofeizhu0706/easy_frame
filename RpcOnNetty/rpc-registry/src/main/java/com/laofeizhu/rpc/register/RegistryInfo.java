package com.laofeizhu.rpc.register;


import lombok.Data;

/**
 * @author 老肥猪
 * @since 2019/8/19
 * 注册信息
 */
@Data
public class RegistryInfo {
    /**
     * 服务名
     */
    private String hostName;
    /**
     * ip
     */
    private String ip;
    /**
     * 端口
     */
    private Integer port;

    public RegistryInfo(String hostName, String ip, Integer port) {
        this.hostName = hostName;
        this.ip = ip;
        this.port = port;
    }

}
