package cn.t.freetunnel.client.socks5.util;

import cn.t.freetunnel.client.socks5.constants.Socks5TunnelClientConfig;
import cn.t.freetunnel.client.socks5.exception.ClientInitFailed;
import cn.t.util.common.FileUtil;
import cn.t.util.common.JsonUtil;
import cn.t.util.common.StringUtil;
import cn.t.util.security.message.base64.Base64Util;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

public class Socks5TunnelClientConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelClientConfigUtil.class);
    private static final String clientConfigFileName = "free-tunnel-client.properties";
    private static final String clientConfPathKey = "clientConfPath";
    private static final String tunnelServerHostKey = "tunnelServerHost";
    private static final String tunnelServerPortKey = "tunnelServerPort";
    private static final String usernameKey = "username";
    private static final String passwordKey = "password";
    private static final String securityKey = "security";

    public static void initClientConfig(String[] args) throws IOException {
        if(args.length == 0) {
            configSocks5ClientConfig();
        } else if(args.length < 3) {
            throw new ClientInitFailed("参数格式: host:port username:password security");
        } else {
            analyseAndConfigSocks5Server(args[0]);
            analyseAndConfigUser(args[1]);
            analyseAndConfigSecurity(args[2]);
        }
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

    private static String analysePath(String path) {
        if(path.contains("~")) {
            return path.replaceAll("~", System.getProperty("user.home").replaceAll("\\\\", "/"));
        } else {
            return path;
        }
    }

    private static void configSocks5ClientConfig() throws IOException {
        logger.info("init client by config file: classpath:{}", clientConfigFileName);
        try (
            InputStream inputStream = FileUtil.getResourceInputStream(Socks5TunnelClientConfigUtil.class.getClassLoader(), clientConfigFileName)
        ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            String clientConfPath = properties.getProperty(clientConfPathKey);
            if(StringUtil.isEmpty(clientConfPath)) {
                throw new ClientInitFailed("free-tunnel-client.properties加载失败, 配置不合法: " + clientConfPathKey);
            } else {
                byte[] data = FileUtil.getFileBytes(new File(clientConfPath));
                if(data.length == 0) {
                    throw new ClientInitFailed("clientConfig文件内容为空: " + clientConfPath);
                } else {
                    Map<String, String> configMap = JsonUtil.deserialize(new String(data), new TypeReference<Map<String, String>>(){});
                    Socks5TunnelClientConfig.socks5ServerHost = checkAndGetClientConfig(configMap, tunnelServerHostKey);
                    Socks5TunnelClientConfig.socks5ServerPort = Integer.parseInt(checkAndGetClientConfig(configMap, tunnelServerPortKey));
                    Socks5TunnelClientConfig.username = checkAndGetClientConfig(configMap, usernameKey);
                    Socks5TunnelClientConfig.password = checkAndGetClientConfig(configMap, passwordKey);
                    Socks5TunnelClientConfig.security = Base64Util.decode(checkAndGetClientConfig(configMap, securityKey).getBytes());
                }
            }
        }
    }

    private static String checkAndGetClientConfig(Map<String, String> configMap, String key) {
        String value = configMap.get(key).trim();
        if(StringUtil.isEmpty(value)) {
            throw new ClientInitFailed("clientConfig配置错误key: " + key);
        }
        return value;
    }
}
