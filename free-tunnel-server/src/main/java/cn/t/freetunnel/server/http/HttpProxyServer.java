package cn.t.freetunnel.server.http;

import cn.t.freetunnel.common.constants.TunnelConstants;
import cn.t.freetunnel.server.constants.HttpProxyServerConfig;
import cn.t.freetunnel.server.util.InitializerBuilder;
import cn.t.tool.nettytool.daemon.DaemonService;
import cn.t.tool.nettytool.daemon.server.NettyTcpServer;
import cn.t.tool.nettytool.initializer.NettyTcpChannelInitializer;
import cn.t.tool.nettytool.launcher.DefaultLauncher;

import java.util.ArrayList;
import java.util.List;

/**
 * http代理
 *
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-24 11:05
 **/
public class HttpProxyServer {
    public static void main(String[] args) {
        List<DaemonService> daemonServerList = new ArrayList<>();
        NettyTcpChannelInitializer nettyChannelInitializer = InitializerBuilder.buildHttpProxyServerChannelInitializer();
        NettyTcpServer proxyServer = new NettyTcpServer(
            "http-proxy-server",
            new int[]{HttpProxyServerConfig.SERVER_PORT},
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
