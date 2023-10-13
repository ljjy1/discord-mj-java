package com.github.dmj.error;

import cn.hutool.core.util.StrUtil;
import lombok.*;


/**
 * @author ljjy1
 * @classname DiscordMjJavaException
 * @description 自定义异常类
 * @date 2023/10/11 9:59
 */
@Getter
public class DiscordMjJavaException extends RuntimeException {

    private static final long serialVersionUID = 7869786563361406291L;

    /**
     * 错误代码
     */
    private int errorCode;

    /**
     * 错误信息.
     */
    private String errorMsg;


    public DiscordMjJavaException(Throwable e){
        super(e);
    }

    public DiscordMjJavaException(int errorCode,String errorMsg){
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public DiscordMjJavaException(int errorCode,String errorMsg,Object...params){
        this.errorCode = errorCode;
        this.errorMsg = StrUtil.format(errorMsg,params);
    }

    public DiscordMjJavaException(String errorMsg){
        this.errorCode = 400;
        this.errorMsg = errorMsg;
    }

    public DiscordMjJavaException(String errorMsg,Object...params){
        this.errorCode = 400;
        this.errorMsg = StrUtil.format(errorMsg,params);
    }

}
