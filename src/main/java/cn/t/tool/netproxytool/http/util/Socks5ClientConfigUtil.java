package cn.t.tool.netproxytool.http.util;

import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.util.common.StringUtil;
import cn.t.util.security.message.base64.Base64Util;

/**
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-05-24 09:31
 **/
public class Socks5ClientConfigUtil {

    /**
     * 解析main入参字符串
     */
    public static Socks5ClientConfig analyseMainParam(String param) {
        if(StringUtil.isEmpty(param)) {
            return null;
        }
        Socks5ClientConfig socks5ClientConfig = new Socks5ClientConfig();
        String[] elements = param.split(" ");
        String socks5ServerElement = elements[0];
        String userElement = elements[1];
        String security = elements[2];
        analyseAndConfigSocks5Server(socks5ClientConfig, socks5ServerElement);
        analyseAndConfigUser(socks5ClientConfig, userElement);
        analyseAndConfigSecurity(socks5ClientConfig, security);
        return socks5ClientConfig;
    }

    public static void analyseAndConfigSocks5Server(Socks5ClientConfig socks5ClientConfig, String socks5ServerElement) {
        if(StringUtil.isEmpty(socks5ServerElement)) {
            return;
        }
        String[] elements = socks5ServerElement.split(":");
        socks5ClientConfig.setSocks5ServerHost(elements[0]);
        if(elements.length > 1) {
            socks5ClientConfig.setSocks5ServerPort(Integer.parseInt(elements[1]));
        }
    }

    public static void analyseAndConfigUser(Socks5ClientConfig socks5ClientConfig, String userElement) {
        if(StringUtil.isEmpty(userElement)) {
            return;
        }
        String[] elements = userElement.split(":");
        socks5ClientConfig.setUsername(elements[0]);
        if(elements.length > 1) {
            socks5ClientConfig.setPassword(elements[1]);
        }
    }

    public static void analyseAndConfigSecurity(Socks5ClientConfig socks5ClientConfig, String security) {
        if(StringUtil.isEmpty(security)) {
            return;
        }
        socks5ClientConfig.setSecurity(Base64Util.decode(security.getBytes()));
    }
}
