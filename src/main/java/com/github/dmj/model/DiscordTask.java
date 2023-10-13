package com.github.dmj.model;

import java.util.function.Function;

/**
 * @author ljjy1
 * @classname DiscordTask
 * @date 2023/10/11 17:01
 */
public class DiscordTask<T,S> {



    private Function<T,S> function;

    /**
     * 参数列表
     * @param function
     */
    private T param;

    public DiscordTask(Function<T, S> function,T param) {
        this.function = function;
        this.param = param;
    }

    /**
     * 运行方法
     */
    public void run(){
        function.apply(param);
    }
}
