package com.github.dmj.model;

import cn.hutool.core.util.StrUtil;
import com.github.dmj.error.DiscordMjJavaException;
import lombok.Data;

import java.io.Serializable;


/**
 * @author ljjy1
 * @classname ResetRequest
 * @date 2023/10/12 14:57
 */
@Data
public class ResetRequest extends BaseRequest implements Serializable {

    /**
     * 机器人返回的消息ID
     */
    private String msgId;
    /**
     * 机器人消息返回消息的文件链接 文件名称hash
     */
    private String msgHash;

    /**
     * 验证参数
     */
    public void check(){
        if(StrUtil.isBlank(userKey)){
            throw new DiscordMjJavaException("用户key不能为空 [The userKey cannot be empty]");
        }
        if(triggerId == null || triggerId == 0){
            throw new DiscordMjJavaException("业务系统生成的唯一ID不能为空 [The triggerId by the service system cannot be empty]");
        }
        if(StrUtil.isBlank(msgId)){
            throw new DiscordMjJavaException("机器人返回的消息ID不能为空 [The msgId returned by the robot cannot be empty]");
        }
        if(StrUtil.isBlank(msgHash)){
            throw new DiscordMjJavaException("文件名称hash不能为空 [The msgHash cannot be empty]");
        }
    }
}
