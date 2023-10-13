package com.github.dmj.annotation;

import java.lang.annotation.*;

/**
 * @author ljjy1
 * @classname MjListener
 * @description TODO
 * @date 2023/10/13 18:21
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface MjMsgListener {

    /**
     * 用户key默认不填 获取全部消息
     * @return
     */
    String userKey() default "ALL";


}
