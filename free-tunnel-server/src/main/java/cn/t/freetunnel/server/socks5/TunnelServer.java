package cn.t.freetunnel.server.socks5;

import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.server.socks5.util.TunnelServerConfigUtil;
import cn.t.freetunnel.server.util.InitializerBuilder;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;

import java.util.ArrayList;
import java.util.List;

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
public class TunnelServer {

    public static void main(String[] args) {
        TunnelServerConfigUtil.initServerConfig();
        List<DaemonService> daemonServerList = new ArrayList<>();
        NettyTcpChannelInitializer nettyChannelInitializer = InitializerBuilder.buildSocks5ProxyServerChannelInitializer();
        NettyTcpServer proxyServer = new NettyTcpServer(
            String.format("socks5-proxy-server(%s:%s)",
                TunnelServerConfig.SERVER_HOST,
                TunnelServerConfig.SERVER_PORT),
            new int[]{TunnelServerConfig.SERVER_PORT},
            nettyChannelInitializer,
            TunnelConstants.BOSS_GROUP,
            TunnelConstants.WORKER_GROUP,
            false,
            true);
        daemonServerList.add(proxyServer);
        DefaultLauncher defaultLauncher = new DefaultLauncher();
        defaultLauncher.setDaemonServiceList(daemonServerList);
        defaultLauncher.startup();
    }
}
