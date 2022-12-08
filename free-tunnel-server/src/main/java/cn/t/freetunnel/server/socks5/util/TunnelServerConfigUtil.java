package cn.t.freetunnel.server.socks5.util;

import cn.t.freetunnel.server.socks5.TunnelServerConfig;
import cn.t.util.common.FileUtil;
import cn.t.util.common.StringUtil;
import cn.t.util.common.SystemUtil;
import cn.t.util.security.message.base64.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class TunnelServerConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(TunnelServerConfigUtil.class);

    public static void initServerConfig() {
        String tunnelServerHomePath = getTunnelServerHomePath();
        if(!StringUtil.isEmpty(tunnelServerHomePath)) {
            File tunnelServerHome = new File(tunnelServerHomePath);
            if(tunnelServerHome.exists()) {
                Map<String, TunnelServerConfig.UserConfig> userConfigMap = loadUserConfig(FileUtil.appendFilePath(tunnelServerHomePath, TunnelServerConfig.TUNNEL_SERVER_USERS_CONFIG_FILE));
                TunnelServerConfig.USER_CONFIG_MAP.putAll(userConfigMap);
            } else {
                logger.warn("tunnelServerHome文件不存在: " + tunnelServerHomePath);
            }
        } else {
            logger.warn("tunnelServerHome环境变量未设置");
        }
    }

    private static String getTunnelServerHomePath() {
        return SystemUtil.getSysEnv(TunnelServerConfig.TUNNEL_SERVER_HOME_KEY);
    }

    private static Map<String, TunnelServerConfig.UserConfig> loadUserConfig(String userConfigLocation) {
        Map<String, TunnelServerConfig.UserConfig> userConfigMap = new HashMap<>();
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
                        TunnelServerConfig.UserConfig userConfig = new TunnelServerConfig.UserConfig();
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
