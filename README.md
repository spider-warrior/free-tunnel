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
    
>4.本地主机启动端口监听
     
     java -jar free-tunnel-server.jar
     logs 文件在/home/{user.home}/logs

#### client

>1.打包可执行jar

>2.进入target目录

>3.启动服务java -jar free-tunnel-client.jar {server-ip}:10086 {username}:{password} gEIZCLU+48tkSknFfuE9kBCuKyhOrwnt54mJ3KX+uWE=

>4.修改浏览器代理地址为127.0.0.1:1087
    

#### 声明

    本项目纯做学习研究使用，不得用作其他不正当或违法乱纪行为


  
    
    
    
