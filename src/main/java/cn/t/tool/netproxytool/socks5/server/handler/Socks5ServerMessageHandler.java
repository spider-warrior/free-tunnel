package cn.t.tool.netproxytool.socks5.server.handler;

import cn.t.tool.netproxytool.event.ProxyConnectionBuildResultListener;
import cn.t.tool.netproxytool.exception.ProxyException;
import cn.t.tool.netproxytool.http.constants.ProxyBuildExecutionStatus;
import cn.t.tool.netproxytool.socks5.config.ServerConfig;
import cn.t.tool.netproxytool.socks5.config.UserConfig;
import cn.t.tool.netproxytool.socks5.constants.*;
import cn.t.tool.netproxytool.socks5.server.listener.Socks5ProxyServerConnectionReadyListener;
import cn.t.tool.netproxytool.util.NetProxyUtil;
import cn.t.util.common.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Socks5ServerMessageHandler
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2021-08-27 13:40
 **/
public class Socks5ServerMessageHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(Socks5ServerMessageHandler.class);

    private Socks5ServerState state = Socks5ServerState.NEGOTIATE;
    private byte[] security;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf)msg;
        if(Socks5ServerState.NEGOTIATE == state) {
            if(byteBuf.readableBytes() < 2) {
                return;
            }
            //解析方法协商要素
            byte version = byteBuf.readByte();
            byte methodLength = byteBuf.readByte();
            byte[] methods = new byte[methodLength];
            byteBuf.readBytes(methods);
            //处理消息内容
            if(version != Socks5ProtocolConstants.VERSION) {
                throw new ProxyException(String.format("不支持的协议版本: %d", version));
            }
            if(ArrayUtil.isEmpty(methods)) {
                throw new ProxyException("客户端未提供支持的认证方法");
            }
            byte methodSelectedValue = -1;
            Socks5Method methodSelected = null;
            //协商方法
            for(byte b: methods) {
                methodSelected = Socks5Method.getSocks5Method(b);
                if(methodSelected != null) {
                    methodSelectedValue = b;
                    break;
                }
            }
            //切换状态
            if(Socks5Method.NO_AUTHENTICATION_REQUIRED == methodSelected) {
                state = Socks5ServerState.CMD;
            } else if(Socks5Method.USERNAME_PASSWORD == methodSelected) {
                state = Socks5ServerState.AUTHENTICATE;
            } else {
                throw new ProxyException(String.format("未协商到合适的认证方法, 客户端支持的内容为: %s", Arrays.toString(methods)));
            }
            ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(2);
            outputBuf.writeByte(version);
            outputBuf.writeByte(methodSelectedValue);
            ctx.writeAndFlush(outputBuf);
        } else if(Socks5ServerState.AUTHENTICATE == state) {
            // 解析鉴权要素
            byte version = byteBuf.readByte();
            byte usernameLength = byteBuf.readByte();
            byte[] usernameBytes = new byte[usernameLength];
            byteBuf.readBytes(usernameBytes);
            byte passwordLength = byteBuf.readByte();
            byte[] passwordBytes = new byte[passwordLength];
            byteBuf.readBytes(passwordBytes);
            String username = new String(usernameBytes);
            String password = new String(passwordBytes);
            //鉴权
            UserConfig userConfig = ServerConfig.USER_CONFIG_MAP.get(username);
            if(userConfig == null || userConfig.getPassword() == null || !userConfig.getPassword().equals(password)) {
                logger.info("用户名密码验证失败, password: {}, 即将关闭连接", password);
                ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(2);
                outputBuf.writeByte(version);
                outputBuf.writeByte(AuthenticationStatus.FAILED.value);
                ctx.writeAndFlush(outputBuf);
                ctx.close();
            } else {
                logger.info("用户名密码验证通过, username: {}", username);
                this.security = userConfig.getSecurity();
                state = Socks5ServerState.CMD;
                ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(2);
                outputBuf.writeByte(version);
                outputBuf.writeByte(AuthenticationStatus.SUCCESS.value);
                ctx.writeAndFlush(outputBuf);
            }
        } else if(Socks5ServerState.CMD == state) {
            if(byteBuf.readableBytes() > 6) {
                //1.解析CMD要素
                //version
                byte version = byteBuf.readByte();
                //cmd
                byte cmdByte = byteBuf.readByte();
                Socks5Cmd socks5Cmd = Socks5Cmd.getCmd(cmdByte);
                //rsv
                byte rsv = byteBuf.readByte();
                if(rsv != 0) {
                    throw new ProxyException(String.format("rsv必须为0, 实际传输值为: %d", rsv));
                }
                //address type
                byte addressTypeByte = byteBuf.readByte();
                Socks5AddressType socks5AddressType = Socks5AddressType.getAddressType(addressTypeByte);
                if(socks5AddressType == null) {
                    throw new ProxyException(String.format("不支持的地址类型: %d", addressTypeByte));
                }
                //target address
                byte[] targetAddressBytes = getAddressBytes(byteBuf, socks5AddressType);
                String targetHost = new String(targetAddressBytes);
                //target port
                int targetPort = byteBuf.readChar();

                InetSocketAddress clientAddress = (InetSocketAddress)ctx.channel().remoteAddress();
                logger.info("[{}]: [{}], 命令类型: {}, 地址: {}:{}", clientAddress, cmdByte, addressTypeByte, targetHost, targetPort);
                //2.处理CMD消息
                if(Socks5Cmd.CONNECT == socks5Cmd) {
                    String clientName = NetProxyUtil.buildProxyConnectionName(clientAddress.getHostString(), clientAddress.getPort(), targetHost, targetPort);
                    ProxyConnectionBuildResultListener proxyConnectionBuildResultListener = (status, remoteChannelHandlerContext) -> {
                        if(ProxyBuildExecutionStatus.SUCCEEDED.value == status) {
                            logger.info("[client: {}] -> [local: {}] -> [remote: {}]: 代理创建成功", ctx.channel().remoteAddress(), remoteChannelHandlerContext.channel().localAddress(), remoteChannelHandlerContext.channel().remoteAddress());
                            ChannelPromise promise = ctx.newPromise();
                            promise.addListener(new Socks5ProxyServerConnectionReadyListener(ctx, remoteChannelHandlerContext, clientName, this.security));

                            ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(1 + 1 + 1 + 1 + Socks5ServerDaemonConfig.SERVER_HOST_BYTES.length + 2);
                            outputBuf.writeByte(version);
                            outputBuf.writeByte(Socks5CmdExecutionStatus.SUCCEEDED.value);
                            outputBuf.writeByte((byte)0);
                            outputBuf.writeByte(Socks5AddressType.IPV4.value);
                            outputBuf.writeBytes(Socks5ServerDaemonConfig.SERVER_HOST_BYTES);
                            outputBuf.writeShort(Socks5ServerDaemonConfig.SERVER_PORT);
                            ctx.writeAndFlush(outputBuf, promise);
                        } else {
                            logger.error("[{}]: 代理客户端失败, remote: {}:{}", clientAddress, targetHost, targetPort);
                            ByteBuf outputBuf = ByteBufAllocator.DEFAULT.buffer(1 + 1 + 1 + 1 + Socks5ServerDaemonConfig.SERVER_HOST_BYTES.length + 2);
                            outputBuf.writeByte(version);
                            outputBuf.writeByte(Socks5CmdExecutionStatus.GENERAL_SOCKS_SERVER_FAILURE.value);
                            outputBuf.writeByte((byte)0);
                            outputBuf.writeByte(Socks5AddressType.IPV4.value);
                            outputBuf.writeBytes(Socks5ServerDaemonConfig.SERVER_HOST_BYTES);
                            outputBuf.writeShort(Socks5ServerDaemonConfig.SERVER_PORT);
                            ctx.writeAndFlush(outputBuf);
                            ctx.close();
                        }
                    };
                    NetProxyUtil.startHttpFetchMessageClient(ctx, proxyConnectionBuildResultListener, clientName, targetHost, targetPort);
                } else {
                    throw new ProxyException("未实现的命令处理: " + socks5Cmd);
                }
            }
        } else {
            throw new ProxyException("未实现的状态处理: " + state);
        }
    }

    private byte[] getAddressBytes(ByteBuf byteBuf, Socks5AddressType socks5AddressType) {
        if(socks5AddressType == Socks5AddressType.IPV4) {
            byte[] bytes = new byte[4];
            byteBuf.readBytes(bytes);
            return bytes;
        } else if(socks5AddressType == Socks5AddressType.IPV6) {
            byte[] bytes = new byte[16];
            byteBuf.readBytes(bytes);
            return bytes;
        } else if(socks5AddressType == Socks5AddressType.DOMAIN) {
            byte[] bytes = new byte[byteBuf.readByte()];
            byteBuf.readBytes(bytes);
            return bytes;
        } else {
            throw new ProxyException("解析地址失败");
        }
    }

}
