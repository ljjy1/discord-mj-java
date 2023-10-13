package com.github.dmj.model;

import com.github.dmj.enums.MjMsgStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * @author ljjy1
 * @classname MjMsg
 * @description  机器人消息
 * @date 2023/10/11 15:54
 */
@Data
@Accessors(chain = true)
public class MjMsg implements Serializable {


    /**
     * 账号key
     */
    private String userKey;

    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 业务唯一ID
     */
    private Integer triggerId;

    /**
     * 服务器ID
     */
    private String guildId;

    /**
     * 服务器名称
     */
    private String guildName;

    /**
     *  频道ID
     */
    private String channelId;

    /**
     * 频道名称
     */
    private String channelName;


    /**
     * 附件
     */
    private Attachment attachment;


    /**
     * 消息内容
     */
    private String content;


    /**
     * 引用的消息ID 通过哪个消息参考生成的新的内容
     */
    private String referenceMsgId;


    /**
     * 消息状态
     */
    private MjMsgStatus status;


    /**
     * 指令列表  用户确认图片可以有哪些执行
     */
    private List<ComponentDetail> components;


    @Data
    @Accessors(chain = true)
    public static class Attachment{

        /**
         * 附件id
         */
        private String id;
        /**
         * 附件url
         */
        private String url;
        /**
         * 附件代理Url
         */
        private String proxyUrl;

        /**
         * 附件名称
         */
        private String fileName;

        /**
         * 附件类型
         */
        private String contentType;

        /**
         * 附件描述
         */
        private String description;

        /**
         * 附件大小
         */
        private Integer size;

        /**
         * 附件高度
         */
        private Integer height;

        /**
         * 附件宽度
         */
        private Integer width;
    }


    @Data
    public static class ComponentDetail{
        private String id;
        private String label;
    }
}
