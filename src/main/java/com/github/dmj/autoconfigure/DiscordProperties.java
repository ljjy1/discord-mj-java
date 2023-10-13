package com.github.dmj.autoconfigure;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author ljjy1
 * @classname DiscordProperties
 * @description 总discord配置类
 * @date 2023/10/11 11:20
 */
@Data
public class DiscordProperties {

    /**
     * 是否启用
     */
    private Boolean enable = Boolean.FALSE;

    /**
     * 多账号key userKey1,userKey2,userKey3 ....
     */
    private String userKeyList;


    /**
     * 多账号配置 key 账号key value 属性
     */
    private Map<String,DiscordAccountProperties> account = new LinkedHashMap<>();


    /**
     * 代理信息
     */
    private DiscordProxyProperties proxy;


}
