package cn.t.tool.netproxytool.socks5.server.handler;

import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.socks5.common.AbstractSocks5InboundHandler;
import cn.t.tool.netproxytool.socks5.constants.Socks5Method;
import cn.t.tool.netproxytool.socks5.constants.Socks5ProtocolConstants;
import cn.t.tool.netproxytool.socks5.model.MethodRequest;
import cn.t.tool.netproxytool.socks5.model.MethodResponse;
import cn.t.tool.netproxytool.socks5.server.analyse.CmdRequestAnalyse;
import cn.t.tool.netproxytool.socks5.server.analyse.authenticationanalyse.UsernamePasswordAuthenticationAnalyse;
import cn.t.tool.nettytool.decoder.NettyB2mDecoder;
import cn.t.util.common.ArrayUtil;
import io.netty.channel.ChannelHandlerContext;

import java.util.Arrays;

/**
 * 协商请求处理器
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 22:30
 **/
public class MethodRequestHandler extends AbstractSocks5InboundHandler<MethodRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MethodRequest methodRequest) {
        byte version = methodRequest.getVersion();
        if(version != Socks5ProtocolConstants.VERSION) {
            throw new ProxyException(String.format("不支持的协议版本: %d", version));
        }
        byte[] methods= methodRequest.getMethods();
        if(ArrayUtil.isEmpty(methods)) {
            throw new ProxyException("客户端未提供支持的认证方法");
        } else {
            Socks5Method selectedSocks5Method = negotiateMethod(methods);
            if(Socks5Method.NO_AUTHENTICATION_REQUIRED == selectedSocks5Method) {
                nettyB2mDecoder.setByteBufAnalyser(new CmdRequestAnalyse());
            } else if(Socks5Method.USERNAME_PASSWORD == selectedSocks5Method) {
                nettyB2mDecoder.setByteBufAnalyser(new UsernamePasswordAuthenticationAnalyse());
            } else {
                throw new ProxyException(String.format("未协商到合适的认证方法, 客户端支持的内容为: %s", Arrays.toString(methods)));
            }
            MethodResponse methodResponse = new MethodResponse();
            methodResponse.setVersion(version);
            methodResponse.setSocks5Method(selectedSocks5Method.rangeStart);
            ctx.writeAndFlush(methodResponse);
        }
    }

    private Socks5Method negotiateMethod(byte[] methods) {
        Socks5Method socks5Method = null;
        for(byte b: methods) {
            if(Socks5Method.USERNAME_PASSWORD.rangeStart == b) {
                socks5Method = Socks5Method.USERNAME_PASSWORD;
                break;
            } else if(Socks5Method.NO_AUTHENTICATION_REQUIRED.rangeStart == b) {
                socks5Method = Socks5Method.NO_AUTHENTICATION_REQUIRED;
            }
        }
        return socks5Method;
    }

    @Override
    public void setNettyB2mDecoder(NettyB2mDecoder nettyB2mDecoder) {
        this.nettyB2mDecoder = nettyB2mDecoder;
    }
}
