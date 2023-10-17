# discord-mj-spring-boot-starter

> 基于 Discord 的 Midjourney API springboot中间件

## 前提条件

- **这些自己提前准备好不懂就找万能的百度/谷歌**
- 魔法(项目非必须 可使用代理,只不过要设置以下条件需要登录到discord网站)
- discord账号
- midjourney账号
- 创建discord自己服务器且把midjourney机器人加入服务器
- 创建服务器频道
- 创建自己服务器机器人并加入自己服务器 (创建机器人必须配置读取消息的权限)

## 项目接入

```text
1.下载项目
2.maven install 到本地仓库 或者打包到自己的私有仓库
3.引入项目
```

```pom
        <dependency>
            <groupId>com.github.dmj</groupId>
            <artifactId>discord-mj-spring-boot-starter</artifactId>
            <version>1.0.0</version>
        </dependency>
```

### 配置信息

> 如果是properties配置 自己转换下

```yml
discord:
  # 是否开启discord
  enable: true
  # discord账户列表key 自定义(不重复即可) 多个使用英文逗号分割
  userKeyList: user1,user2
  # user1自定义的key
  user1:
    userKey: user1
    # discord 登录token
    userToken: xxxx
    # discord 账号 与discord 登录token有一个配置即可
    user:
    # discord 密码 与discord 登录token有一个配置即可
    password:
    # 创建的机器人token
    botToken: xxxx
    # 服务器ID
    guildId: 1160515508215496725
    # 频道ID
    channelId: 1160756682226413598
    # 并发执行的任务个数 (单个discord同时发布多个绘图任务会提示并发过大 目前我只有同步发布5个出现过)
    concurSize: 3
    # 等待执行的个数 (超过将会抛弃任务)
    waitSize: 10

    #第二个账号配置  没有多账号不需要配置(这些key都需要删除 否则会抛出异常)
    user2:
      userKey: user2
      # discord 登录token
      userToken: xxxx
      # discord 账号 与discord 登录token有一个配置即可
      user:
      # discord 密码 与discord 登录token有一个配置即可
      password:
      # 创建的机器人token
      botToken: xxxx
      # 服务器ID
      guildId: 1160515508215496725
      # 频道ID
      channelId: 1160756682226413598
      # 并发执行的任务个数 (单个discord同时发布多个绘图任务会提示并发过大 目前我只有同步发布5个出现过)
      concurSize: 3
      # 等待执行的个数 (超过将会抛弃任务)
      waitSize: 10
  proxy:
    # 是否开启代理
    enable: true
    # 代理IP
    address: 127.0.0.1
    # 代理端口
    port: 7890
```

### 配置的获取方式

#### userToken获取

> 账号token

登录discord 进入自己频道随便发送一条消息 查看一条请求

<img src="https://img.irelax.top/img/screenshot-20231017-095716.png" style="zoom:200%;" />

#### user

> discord账号

由于userToken是会变动,暂时我也不确定 多久过期 我之前测试配置的5天还能用 所以兼容了实现账号获取token 与userToken二选一配置即可

#### password

> discord密码

#### botToken

[配置discord应用和机器人 ]([Discord Developer Portal — My Applications](https://discord.com/developers/applications) )

```text
https://discord.com/developers/applications
```

打开自己的应用配置界面 进入应用

![image-20231017113216158](https://img.irelax.top/img/image-20231017113216158.png)

![image-20231017113300674](https://img.irelax.top/img/image-20231017113300674.png)

刷新后会有一个复制token操作 获取即可

#### guildId

> 服务器ID

![image-20231017130643041](https://img.irelax.top/img/image-20231017130643041.png)

#### channelId

> 频道ID

![image-20231017130643041](https://img.irelax.top/img/image-20231017130643041.png)

### 案例代码

地址  [ljjy1/discord-mj-java-test: 测试中间件使用 (github.com)](https://github.com/ljjy1/discord-mj-java-test)

#### 启动成功样式

![image-20231017145014039](https://img.irelax.top/img/image-20231017145014039.png)

#### 调用生成图片接口

![image-20231017145416657](https://img.irelax.top/img/image-20231017145416657.png)

#### 监听到消息

![image-20231017145238327](https://img.irelax.top/img/image-20231017145238327.png)

### 其他需要自行处理的问题

1. 一些敏感的关键词 需要自行在调用接口前 过滤 否则调用接口会报错,且自建的机器人监听不到消息 (
   discord发送的敏感消息只能账号本身可见)  (建议用数据库或者其他在自己本地存储起来)
2. 关键词对中文不太友好 建议传入接口之前 先用网易/或者其他翻译的开放API翻译成英文 传入接口



