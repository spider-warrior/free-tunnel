#自由通道

### 功能简介
 - 透明http代理
 - 透明socks5代理
 - http加密隧道代理


## http加密隧道模式

### server

>1.购买主机

>2.环境配置
    
    install jdk11 or later
    设置环境变量TUNNEL_SERVER_HOME,并创建users文件
    export TUNNEL_SERVER_HOME=/home/{username}/.tunnelServerHome(or export TUNNEL_SERVER_HOME=~/.tunnelServerHome)
    users内容格式: {username}={password}:gEIZCLU+48tkSknFfuE9kBCuKyhOrwnt54mJ3KX+uWE=(格式:username=password:key)
    
>3.上传jar和lib包
    
>4.本地主机启动端口监听(logs文件在/home/{user.home}/logs)
```shell
#!/bin/bash
nohup java \
-Duser.timezone=Asia/Shanghai \
-Dfile.encoding=UTF-8 \
-Djava.net.preferIPv4Stack=true \
-Dlogback.configurationFile=defaultServerConfig/logback.xml \
-server \
-Xms64m \
-Xmx64m \
-XX:MetaspaceSize=32M \
-XX:MaxMetaspaceSize=32M \
-XX:CompressedClassSpaceSize=6m \
-XX:InitialCodeCacheSize=16m \
-XX:ReservedCodeCacheSize=16m \
-XX:MaxDirectMemorySize=64m \
-XX:+HeapDumpOnOutOfMemoryError \
-Xlog:gc:gc.log \
-XX:+UnlockDiagnosticVMOptions \
-XX:+LogVMOutput \
-XX:LogFile=vm.log \
-jar free-tunnel-server.jar >proxy-server.log 2>&1 &
tail -f proxy-server.log | grep -E 'WARN|ERROR'
```

#### client

>1.打包可执行jar

>2.进入target目录

>3.启动服务
```shell
#!/bin/bash
nohup java \
-Duser.timezone=Asia/Shanghai \
-Dfile.encoding=UTF-8 \
-Djava.net.preferIPv4Stack=true \
-Dlogback.configurationFile=defaultClientConfig/logback.xml \
-server \
-Xms64m \
-Xmx64m \
-XX:MetaspaceSize=32M \
-XX:MaxMetaspaceSize=32M \
-XX:CompressedClassSpaceSize=6m \
-XX:InitialCodeCacheSize=16m \
-XX:ReservedCodeCacheSize=16m \
-XX:MaxDirectMemorySize=64m \
-XX:+HeapDumpOnOutOfMemoryError \
-Xlog:gc:gc.log \
-XX:+UnlockDiagnosticVMOptions \
-XX:+LogVMOutput \
-XX:LogFile=vm.log \
-jar free-tunnel-client.jar 45.131.66.191:10086 admin:123456 gEIZCLU+48tkSknFfuE9kBCuKyhOrwnt54mJ3KX+uWE= >proxy-client.log 2>&1 &
tail -f proxy-client.log | grep -E 'WARN|ERROR'
```

>4.修改浏览器代理地址为127.0.0.1:1087
    

#### 声明

    本项目纯做学习研究使用，不得用作其他不正当或违法乱纪行为


  
    
    
    
