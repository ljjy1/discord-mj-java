package com.github.dmj.service;


import cn.hutool.core.util.StrUtil;
import com.github.dmj.error.DiscordMjJavaException;
import com.github.dmj.model.*;
import com.github.dmj.queue.TaskQueue;
import com.github.dmj.service.api.DiscordApi;
import com.github.dmj.util.UniqueUtil;

import java.io.File;
import java.net.URLConnection;
import java.util.Map;


/**
 * @author ljjy1
 * @classname DiscordService
 * @description  API接口服务
 * @date 2023/10/11 16:31
 */

public class DiscordService {

    private final Map<String, DiscordApi> discordApiMap;

    public DiscordService(Map<String, DiscordApi> discordApiMap) {
        this.discordApiMap = discordApiMap;
    }

    /**
     * 文生图/图生图 返回唯一id 用于获取后续图片信息 对应机器人命令 /imagine
     */
    public Integer imagine(ImagineInRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerImagine,request);
        return triggerId;
    }

    /**
     * 图片细节增强 对应图片U1 U2 U3 U4
     * @return
     */
    public Integer upscale(UpscaleVariationRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerUpscale,request);
        return triggerId;
    }


    /**
     * 图片变化 对应图片 V1 V2 V3 V4
     */
    public Integer variation(UpscaleVariationRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerVariation,request);
        return triggerId;
    }


    /**
     * 图片重绘 对应刷新按钮
     */
    public Integer reset(ResetRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerReset,request);
        return triggerId;
    }


    /**
     * 单张图片 微改变Subtle
     */
    public Integer soloLowVariation(SoloVariationRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerSoloLowVariation,request);
        return triggerId;
    }

    /**
     * 单张图片 较大改变Strong
     */
    public Integer soloHighVariation(SoloVariationRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerSoloHighVariation,request);
        return triggerId;
    }

    /**
     * 对单张图片进行缩小操作zoomout(2x:50 1.5X 75)
     */
    public Integer zoomOut(ZoomOutRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerZoomOut,request);
        return triggerId;
    }

    /**
     * 图片进行某方向的扩展 (left/right/up/down)
     */
    public Integer expand(ExpandRequest request){
        request.check();
        String userKey = request.getUserKey();
        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = request.getTriggerId();
        //加入队列
        TaskQueue.getInstance().putTask(userKey,discordApi::triggerExpand,request);
        return triggerId;
    }


    /**
     * 上传文件
     * @param userKey
     * @param file
     * @return
     */
    public UploadDiscordResponse uploadFileToDiscord(String userKey, File file){
        if(file == null || file.length() == 0){
            throw new DiscordMjJavaException("上传的文件不能为空,且需要有数据 [The file to be uploaded cannot be empty and must contain data]");
        }
        if(StrUtil.isBlank(userKey)){
            throw new DiscordMjJavaException("用户key不能为空 [The userKey cannot be empty]");
        }
        //判断文件需要是图片
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        if(!contentType.startsWith("image/")){
            throw new DiscordMjJavaException("上传的文件必须为图片类型 [The file to be uploaded must be an image]");
        }
        String uuid = StrUtil.uuid().replace("-", "");
        String fileName = uuid+".png";

        DiscordApi discordApi = discordApiMap.get(userKey);
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",userKey,userKey);
        }
        Integer triggerId = UniqueUtil.generateUniqueId();
        Map<String, String> map = discordApi.uploadFile(file, fileName);
        return new UploadDiscordResponse().setUploadUrl(map.get("uploadUrl")).setUploadFilename(map.get("uploadFilename")).setTriggerId(triggerId);
    }


    /**
     * 获取文件下载链接
     * @return
     */
    public String getUploadFileUrl(GetUploadFileRequest request){
        request.check();
        DiscordApi discordApi = discordApiMap.get(request.getUserKey());
        if(discordApi == null){
            throw new DiscordMjJavaException("未找到对应用户key{}配置 [The corresponding user key:{} configuration is not found]",request.getUserKey(),request.getUserKey());
        }
        return discordApi.message(request.getUploadFilename());
    }














}
