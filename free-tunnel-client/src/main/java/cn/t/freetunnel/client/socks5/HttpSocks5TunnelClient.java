package cn.t.freetunnel.client.socks5;

import cn.t.freetunnel.client.socks5.constants.HttpSocks5TunnelClientConfig;
import cn.t.freetunnel.client.socks5.util.InitializerBuilder;
import cn.t.freetunnel.client.socks5.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;
import cn.t.util.common.FileUtil;
import cn.t.util.common.JsonUtil;
import cn.t.util.common.StringUtil;
import cn.t.util.security.message.base64.Base64Util;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * http代理服务器作socks5客户端
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:05
 **/
public class HttpSocks5TunnelClient {

    private static final Logger logger = LoggerFactory.getLogger(HttpSocks5TunnelClient.class);

    public static void main(String[] args) {
        if(args.length == 0) {
            configSocks5ClientConfig();
        } else if(args.length < 3) {
            System.err.println("参数格式: host:port username:password security");
            System.exit(1);
        } else {
            analyseAndConfigSocks5Server(args[0]);
            analyseAndConfigUser(args[1]);
            analyseAndConfigSecurity(args[2]);
        }
        NettyTcpChannelInitializer nettyChannelInitializer = InitializerBuilder.httpSocks5TunnelClientInitializer();
        NettyTcpServer proxyServer = new NettyTcpServer("http-socks5-tunnel-client", HttpSocks5TunnelClientConfig.SERVER_PORT, nettyChannelInitializer, TunnelConstants.WORKER_GROUP, false, true);
        List<DaemonService> daemonServerList = new ArrayList<>();
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }

    private static void analyseAndConfigSocks5Server(String socks5ServerElement) {
        if(StringUtil.isEmpty(socks5ServerElement)) {
            return;
        }
        String[] elements = socks5ServerElement.split(":");
        Socks5TunnelClientConfig.socks5ServerHost = elements[0];
        if(elements.length > 1) {
            Socks5TunnelClientConfig.socks5ServerPort = Integer.parseInt(elements[1]);
        }
    }

    private static void analyseAndConfigUser(String userElement) {
        if(StringUtil.isEmpty(userElement)) {
            return;
        }
        String[] elements = userElement.split(":");
        Socks5TunnelClientConfig.username = elements[0];
        if(elements.length > 1) {
            Socks5TunnelClientConfig.password = elements[1];
        }
    }

    private static void analyseAndConfigSecurity(String security) {
        if(StringUtil.isEmpty(security)) {
            return;
        }
        Socks5TunnelClientConfig.security = Base64Util.decode(security.getBytes());
    }

    private static void configSocks5ClientConfig() {
        try (
            InputStream inputStream = FileUtil.getResourceInputStream(HttpSocks5TunnelClient.class.getClassLoader(), "free-tunnel-client.properties")
        ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String clientConfPath = properties.getProperty("clientConfPath");
            if(StringUtil.isEmpty(clientConfPath)) {
                System.err.println("free-tunnel-client.properties加载失败, clientConfPath不存在");
                System.exit(1);
            } else {
                try (
                    InputStream clientConfPathInputStream = Files.newInputStream(Paths.get(analysePath(clientConfPath)));
                ) {
                    byte[] data = new byte[clientConfPathInputStream.available()];
                    int length = clientConfPathInputStream.read(data);
                    if(length == 0) {
                        System.err.println("clientConf内容为空, 目录: " + clientConfPath);
                        System.exit(1);
                    } else {
                        Map<String, String> configMap = JsonUtil.deserialize(new String(data), new TypeReference<Map<String, String>>(){});
                        Socks5TunnelClientConfig.socks5ServerHost = configMap.get("socks5ServerHost").trim();
                        Socks5TunnelClientConfig.socks5ServerPort = Integer.parseInt(configMap.get("socks5ServerPort").trim());
                        Socks5TunnelClientConfig.username = configMap.get("username").trim();
                        Socks5TunnelClientConfig.password = configMap.get("password").trim();
                        Socks5TunnelClientConfig.security = Base64Util.decode(configMap.get("security").getBytes());
                    }
                } catch (Exception e) {
                    System.err.println("clientConf配置异常, 目录: " + clientConfPath);
                    System.exit(1);
                }
            }
        } catch (Exception e) {
            logger.error("free-tunnel-client.properties加载失败", e);
            System.exit(1);
        }
    }

    private static String analysePath(String path) {
        if(path.contains("~")) {
            return path.replaceAll("~", System.getProperty("user.home").replaceAll("\\\\", "/"));
        } else {
            return path;
        }
    }
}
