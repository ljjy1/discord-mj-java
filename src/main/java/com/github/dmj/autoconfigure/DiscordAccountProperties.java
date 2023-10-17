package com.github.dmj.autoconfigure;

import lombok.Data;

/**
 * @author ljjy1
 * @classname DiscordAccountProperties
 * @description 账号属性类
 * @date 2023/10/13 14:05
 */
@Data
public class DiscordAccountProperties {

    /**
     * 用户key 自己定义不重复即可(用于切换用户调用接口)
     */
    private String userKey;
    /**
     * 用户token
     */
    private String userToken;

    /**
     * discord账号
     */
    private String user;

    /**
     * discord密码
     */
    private String password;

    /**
     * 当前用户下应用机器人token
     */
    private String botToken;
    /**
     * 服务器ID
     */
    private String guildId;
    /**
     * 频道ID
     */
    private String channelId;
    /**
     * 并发执行任务大小
     */
    private int concurSize = 3;
    /**
     * 等待队列大小
     */
    private int waitSize = 10;
}
