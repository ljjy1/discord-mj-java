package com.github.dmj.service.api;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.dmj.autoconfigure.DiscordAccountProperties;
import com.github.dmj.autoconfigure.DiscordProxyProperties;
import com.github.dmj.constant.Constants;
import com.github.dmj.error.DiscordMjJavaException;
import com.github.dmj.model.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author ljjy1
 * @classname DiscordApi
 * @description  API接口
 * @date 2023/10/11 10:55
 */
@Slf4j
public class DiscordApi {

    /**
     * 连接超时时间 单位毫秒
     */
    private final Integer connectTimeOut = 5000;


    private final String uploadAttachmentUrl;
    private final String sendMessageUrl;

    private final String triggerUrl = Constants.TRIGGER_URL;

    private final DiscordAccountProperties discordAccountProperties;


    private final DiscordProxyProperties discordProxyProperties;

    private final Cache<String, String> cache;


    private final String version = "1118961510123847772";
    private final String id = "938956540159881230";
    private final String applicationId = "936929561302675456";
    private final String sessionId = "7adb7b9360a4ee4fea41aecad803f1d9";

    public DiscordApi(DiscordAccountProperties discordAccountProperties,DiscordProxyProperties discordProxyProperties) {
        this.discordAccountProperties = discordAccountProperties;
        this.discordProxyProperties = discordProxyProperties;
        uploadAttachmentUrl = StrUtil.format(Constants.UPLOAD_ATTACHMENT_URL, discordAccountProperties.getChannelId());
        sendMessageUrl = StrUtil.format(Constants.SEND_MESSAGE_URL, discordAccountProperties.getChannelId());

        cache = CacheBuilder.newBuilder()
                //设置最大500容量
                .maximumSize(10)
                // 根据写入时间设置6个小时逐出
                .expireAfterWrite(6, TimeUnit.HOURS)
                .build();
    }



    private String getUserToken(){
        if(StrUtil.isNotBlank(discordAccountProperties.getUserToken())){
            return discordAccountProperties.getUserToken();
        }
        if(StrUtil.hasBlank(discordAccountProperties.getUser(),discordAccountProperties.getPassword())){
            throw new DiscordMjJavaException("请确认是否正确配置了账号token或者账号密码 [Check whether the account token or password is correctly configured]");
        }

        //从缓存中获取token
        String userToken = cache.getIfPresent("userToken");
        if(StrUtil.isBlank(userToken)){

            HttpRequest post = HttpUtil.createPost(Constants.LOGIN).timeout(connectTimeOut)
                    .header("Content-Type", "application/json");
            //判断是否使用代理 与是否请求 discord.com的网站
            if (discordProxyProperties.isEnable() && StrUtil.containsAnyIgnoreCase(Constants.LOGIN, "discord.com")) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(discordProxyProperties.getAddress(), discordProxyProperties.getPort()));
                post.setProxy(proxy);
            }

            Map<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("gift_code_sku_id",null);
            paramsMap.put("login",discordAccountProperties.getUser());
            paramsMap.put("login_source",null);
            paramsMap.put("password",discordAccountProperties.getPassword());
            paramsMap.put("undelete",null);
            HttpResponse response = null;
            try {
                response = post.body(JSONUtil.toJsonStr(paramsMap)).execute();
                int status = response.getStatus();
                if(status != 200){
                    throw new DiscordMjJavaException("调用登录API不成功,状态:{}",status);
                }
                String body = response.body();
                if(StrUtil.isBlank(body)){
                    throw new DiscordMjJavaException("调用登录API不成功,响应body为空");
                }
                //获取响应token
                JSONObject jsonObject = JSONUtil.parseObj(body);
                String token = jsonObject.getStr("token");
                if(StrUtil.isBlank(token)){
                    throw new DiscordMjJavaException("调用登录API不成功,响应body内token为空");
                }
                userToken = token;
                //存入缓存
                cache.put("userToken",userToken);
            } catch (Exception e) {
                throw new DiscordMjJavaException("discord登录异常:{}",e.getMessage());
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
        return userToken;
    }



    /**
     * 封装get统一请求头和参数
     *
     * @return
     */
    public HttpRequest requestGet(String url) {
        HttpRequest get = HttpUtil.createGet(url).timeout(connectTimeOut)
                .header("Content-Type", "application/json")
                .header("Authorization", getUserToken());
        //判断是否使用代理 与是否请求 discord.com的网站
        if (discordProxyProperties.isEnable() && StrUtil.containsAnyIgnoreCase(url, "discord.com")) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(discordProxyProperties.getAddress(), discordProxyProperties.getPort()));
            get.setProxy(proxy);
        }

        return get;
    }

    /**
     * 封装post统一请求头和参数
     *
     * @return
     */
    public HttpRequest requestPostJson(String url) {
        HttpRequest post = HttpUtil.createPost(url).timeout(connectTimeOut)
                .header("Content-Type", "application/json")
                .header("Authorization", getUserToken());
        //判断是否使用代理 与是否请求 discord.com的网站
        if (discordProxyProperties.isEnable() && StrUtil.containsAnyIgnoreCase(url, "discord.com")) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(discordProxyProperties.getAddress(), discordProxyProperties.getPort()));
            post.setProxy(proxy);
        }
        return post;
    }

    /**
     * 封装post统一请求头和参数
     *
     * @return
     */
    public HttpRequest requestPostForm(String url) {
        HttpRequest post = HttpUtil.createPost(url).timeout(connectTimeOut)
                .header("Content-Type", "multipart/form-data")
                .header("Authorization", getUserToken());
        //判断是否使用代理 与是否请求 discord.com的网站
        if (discordProxyProperties.isEnable() && StrUtil.containsAnyIgnoreCase(url, "discord.com")) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(discordProxyProperties.getAddress(), discordProxyProperties.getPort()));
            post.setProxy(proxy);
        }
        return post;
    }

    @NotNull
    private Map<String, Object> getTriggerParams(Integer type, Map<String, Object> data) {
        Map<String, Object> params = new HashMap<>();
        params.put("type", type);
        params.put("application_id", applicationId);
        params.put("session_id", sessionId);
        params.put("guild_id", discordAccountProperties.getGuildId());
        params.put("channel_id", discordAccountProperties.getChannelId());
        params.put("data", data);
        return params;
    }

    /**
     * 文生图/ 图生图
     *
     * @param triggerImagineInRequest
     * @return
     */
    public String triggerImagine(ImagineInRequest triggerImagineInRequest) {
        HttpRequest postRequest = requestPostForm(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("version", version);
            dataMap.put("id", id);
            dataMap.put("name", "imagine");
            dataMap.put("type", 1);
            List<Map<String, Object>> options = new ArrayList<>();
            Map<String, Object> option = new HashMap<>();
            option.put("type", 3);
            option.put("name", "prompt");
            option.put("value", triggerImagineInRequest.getPrompt());
            options.add(option);
            dataMap.put("options", options);
            dataMap.put("attachments", new ArrayList<>());
            ;
            Map<String, Object> params = getTriggerParams(2, dataMap);
            postRequest.form("payload_json", JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    /**
     * 图片细节增强
     *
     * @param upscaleVariationRequest
     * @return
     */
    public String triggerUpscale(UpscaleVariationRequest upscaleVariationRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::JOB::upsample::{}::{}", upscaleVariationRequest.getIndex(), upscaleVariationRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", upscaleVariationRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    /**
     * 图片细节变化
     *
     * @param upscaleVariationRequest
     * @return
     */
    public String triggerVariation(UpscaleVariationRequest upscaleVariationRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::JOB::variation::{}::{}", upscaleVariationRequest.getIndex(), upscaleVariationRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", upscaleVariationRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    /**
     * 图片重绘
     */
    public String triggerReset(ResetRequest resetRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::JOB::reroll::0::{}::SOLO", resetRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", resetRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    /**
     * 单张图片 微改变Subtle
     */
    public String triggerSoloLowVariation(SoloVariationRequest soloVariationRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::JOB::low_variation::1::{}::SOLO", soloVariationRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", soloVariationRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    /**
     * 单张图片 较大改变Strong
     */
    public String triggerSoloHighVariation(SoloVariationRequest soloVariationRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::JOB::high_variation::1::{}::SOLO", soloVariationRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", soloVariationRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    /**
     * 单张图片 进行缩小操作zoomout(2x:50 1.5X 75)
     */
    public String triggerZoomOut(ZoomOutRequest zoomOutRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::Outpaint::{}::1::{}::SOLO", zoomOutRequest.getZoomout(), zoomOutRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", zoomOutRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }


    /**
     * 单张图片 图片进行某方向的扩展 (left/right/up/down)
     */
    public String triggerExpand(ExpandRequest expandRequest) {
        HttpRequest postRequest = requestPostJson(triggerUrl);
        HttpResponse response = null;
        try {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("component_type", 2);
            String customId = StrUtil.format("MJ::JOB::pan_{}::1::{}::SOLO", expandRequest.getDirection(), expandRequest.getMsgHash());
            dataMap.put("custom_id", customId);

            Map<String, Object> params = getTriggerParams(3, dataMap);
            params.put("message_flags", 0);
            params.put("message_id", expandRequest.getMsgId());

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
        return null;
    }

    /**
     * 上传文件 返回服务器文件名和 文件上传连接(用户上传的url)
     * @param file
     * @param fileName
     * @return
     */
    public Map<String,String> uploadFile(File file, String fileName) {
        HttpRequest postRequest = requestPostJson(uploadAttachmentUrl);
        HttpResponse response = null;
        HttpResponse response2 = null;
        Map<String,String> returnMap = new HashMap<>();
        try {
            Map<String, Object> params = new HashMap<>();
            ArrayList<Map<String, Object>> files = new ArrayList<>();

            Map<String, Object> fileMap = new HashMap<>();
            fileMap.put("filename", fileName);
            fileMap.put("file_size", file.length());
            fileMap.put("id", "0");
            files.add(fileMap);
            params.put("files", files);

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();

            if(response.getStatus() != 200){
                throw new DiscordMjJavaException("预上传文件到discord状态异常 [The pre-upload file to discord is abnormal. Procedure]");
            }

            String body = response.body();
            if(StrUtil.isBlank(body)){
                throw new DiscordMjJavaException("预上传文件到discord 返回体为空 [The return body of a pre-uploaded file to discord is empty]");
            }
            System.out.println(body);

            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray attachments = jsonObject.getJSONArray("attachments");
            if(attachments.isEmpty()){
                throw new DiscordMjJavaException("预上传文件到discord 返回的attachments为空 [The attachments returned for pre-uploading files to discord are empty]");
            }
            JSONObject attachment = attachments.getJSONObject(0);
            String uploadUrl = attachment.getStr("upload_url");
            String uploadFilename = attachment.getStr("upload_filename");

            //上传文件
            // 构建 HTTP 请求
            HttpRequest putRequest = HttpRequest.put(uploadUrl).timeout(30000);
            putRequest.header("Content-Type","image/png");
            // 设置请求体为文件内容
            putRequest.body(FileUtil.readBytes(file));

            // 发送请求并获取响应
            response2 = putRequest.execute();

            if(response2.getStatus() != 200){
                throw new DiscordMjJavaException("上传文件到discord状态异常 [The upload file to discord is abnormal. Procedure]");
            }
            returnMap.put("uploadUrl",uploadUrl);
            returnMap.put("uploadFilename",uploadFilename);
            return returnMap;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
            if (response2 != null) {
                response2.close();
            }
        }
    }


    /**
     * 通过文件名 获取文件地址链接
     */
    public String message(String uploadFilename){
        HttpRequest postRequest = requestPostJson(sendMessageUrl);
        HttpResponse response = null;
        try {

            Map<String, Object> params = new HashMap<>();
            params.put("content","");
            params.put("nonce","");
            params.put("channel_id","1105829904790065223");
            params.put("type",0);
            params.put("sticker_ids",new ArrayList<>());

            List<Map<String,Object>> attachments = new ArrayList<>();
            Map<String,Object> attachment = new HashMap<>();

            attachment.put("id","0");
            String[] split = uploadFilename.split("/");
            attachment.put("filename",split[split.length-1]);
            attachment.put("uploaded_filename",uploadFilename);
            attachments.add(attachment);
            params.put("attachments",attachments);

            postRequest.body(JSONUtil.toJsonStr(params));
            response = postRequest.execute();

            if(response.getStatus() != 200){
                throw new DiscordMjJavaException("获取文件链接状态异常 [The file link status is abnormal. Procedure]");
            }

            String body = response.body();
            if(StrUtil.isBlank(body)){
                throw new DiscordMjJavaException("获取文件链接状态异常 返回体为空 [Obtain file link status Abnormal Return box is empty]");
            }
            System.out.println(body);

            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONArray attachmentsArr = jsonObject.getJSONArray("attachments");
            if(attachmentsArr.isEmpty()){
                throw new DiscordMjJavaException("获取文件链接 返回的attachments为空 [The attachments returned for the document link are empty]");
            }
            JSONObject attachmentObj = attachmentsArr.getJSONObject(0);
            return attachmentObj.getStr("url");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            // 手动关闭连接
            if (response != null) {
                response.close();
            }
        }
    }





}
