package cn.t.freetunnel.server.socks5;

import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.server.constants.Socks5TunnelServerConfig;
import cn.t.freetunnel.server.util.InitializerBuilder;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;
import cn.t.util.common.StringUtil;
import cn.t.util.common.SystemUtil;
import cn.t.util.io.FileUtil;
import cn.t.util.security.message.base64.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * 代理服务器
 * <a href="https://www.cnblogs.com/cc11001100/p/9949729.html">自己动手开发Socks5代理服务器</a>
 * <a href="https://www.ietf.org/rfc/rfc1928.txt">SOCKS Protocol Version 5</a>
 *
 * 使用socks5代理的坑，域名在本地解析还是在代理服务器端解析，有些比如google.com就必须在代理服务器端解析
 * <a href="https://blog.emacsos.com/use-socks5-proxy-in-curl.html">How to Use Socks5 Proxy in Curl</a>
 *
 * @author yj
 * @since 2020-01-12 13:41
 **/
public class Socks5TunnelServer {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelServer.class);

    public static void main(String[] args) {
        loadServerConfig();
        List<DaemonService> daemonServerList = new ArrayList<>();
        NettyTcpChannelInitializer nettyChannelInitializer = InitializerBuilder.buildSocks5ProxyServerChannelInitializer();
        NettyTcpServer proxyServer = new NettyTcpServer(String.format("socks5-proxy-server(%s:%s)", Socks5TunnelServerConfig.SERVER_HOST, Socks5TunnelServerConfig.SERVER_PORT), Socks5TunnelServerConfig.SERVER_PORT, nettyChannelInitializer, TunnelConstants.WORKER_GROUP, false, true);
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }

    public static void loadServerConfig() {
        String socks5Home = getSocks5Home();
        if(!StringUtil.isEmpty(socks5Home)) {
            File home = new File(socks5Home);
            if(home.exists()) {
                Map<String, Socks5TunnelServerConfig.UserConfig> userConfigMap = loadUserConfig(FileUtil.appendFilePath(socks5Home, Socks5TunnelServerConfig.SOCKS5_SERVER_USERS_CONFIG_FILE));
                Socks5TunnelServerConfig.ServerConfig.USER_CONFIG_MAP.putAll(userConfigMap);
            } else {
                logger.warn("{}未设置", Socks5TunnelServerConfig.SOCKS5_SERVER_HOME_KEY);
            }
        } else {
            logger.warn("{}未设置", Socks5TunnelServerConfig.SOCKS5_SERVER_HOME_KEY);
        }
    }

    private static String getSocks5Home() {
        return SystemUtil.getSysEnv(Socks5TunnelServerConfig.SOCKS5_SERVER_HOME_KEY);
    }

    private static Map<String, Socks5TunnelServerConfig.UserConfig> loadUserConfig(String userConfigLocation) {
        Map<String, Socks5TunnelServerConfig.UserConfig> userConfigMap = new HashMap<>();
        File config = new File(userConfigLocation);
        if(config.exists()) {
            try (
                FileInputStream fileInputStream = new FileInputStream(config)
            ) {
                Properties properties = new Properties();
                properties.load(fileInputStream);
                if(!properties.isEmpty()) {
                    properties.forEach((k, v) -> {
                        String passwordAndSecurity = (String)v;
                        String[] elements = passwordAndSecurity.split(":");
                        logger.info("添加用户, username: {}, password: {}, security: {}", k, elements[0], elements.length > 1 ? elements[1] : "");
                        Socks5TunnelServerConfig.UserConfig userConfig = new Socks5TunnelServerConfig.UserConfig();
                        userConfig.setUsername((String)k);
                        userConfig.setPassword(elements[0]);
                        if(elements.length > 1) {
                            userConfig.setSecurity(Base64Util.decode(elements[1].getBytes()));
                        }
                        userConfigMap.put(userConfig.getUsername(), userConfig);
                    });
                }
            } catch (IOException e) {
                logger.error("加载用户配置文件失败", e);
            }
        } else {
            logger.warn("用户配置文件不存在: {}", userConfigLocation);
        }
        return userConfigMap;
    }

}
