package com.github.dmj.autoconfigure;

import cn.hutool.core.util.StrUtil;
import com.github.dmj.error.DiscordMjJavaException;
import com.github.dmj.listener.MjMsgNotify;
import com.github.dmj.service.DiscordService;
import com.github.dmj.service.api.DiscordApi;
import com.github.dmj.util.PropertyUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ljjy1
 * @classname DiscordPropertiesAutoConfig
 * @description 配置读取装配类
 * @date 2023/10/11 9:42
 */

@ConditionalOnProperty(name = "discord.enable",havingValue = "true")
public class DiscordPropertiesAutoConfig implements ApplicationContextAware , EnvironmentAware {

    public static ApplicationContext applicationContext;
    public static DiscordProperties discordProperties;

    @Override
    public void setEnvironment(Environment environment) {
        discordProperties = new DiscordProperties();
        String prefix = "discord.";
        String proxyPrefix = "discord.proxy.";
        String userKeyList = environment.getProperty(prefix + "userKeyList");
        discordProperties.setEnable(Boolean.TRUE);
        discordProperties.setUserKeyList(userKeyList);
        assert userKeyList != null;
        for (String userKey : userKeyList.split(",")) {
            DiscordAccountProperties discordAccountProperties = PropertyUtil.handle(environment, prefix + userKey, DiscordAccountProperties.class);
            //判断如果有属性为空 则报错
            if(StrUtil.isBlank(discordAccountProperties.getUserKey())){
                throw new DiscordMjJavaException("请填写账号key! [Please fill in the userKey]");
            }
            if(StrUtil.isBlank(discordAccountProperties.getUserToken())){
                if(StrUtil.hasBlank(discordAccountProperties.getUser(),discordAccountProperties.getPassword())){
                    throw new DiscordMjJavaException("请填写账号token,或者配置账号密码! [Enter the account token or set the user password ]");
                }
            }
            if(StrUtil.isBlank(discordAccountProperties.getBotToken())){
                throw new DiscordMjJavaException("请填写机器人token! [Please fill in the botToken]");
            }
            if(StrUtil.isBlank(discordAccountProperties.getGuildId())){
                throw new DiscordMjJavaException("请填写服务器ID! [Please fill in the guildId]");
            }
            if(StrUtil.isBlank(discordAccountProperties.getChannelId())){
                throw new DiscordMjJavaException("请填写频道ID! [Please fill in the channelId]");
            }
            discordProperties.getAccount().put(userKey,discordAccountProperties);
        }

        /**
         * 是否开启代理参数
         */
        String enableProxyVar = environment.getProperty(proxyPrefix + "enable");
        if (StrUtil.isNotBlank(enableProxyVar)) {
            boolean enableProxy = Boolean.parseBoolean(enableProxyVar);
            DiscordProxyProperties discordProxyProperties = new DiscordProxyProperties();
            if(enableProxy){
                discordProxyProperties.setEnable(Boolean.TRUE);
                String proxyAddress = environment.getProperty(proxyPrefix + "address");
                if (StrUtil.isBlank(proxyAddress)) {
                    throw new DiscordMjJavaException("开启了代理,请填写代理IP [if the proxy is enabled, enter the proxy ip address]");
                }
                discordProxyProperties.setAddress(proxyAddress);
                String proxyPortVar = environment.getProperty(proxyPrefix + "port");
                if (StrUtil.isBlank(proxyPortVar)) {
                    throw new DiscordMjJavaException("开启了代理,请填写代理端口 [if the proxy is enabled, enter the proxy port]");
                }
                int proxyPort = Integer.parseInt(proxyPortVar);
                discordProxyProperties.setPort(proxyPort);
            }
            discordProperties.setProxy(discordProxyProperties);
        }
    }

    @Bean("discordService")
    public DiscordService discordService() {
        ConcurrentHashMap<String, DiscordApi> discordApiMap = new ConcurrentHashMap<>();
        Iterator<Map.Entry<String, DiscordAccountProperties>> iterator = discordProperties.getAccount().entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, DiscordAccountProperties> next = iterator.next();
            String userKey = next.getKey();
            DiscordAccountProperties discordAccountProperties = next.getValue();
            //注册DiscordApi
            DiscordApi discordApi = new DiscordApi(discordAccountProperties,discordProperties.getProxy());
            discordApiMap.put(userKey,discordApi);
        }
        return new DiscordService(discordApiMap);
    }

    @Bean("mjMsgNotify")
    public MjMsgNotify mjMsgNotify() {
        return new MjMsgNotify();
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        DiscordPropertiesAutoConfig.applicationContext = applicationContext;
    }
}
