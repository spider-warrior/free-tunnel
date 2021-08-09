package cn.t.tool.netproxytool.socks5.util;

import cn.t.tool.netproxytool.socks5.config.ServerConfig;
import cn.t.tool.netproxytool.socks5.config.UserConfig;
import cn.t.tool.netproxytool.socks5.constants.Socks5ServerDaemonConfig;
import cn.t.util.common.StringUtil;
import cn.t.util.common.SystemUtil;
import cn.t.util.io.FileUtil;
import cn.t.util.security.message.base64.Base64Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author yj
 * @since 2020-05-26 20:37
 **/
public class ServerConfigUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServerConfigUtil.class);

    public static ServerConfig loadServerConfig() {
        ServerConfig serverConfig = new ServerConfig();
        String socks5Home = getSocks5Home();
        if(!StringUtil.isEmpty(socks5Home)) {
            File home = new File(socks5Home);
            if(home.exists()) {
                Map<String, UserConfig> userConfigMap = loadUserConfig(FileUtil.appendFilePath(socks5Home, Socks5ServerDaemonConfig.SOCKS5_SERVER_USERS_CONFIG_FILE));
                serverConfig.setUserConfigMap(userConfigMap);
            } else {
                logger.warn("{}未设置", Socks5ServerDaemonConfig.SOCKS5_SERVER_HOME_KEY);
            }
        } else {
            logger.warn("{}未设置", Socks5ServerDaemonConfig.SOCKS5_SERVER_HOME_KEY);
        }
        return serverConfig;
    }

    private static String getSocks5Home() {
        return SystemUtil.getSysEnv(Socks5ServerDaemonConfig.SOCKS5_SERVER_HOME_KEY);
    }

    private static Map<String, UserConfig> loadUserConfig(String userConfigLocation) {
        Map<String, UserConfig> userConfigMap = new HashMap<>();
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
                        UserConfig userConfig = new UserConfig();
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
