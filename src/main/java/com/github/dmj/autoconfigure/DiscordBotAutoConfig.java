package com.github.dmj.autoconfigure;

import com.github.dmj.bot.DiscordBot;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ljjy1
 * @classname DiscordBotAutoConfig
 * @description 机器人配置类
 * @date 2023/10/11 14:00
 */
@Configuration
@AutoConfigureAfter({DiscordPropertiesAutoConfig.class})
@ConditionalOnProperty(name = "discord.enable",havingValue = "true")
public class DiscordBotAutoConfig {


    @PostConstruct
    public void init() throws Exception {
        initBot();
    }


    /**
     * 初始化机器人
     */
    public void initBot() throws Exception{
        DiscordProperties discordProperties = DiscordPropertiesAutoConfig.discordProperties;
        Iterator<Map.Entry<String, DiscordAccountProperties>> iterator = discordProperties.getAccount().entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, DiscordAccountProperties> next = iterator.next();
            DiscordAccountProperties discordAccountProperties = next.getValue();
            new DiscordBot(discordAccountProperties, discordProperties.getProxy());
        }
    }

}
