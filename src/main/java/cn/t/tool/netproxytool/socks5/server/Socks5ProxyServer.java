package cn.t.tool.netproxytool.socks5.server;

import cn.t.tool.netproxytool.socks5.config.ServerConfig;
import cn.t.tool.netproxytool.socks5.constants.Socks5ServerDaemonConfig;
import cn.t.tool.netproxytool.socks5.util.ServerConfigUtil;
import cn.t.tool.netproxytool.util.InitializerBuilder;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.NettyChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * 代理服务器
 * <a>https://www.cnblogs.com/cc11001100/p/9949729.html</a>
 * <a>https://www.ietf.org/rfc/rfc1928.txt</a>
 *
 * 使用socks5代理的坑，域名在本地解析还是在代理服务器端解析，有些比如google.com就必须在代理服务器端解析
 * <a>https://blog.emacsos.com/use-socks5-proxy-in-curl.html</a>
 *
 * @author yj
 * @since 2020-01-12 13:41
 **/
public class Socks5ProxyServer {
    public static void main(String[] args) {
        ServerConfig serverConfig = ServerConfigUtil.loadServerConfig();
        List<DaemonService> daemonServerList = new ArrayList<>();
        NettyChannelInitializer nettyChannelInitializer = InitializerBuilder.buildSocks5ProxyServerChannelInitializer();
        NettyTcpServer proxyServer = new NettyTcpServer(String.format("socks5-proxy-server(%s:%s)", Socks5ServerDaemonConfig.SERVER_HOST, Socks5ServerDaemonConfig.SERVER_PORT), Socks5ServerDaemonConfig.SERVER_PORT, nettyChannelInitializer);
        proxyServer.childAttr(Socks5ServerDaemonConfig.SERVER_CONFIG_KEY, serverConfig);
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }
}
