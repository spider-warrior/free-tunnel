package cn.t.tool.netproxytool.socks5.client.handler;

import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.http.config.Socks5ClientConfig;
import cn.t.tool.netproxytool.socks5.client.analyse.AuthenticationResponseAnalyse;
import cn.t.tool.netproxytool.socks5.client.analyse.CmdResponseAnalyse;
import cn.t.tool.netproxytool.socks5.common.AbstractSocks5InboundHandler;
import cn.t.tool.netproxytool.socks5.constants.Socks5ClientConstants;
import cn.t.tool.netproxytool.socks5.constants.Socks5Method;
import cn.t.tool.netproxytool.socks5.constants.Socks5ProtocolConstants;
import cn.t.tool.netproxytool.socks5.model.CmdRequest;
import cn.t.tool.netproxytool.socks5.model.MethodRequest;
import cn.t.tool.netproxytool.socks5.model.MethodResponse;
import cn.t.tool.netproxytool.socks5.model.UsernamePasswordAuthenticationRequest;
import cn.t.tool.netproxytool.socks5.util.Socks5MessageUtil;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 协商响应处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:30
 **/
public class MethodResponseHandler extends AbstractSocks5InboundHandler<MethodResponse> {

    private static final Logger logger = LoggerFactory.getLogger(MethodResponseHandler.class);

    private final String targetHost;
    private final short targetPort;
    private final Socks5ClientConfig socks5ClientConfig;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MethodResponse response) {
        logger.info("[{} -> {}]协商结果: version: {}, method: {}", ctx.channel().localAddress(), ctx.channel().remoteAddress(), response.getVersion(), response.getSocks5Method());
        byte version = response.getVersion();
        if(version != Socks5ProtocolConstants.VERSION) {
            throw new ProxyException(String.format("不支持的协议版本: %d", version));
        }
        byte socks5MethodValue =  response.getSocks5Method();
        Socks5Method socks5Method = Socks5Method.getSocks5Method(socks5MethodValue);
        if(socks5Method == null || Socks5Method.NO_ACCEPTABLE_METHODS == socks5Method) {
            throw new ProxyException("客户端未提供支持的认证方法");
        } else {
            //不需要认证, 直接构建cmd请求
            if(Socks5Method.NO_AUTHENTICATION_REQUIRED == socks5Method) {
                CmdRequest cmdRequest = Socks5MessageUtil.buildConnectRequest(targetHost.getBytes(), targetPort);
                nettyB2mDecoder.setByteBufAnalyser(new CmdResponseAnalyse());
                ctx.writeAndFlush(cmdRequest);
                //用户名密码认证
            } else if(Socks5Method.USERNAME_PASSWORD == socks5Method) {
                if(socks5ClientConfig == null) {
                    throw new ProxyException("客户端未配置Socks5ClientConfig");
                }
                UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest = Socks5MessageUtil.buildUsernamePasswordAuthenticationRequest(socks5ClientConfig.getUsername().getBytes(), socks5ClientConfig.getPassword().getBytes());
                nettyB2mDecoder.setByteBufAnalyser(new AuthenticationResponseAnalyse());
                ctx.writeAndFlush(usernamePasswordAuthenticationRequest);
            } else {
                throw new ProxyException("客户端未实现的方法处理: " + socks5Method);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //init attr
        ctx.channel().attr(Socks5ClientConstants.TARGET_HOST_KEY).set(targetHost);
        ctx.channel().attr(Socks5ClientConstants.TARGET_PORT_KEY).set(targetPort);
        //send negotiate msg
        MethodRequest methodRequest = new MethodRequest();
        methodRequest.setVersion(Socks5ProtocolConstants.VERSION);
        methodRequest.setMethods(new byte[]{Socks5Method.USERNAME_PASSWORD.rangeStart});
        ctx.writeAndFlush(methodRequest);
    }

    @Override
    public void setNettyB2mDecoder(NettyB2mDecoder nettyB2mDecoder) {
        this.nettyB2mDecoder = nettyB2mDecoder;
    }

    public MethodResponseHandler(String targetHost, short targetPort, Socks5ClientConfig socks5ClientConfig) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
        this.socks5ClientConfig = socks5ClientConfig;
    }
}
