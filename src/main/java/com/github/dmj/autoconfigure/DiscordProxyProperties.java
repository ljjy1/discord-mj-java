package com.github.dmj.autoconfigure;

import lombok.Data;

/**
 * @author ljjy1
 * @classname DiscordProxyProperties
 * @description 代理属性
 * @date 2023/10/13 14:05
 */
@Data
public class DiscordProxyProperties {

    /**
     * 是否开启代理
     */
    private Boolean enable = Boolean.FALSE;
    /**
     * 代理IP
     */
    private String address;
    /**
     * 代理端口
     */
    private Integer port;


    public Boolean isEnable() {
        return enable;
    }
}
