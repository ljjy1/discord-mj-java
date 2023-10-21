package com.github.dmj.queue;

import com.github.dmj.model.MjMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author ljjy1
 * @classname MessageQueue
 * @description  消息队列
 * @date 2023/10/11 15:47
 */
@Slf4j
public class MessageQueue {

    private static final LinkedBlockingQueue<MjMsg> msgQueue = new LinkedBlockingQueue<>(300);

    private MessageQueue() {
    }

    private static class MessageEmbedQueueHolder{
        private static final MessageQueue INSTANCE = new MessageQueue();
    }


    public static MessageQueue getInstance(){
        return MessageEmbedQueueHolder.INSTANCE;
    }

    /**
     * 获取并移除队首元素 如果队列为空则会阻塞
     */
    public MjMsg takeMsg(){
        try {
            return msgQueue.take();
        } catch (InterruptedException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 往队列添加元素  如果队列满了 会阻塞等待
     * @param msg
     */
    public void putMsg(MjMsg msg){
        msgQueue.offer(msg);
    }

}
