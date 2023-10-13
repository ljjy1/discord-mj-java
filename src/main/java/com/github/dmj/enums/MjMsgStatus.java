package com.github.dmj.enums;

import lombok.Getter;

/**
 * @author ljjy1
 * @classname MjMsgStatus
 * @description 机器人消息状态枚举
 * @date 2023/10/13 16:01
 */
@Getter
public enum MjMsgStatus {

    START("START","首次触发"),
    UPDATE("UPDATE","更新"),
    ERR("ERR","错误停止"),
    END("END","生成结束"),

    ;
    /**
     * 状态
     */
    private String status;

    private String msg;


    MjMsgStatus(String type, String msg) {
        this.status = type;
        this.msg = msg;
    }
}
