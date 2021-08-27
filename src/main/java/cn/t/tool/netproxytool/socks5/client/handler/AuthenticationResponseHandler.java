package cn.t.tool.netproxytool.socks5.client.handler;

import cn.t.tool.netproxytool.socks5.client.analyse.CmdResponseAnalyse;
import cn.t.tool.netproxytool.socks5.common.AbstractSocks5InboundHandler;
import cn.t.tool.netproxytool.socks5.constants.Socks5ClientConstants;
import cn.t.tool.netproxytool.socks5.constants.Socks5CmdExecutionStatus;
import cn.t.tool.netproxytool.socks5.model.AuthenticationResponse;
import cn.t.tool.netproxytool.socks5.model.CmdRequest;
import cn.t.tool.netproxytool.socks5.util.Socks5MessageUtil;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 鉴权响应处理器处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:30
 **/
public class AuthenticationResponseHandler extends AbstractSocks5InboundHandler<AuthenticationResponse> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationResponseHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthenticationResponse response) {
        logger.info("[{} -> {}]鉴权结果: version: {}, status: {}({})", ctx.channel().localAddress(), ctx.channel().remoteAddress(), response.getVersion(), response.getStatus(), Socks5CmdExecutionStatus.getSocks5CmdExecutionStatus(response.getStatus()));
        if(Socks5CmdExecutionStatus.SUCCEEDED.value == response.getStatus()) {
            String targetHost = ctx.channel().attr(Socks5ClientConstants.TARGET_HOST_KEY).get();
            Short targetPort = ctx.channel().attr(Socks5ClientConstants.TARGET_PORT_KEY).get();
            ctx.channel().attr(Socks5ClientConstants.USE_SAFE_ENCRYPT).set(Boolean.TRUE);
            CmdRequest cmdRequest = Socks5MessageUtil.buildConnectRequest(targetHost.getBytes(), targetPort);
            nettyB2mDecoder.setByteBufAnalyser(new CmdResponseAnalyse());
            ctx.writeAndFlush(cmdRequest);
        }
    }

    @Override
    public void setNettyB2mDecoder(NettyB2mDecoder nettyB2mDecoder) {
        this.nettyB2mDecoder = nettyB2mDecoder;
    }

}
