package com.github.dmj.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author ljjy1
 * @classname BaseRequest
 * @date 2023/10/12 11:07
 */
@Data
public class BaseRequest implements Serializable {

    /**
     * 账号key
     */
    protected String userKey;

    /**
     * 业务系统生成的唯一ID
     */
    protected Integer triggerId;


}
