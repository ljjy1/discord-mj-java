package com.github.dmj.listener;

import com.github.dmj.annotation.MjMsgListener;
import com.github.dmj.autoconfigure.DiscordPropertiesAutoConfig;
import com.github.dmj.model.MjMsg;
import com.github.dmj.queue.MessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author ljjy1
 * @classname MjMsgNotify
 * @description 机器人消息通知
 * @date 2023/10/11 16:10
 */
@Slf4j
@Component
public class MjMsgNotify {

    private static final ExecutorService executorService;
    /**
     * 消息接收实现
     */
    private static final Map<String, MjListener> listenerMap = new ConcurrentHashMap<>();

    static {
        executorService = Executors.newFixedThreadPool(1);
        //获取了所有实现了MjMsgListener的类
        Map<String, MjListener> msgListenerMap = DiscordPropertiesAutoConfig.applicationContext.getBeansOfType(MjListener.class);

        for (MjListener mjListener : msgListenerMap.values()) {
            Class<? extends MjListener> aClass = mjListener.getClass();
            MjMsgListener annotation = aClass.getAnnotation(MjMsgListener.class);
            if(annotation != null){
                listenerMap.put(annotation.userKey(), mjListener);
            }
        }
        msgNotify();
    }


    /**
     * 消息推送
     */
    public static void msgNotify(){
        MessageQueue messageEmbedQueue = MessageQueue.getInstance();
        log.info("MJ消息通知启动.........");
        executorService.execute(() -> {
            while (true){
                MjMsg mjMsg = messageEmbedQueue.takeMsg();
                if(mjMsg != null){
                    Iterator<Map.Entry<String, MjListener>> iterator = listenerMap.entrySet().iterator();
                    while (iterator.hasNext()){
                        Map.Entry<String, MjListener> next = iterator.next();
                        String userKey = next.getKey();
                        MjListener mjListener = next.getValue();
                        if(userKey.equals("ALL")){
                            mjListener.onEmbedMsg(mjMsg);
                        }else{
                            String mjMsgUserKey = mjMsg.getUserKey();
                            if(mjMsgUserKey.equals(userKey)){
                                mjListener.onEmbedMsg(mjMsg);
                            }
                        }

                    }

                }
            }
        });
    }
}
