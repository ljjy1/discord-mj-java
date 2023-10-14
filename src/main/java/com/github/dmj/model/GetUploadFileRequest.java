package com.github.dmj.model;

import cn.hutool.core.util.StrUtil;
import com.github.dmj.error.DiscordMjJavaException;
import lombok.Data;

import java.io.Serializable;

/**
 * @author ljj1
 * @classname GetUploadFileRequest
 * @description TODO
 * @date 2023/10/14 15:53
 */

@Data
public class GetUploadFileRequest extends BaseRequest implements Serializable {


    /**
     * 上传文件返回的文件名
     */
    private String uploadFilename;


    public void check(){

        if(StrUtil.isBlank(userKey)){
            throw new DiscordMjJavaException("用户key不能为空 [The userKey cannot be empty]");
        }

        if(StrUtil.isBlank(uploadFilename)){
            throw new DiscordMjJavaException("上传文件返回的文件名不能为空 [The uploadFilename cannot be empty]");
        }
    }

}
