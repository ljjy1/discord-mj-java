package com.github.dmj.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author ljjy1
 * @classname UploadDiscordResponse
 * @date 2023/10/12 15:39
 */
@Data
@Accessors(chain = true)
public class UploadDiscordResponse implements Serializable {


    /**
     *  上传文件名
     */
    private String uploadFilename;

    /**
     * 上传文件返回的url
     */
    private String uploadUrl;

    /**
     * 业务系统唯一ID
     */
    private Integer triggerId;
}
