package cn.t.tool.netproxytool.socks5.server.handler;

import cn.t.tool.netproxytool.socks5.common.AbstractSocks5InboundHandler;
import cn.t.tool.netproxytool.socks5.config.ServerConfig;
import cn.t.tool.netproxytool.socks5.config.UserConfig;
import cn.t.tool.netproxytool.socks5.constants.AuthenticationStatus;
import cn.t.tool.netproxytool.socks5.constants.Socks5ProtocolConstants;
import cn.t.tool.netproxytool.socks5.constants.Socks5ServerDaemonConfig;
import cn.t.tool.netproxytool.socks5.model.AuthenticationResponse;
import cn.t.tool.netproxytool.socks5.model.UsernamePasswordAuthenticationRequest;
import cn.t.tool.netproxytool.socks5.server.analyse.CmdRequestAnalyse;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 认证请求处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:30
 **/
public class UsernamePasswordAuthenticationRequestHandler extends AbstractSocks5InboundHandler<UsernamePasswordAuthenticationRequest> {

    private static final Logger logger = LoggerFactory.getLogger(UsernamePasswordAuthenticationRequestHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UsernamePasswordAuthenticationRequest usernamePasswordAuthenticationRequest) {
        String username = new String(usernamePasswordAuthenticationRequest.getUsername());
        String password = new String(usernamePasswordAuthenticationRequest.getPassword());
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setVersion(Socks5ProtocolConstants.VERSION);
        boolean authenticationSuccess = false;
        UserConfig userConfig = ServerConfig.USER_CONFIG_MAP.get(username);
        if(userConfig != null && userConfig.getPassword() != null && userConfig.getPassword().equals(password)) {
            authenticationSuccess = true;
        }
        if(authenticationSuccess) {
            logger.info("用户名密码验证通过, username: {}", username);
            ctx.channel().attr(Socks5ServerDaemonConfig.CHANNEL_USERNAME).set(username);
            nettyB2mDecoder.setByteBufAnalyser(new CmdRequestAnalyse());
            authenticationResponse.setStatus(AuthenticationStatus.SUCCESS.value);
            ctx.writeAndFlush(authenticationResponse);
        } else {
            logger.info("用户名密码验证失败, password: {}", password);
            authenticationResponse.setStatus(AuthenticationStatus.FAILED.value);
            ctx.writeAndFlush(authenticationResponse);
        }
    }
    @Override
    public void setNettyB2mDecoder(NettyB2mDecoder nettyB2mDecoder) {
        this.nettyB2mDecoder = nettyB2mDecoder;
    }
}
