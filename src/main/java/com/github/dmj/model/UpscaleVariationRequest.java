package com.github.dmj.model;

import cn.hutool.core.util.StrUtil;
import com.github.dmj.error.DiscordMjJavaException;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author ljjy1
 * @classname UpscaleVariationRequest
 * @date 2023/10/12 14:32
 */
@Data
public class UpscaleVariationRequest extends BaseRequest implements Serializable {

    /**
     * 图片位置 1-4
     */
    private Integer index;
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

        if(index == null || !Arrays.asList(1,2,3,4).contains(index)){
            throw new DiscordMjJavaException("图片位置不能为空,且只能选择1,2,3,4 [The image location cannot be empty and can only be 1,2,3,4]");
        }
        if(StrUtil.isBlank(msgId)){
            throw new DiscordMjJavaException("机器人返回的消息ID不能为空 [The msgId returned by the robot cannot be empty]");
        }
        if(StrUtil.isBlank(msgHash)){
            throw new DiscordMjJavaException("文件名称hash不能为空 [The msgHash cannot be empty]");
        }
    }
}
