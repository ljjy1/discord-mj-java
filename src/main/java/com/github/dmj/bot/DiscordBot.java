package com.github.dmj.bot;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.dmj.autoconfigure.DiscordAccountProperties;
import com.github.dmj.autoconfigure.DiscordProxyProperties;
import com.github.dmj.enums.MjMsgStatus;
import com.github.dmj.model.MjMsg;
import com.github.dmj.queue.MessageQueue;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.neovisionaries.ws.client.WebSocketFactory;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.DigestUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author ljjy1
 * @classname DiscordBot
 * @description discord机器人
 * @date 2023/10/11 13:38
 */
@Slf4j
public class DiscordBot extends ListenerAdapter {

    private final DiscordAccountProperties discordAccountProperties;


    private final Cache<String, String> cache;

    /**
     * 初始化机器人
     */
    public DiscordBot(DiscordAccountProperties discordAccountProperties, DiscordProxyProperties discordProxyProperties) throws Exception {
        this.discordAccountProperties = discordAccountProperties;
        JDABuilder jdaBuilder = JDABuilder.createDefault(discordAccountProperties.getBotToken());

        //判断是否需要代理
        if (discordProxyProperties.isEnable()) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(discordProxyProperties.getAddress(), discordProxyProperties.getPort()));
            OkHttpClient.Builder builder = new OkHttpClient.Builder().proxy(proxy);

            WebSocketFactory webSocketFactory = new WebSocketFactory();
            webSocketFactory.getProxySettings().setHost(discordProxyProperties.getAddress()).setPort(discordProxyProperties.getPort());
            jdaBuilder.setHttpClientBuilder(builder).setWebsocketFactory(webSocketFactory);
        }
        JDA jda = jdaBuilder.build().awaitReady();
        //注册监听事件
        jda.addEventListener(this);
        cache = CacheBuilder.newBuilder()
                //设置最大500容量
                .maximumSize(500)
                // 根据写入时间设置3秒逐出
                .expireAfterWrite(3, TimeUnit.SECONDS)
                .build();
        log.info("启用userKey:[{}],token:[{}]机器人成功",discordAccountProperties.getUserKey(),discordAccountProperties.getBotToken());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        //把所有消息整合到一个队列里面
        String userKey = discordAccountProperties.getUserKey();
        Message message = event.getMessage();

        //判断消息是不是MJ机器人的    //直接退出
        if (!message.getAuthor().getId().equals("936929561302675456")) return;

        //判断是不是业务系统发的消息 有业务triggerId
        String triggerIdPattern = "<#(\\w+?)#>";
        Pattern compiledPattern = Pattern.compile(triggerIdPattern);
        Matcher matcher = compiledPattern.matcher(message.getContentRaw());

        // 检查是否有匹配项   //没有匹配自己的业务 退出
        if (!matcher.find()) return;

        String match = matcher.group();

        match = match.replace("<#", "");
        match = match.replace("#>", "");
        Integer triggerId = Integer.parseInt(match);


        MjMsg mjMsg = new MjMsg();
        mjMsg.setUserKey(userKey);
        mjMsg.setMsgId(message.getId());
        mjMsg.setTriggerId(triggerId);


        MessageChannel channel = message.getChannel();
        mjMsg.setChannelId(channel.getId());
        mjMsg.setChannelName(channel.getName());

        Guild guild = message.getGuild();
        mjMsg.setGuildId(guild.getId());
        mjMsg.setGuildName(guild.getName());


        List<Message.Attachment> attachments = message.getAttachments();
        if (CollUtil.isNotEmpty(attachments)) {
            Message.Attachment attachment = attachments.get(0);
            mjMsg.setAttachment(new MjMsg.Attachment()
                    .setId(attachment.getId()).setDescription(attachment.getDescription())
                    .setUrl(attachment.getUrl()).setProxyUrl(attachment.getProxyUrl())
                    .setSize(attachment.getSize()).setHeight(attachment.getHeight())
                    .setWidth(attachment.getWidth()).setFileName(attachment.getFileName())
                    .setContentType(attachment.getContentType()));
        }


        String contentRaw = message.getContentRaw();
        mjMsg.setContent(contentRaw);

        if (contentRaw.toLowerCase().contains("waiting to start")) {
            mjMsg.setStatus(MjMsgStatus.START);
        } else if (contentRaw.toLowerCase().contains("(stopped)")) {
            mjMsg.setStatus(MjMsgStatus.ERR);
        } else {
            mjMsg.setStatus(MjMsgStatus.END);
            //获取指令按钮
            List<Button> buttons = message.getButtons();
            if (CollUtil.isNotEmpty(buttons)) {
                List<MjMsg.ComponentDetail> components = new ArrayList<>();
                for (Button button : buttons) {
                    String id = button.getId();
                    String label = button.getLabel();
                    MjMsg.ComponentDetail componentDetail = new MjMsg.ComponentDetail();
                    componentDetail.setId(id);
                    componentDetail.setLabel(label);
                    components.add(componentDetail);
                }
                mjMsg.setComponents(components);
            }
        }

        if (message.getMessageReference() != null)
            mjMsg.setReferenceMsgId(message.getMessageReference().getMessageId());

        try {
            // 创建ObjectMapper对象
            ObjectMapper mapper = new ObjectMapper();
            // 设置输出有序的JSON字符串
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            // 将对象转换为JSON字符串
            String json = mapper.writeValueAsString(mjMsg);
            //当前json用于幂等处理
            //将信息MD5
            String md5 = DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
            //判断是否在缓存中
            if(cache.getIfPresent(md5) != null){
                return;
            }
            cache.put(md5,md5);
        } catch (JsonProcessingException e) {
            //转换失败了不处理 正常发送消息
        }
        log.debug("收到Received消息:{}",JSONUtil.toJsonStr(mjMsg));
        MessageQueue.getInstance().putMsg(mjMsg);

    }


    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event) {
        //把所有消息整合到一个队列里面
        String userKey = discordAccountProperties.getUserKey();
        Message message = event.getMessage();

        //判断消息是不是MJ机器人的
        if (!message.getAuthor().getId().equals("936929561302675456")) {
            //直接退出
            return;
        }
        //判断是不是业务系统发的消息 有业务triggerId
        String triggerIdPattern = "<#(\\w+?)#>";
        Pattern compiledPattern = Pattern.compile(triggerIdPattern);
        Matcher matcher = compiledPattern.matcher(message.getContentRaw());

        // 检查是否有匹配项
        if (!matcher.find()) {
            //没有匹配自己的业务 退出
            return;
        }
        String match = matcher.group();
        match = match.replace("<#", "");
        match = match.replace("#>", "");
        Integer triggerId = Integer.parseInt(match);
        MjMsg mjMsg = new MjMsg();
        mjMsg.setUserKey(userKey);
        mjMsg.setMsgId(message.getId());
        mjMsg.setTriggerId(triggerId);

        MessageChannel channel = message.getChannel();
        mjMsg.setChannelId(channel.getId());
        mjMsg.setChannelName(channel.getName());

        Guild guild = message.getGuild();
        mjMsg.setGuildId(guild.getId());
        mjMsg.setGuildName(guild.getName());

        List<Message.Attachment> attachments = message.getAttachments();
        if (CollUtil.isNotEmpty(attachments)) {
            Message.Attachment attachment = attachments.get(0);
            mjMsg.setAttachment(new MjMsg.Attachment()
                    .setId(attachment.getId()).setDescription(attachment.getDescription())
                    .setUrl(attachment.getUrl()).setProxyUrl(attachment.getProxyUrl())
                    .setSize(attachment.getSize()).setHeight(attachment.getHeight())
                    .setWidth(attachment.getWidth()).setFileName(attachment.getFileName())
                    .setContentType(attachment.getContentType()));
        }

        String contentRaw = message.getContentRaw();
        mjMsg.setContent(contentRaw);
        mjMsg.setStatus(MjMsgStatus.UPDATE);

        if (message.getMessageReference() != null)
            mjMsg.setReferenceMsgId(message.getMessageReference().getMessageId());
        try {
            // 创建ObjectMapper对象
            ObjectMapper mapper = new ObjectMapper();
            // 设置输出有序的JSON字符串
            mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
            // 将对象转换为JSON字符串
            String json = mapper.writeValueAsString(mjMsg);
            //当前json用于幂等处理
            //将信息MD5
            String md5 = DigestUtils.md5DigestAsHex(json.getBytes(StandardCharsets.UTF_8));
            //判断是否在缓存中
            if(cache.getIfPresent(md5) != null){
                return;
            }
            cache.put(md5,md5);
        } catch (JsonProcessingException e) {
            //转换失败了不处理 正常发送消息
        }
        log.debug("收到Update消息:{}",JSONUtil.toJsonStr(mjMsg));
        MessageQueue.getInstance().putMsg(mjMsg);
    }

}
