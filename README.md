###自由通道

#### proxy-server 启动步骤
>1.购买主机

>2.环境配置
    
    jdk11
    设置环境变量SOCKS5_HOME,并创建users文件
    export SOCKS5_HOME=/home/{username}/.socks5
    users内容格式: {username}={password}:gEIZCLU+48tkSknFfuE9kBCuKyhOrwnt54mJ3KX+uWE=(username=password:key)
    

>3.上传jar和lib包
    


>4.本地主机启动端口监听
     
     java -jar net-proxy.jar
     logs 文件在/home/{user}/logs

#### proxy-client 启动步骤
>1.打包可执行jar

>2.进去target目录

>3.启动服务java -cp net-proxy.jar cn.t.tool.netproxytool.http.HttpProxyServerViaSocks5 {proxy-server-ip}:10086 {username}:{password} gEIZCLU+48tkSknFfuE9kBCuKyhOrwnt54mJ3KX+uWE=

>4.修改浏览器代理地址为127.0.0.1:1080
    

#### 注意

    本项目纯做学习研究使用，不得用作其他不正当或违法乱纪行为


  
    
    
    
