package cn.t.tool.netproxytool.socks5.config;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yj
 * @since 2020-05-26 20:04
 **/
public class ServerConfig {

    /**
     * 用户配置
     */
    private Map<String, UserConfig> userConfigMap = new HashMap<>();

    public Map<String, UserConfig> getUserConfigMap() {
        return userConfigMap;
    }

    public void setUserConfigMap(Map<String, UserConfig> userConfigMap) {
        this.userConfigMap = userConfigMap;
    }
}
