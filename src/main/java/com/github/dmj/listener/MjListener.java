package com.github.dmj.listener;

import com.github.dmj.model.MjMsg;

/**
 * @author ljjy1
 * @classname MjListener
 * @description 机器人消息监听类
 * @date 2023/10/11 16:03
 */
public interface MjListener {


    /**
     * 接收消息
     */
    void onEmbedMsg(MjMsg msg);
}
