package com.github.dmj.model;

import cn.hutool.core.util.StrUtil;
import com.github.dmj.error.DiscordMjJavaException;
import com.github.dmj.util.UniqueUtil;
import lombok.Data;

import java.io.Serializable;


/**
 * @author ljjy1
 * @classname ImagineInRequest
 * @date 2023/10/12 11:04
 */
@Data
public class ImagineInRequest extends BaseRequest implements Serializable {

    /**
     * 文本
     */
    private String prompt;

    /**
     * 图片链接
     *
     */
    private String picurl;



    /**
     * 验证参数
     */
    public void check(){
        if(StrUtil.isBlank(userKey)){
            throw new DiscordMjJavaException("用户key不能为空 [The userKey cannot be empty]");
        }
        if(StrUtil.isBlank(prompt) && prompt.trim().equals("")){
            throw new DiscordMjJavaException("文本不能为空 [The prompt cannot be empty]");
        }

        if (StrUtil.isBlank(picurl) && (prompt.startsWith("http://") || prompt.startsWith("https://"))) {
            String[] parts = prompt.split(" ", 2);
            if (parts.length >= 2) {
                picurl = parts[0];
                prompt = parts[1];
            }
        }
        triggerId = UniqueUtil.generateUniqueId();
        prompt = (StrUtil.isNotBlank(picurl) ? picurl + " " : "") + "<#" + triggerId + "#>" + prompt;
    }
}
