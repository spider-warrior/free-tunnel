package cn.t.freetunnel.server.socks5.handler;

import cn.t.freetunnel.common.constants.*;
import cn.t.freetunnel.common.exception.TunnelException;
import cn.t.freetunnel.common.listener.TunnelBuildResultListener;
import cn.t.freetunnel.common.util.ByteBufUtil;
import cn.t.freetunnel.common.util.TunnelUtil;
import cn.t.freetunnel.server.constants.Socks5TunnelServerConfig;
import cn.t.freetunnel.server.socks5.listener.Socks5TunnelServerFirstTimeReadyListener;
import cn.t.freetunnel.server.socks5.listener.Socks5TunnelServerFirstTimeReadyListenerForFreeTunnelClient;
import cn.t.freetunnel.server.socks5.listener.Socks5TunnelServerReuseReadyListener;
import cn.t.freetunnel.server.socks5.listener.Socks5TunnelServerReuseReadyListenerForFreeTunnelClient;
import cn.t.freetunnel.server.tunnelprovider.UnPooledTunnelProvider;
import cn.t.freetunnel.server.util.Socks5MessageUtil;
import cn.t.util.common.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Socks5ServerMessageHandler
 * @author <a href="mailto:yangjian@liby.ltd">研发部-杨建</a>
 * @version V1.0
 * @since 2021-08-27 13:40
 **/
@ChannelHandler.Sharable
public class Socks5TunnelServerMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(Socks5TunnelServerMessageHandler.class);

    private Socks5ServerState state = Socks5ServerState.NEGOTIATE;
    private byte[] security;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf byteBuf) {
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
            if(version != Socks5Constants.VERSION) {
                throw new TunnelException(String.format("不支持的协议版本: %d", version));
            }
            if(ArrayUtil.isEmpty(methods)) {
                throw new TunnelException("客户端未提供支持的认证方法");
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
                throw new TunnelException(String.format("未协商到合适的认证方法, 客户端支持的内容为: %s", Arrays.toString(methods)));
            }
            ByteBuf outputBuf = ctx.alloc().buffer(2);
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
            Socks5TunnelServerConfig.UserConfig userConfig = Socks5TunnelServerConfig.ServerConfig.USER_CONFIG_MAP.get(username);
            if(userConfig == null || userConfig.getPassword() == null || !userConfig.getPassword().equals(password)) {
                logger.error("用户名密码验证失败, password: {}, 即将关闭连接", password);
                ByteBuf outputBuf = ctx.alloc().buffer(2);
                outputBuf.writeByte(version);
                outputBuf.writeByte(AuthenticationStatus.FAILED.value);
                ctx.writeAndFlush(outputBuf).addListener(ChannelFutureListener.CLOSE);
            } else {
                logger.info("用户名密码验证通过, username: {}", username);
                if(userConfig.getSecurity() != null) {
                    security = userConfig.getSecurity();
                }
                state = Socks5ServerState.CMD;
                ByteBuf outputBuf = ctx.alloc().buffer(2);
                outputBuf.writeByte(version);
                outputBuf.writeByte(AuthenticationStatus.SUCCESS.value);
                ctx.writeAndFlush(outputBuf);
            }
        } else if(Socks5ServerState.CMD == state) {
            if(byteBuf.readableBytes() > 6) {
                int readerIndexBackup = byteBuf.readerIndex();
                //1.解析CMD要素
                //version
                byte version = byteBuf.readByte();
                //cmd
                byte cmdByte = byteBuf.readByte();
                Socks5Cmd socks5Cmd = Socks5Cmd.getCmd(cmdByte);
                //rsv
                byte rsv = byteBuf.readByte();
                boolean freeTunnelClient = (rsv == 10);
                if(rsv != 0 && !freeTunnelClient) {
                    throw new TunnelException(String.format("rsv必须为0(10代表free-tunnel客户端), 实际传输值为: %d", rsv));
                }
                //address type
                byte addressTypeByte = byteBuf.readByte();
                Socks5AddressType socks5AddressType = Socks5AddressType.getAddressType(addressTypeByte);
                if(socks5AddressType == null) {
                    throw new TunnelException(String.format("不支持的地址类型: %d", addressTypeByte));
                }
                //target address
                byte[] targetAddressBytes = getAddressBytes(byteBuf, socks5AddressType);
                if(targetAddressBytes == null) {
                    byteBuf.readerIndex(readerIndexBackup);
                    return;
                }
                String targetHost;
                if(Socks5AddressType.IPV4 == socks5AddressType) {
                    StringBuilder builder = new StringBuilder();
                    for(byte b: targetAddressBytes) {
                        builder.append(b&0xFF).append(".");
                    }
                    targetHost = builder.deleteCharAt(builder.length() - 1).toString();
                } else if(Socks5AddressType.IPV6 == socks5AddressType) {
                    throw new TunnelException("不支持的地址类型: ipv6");
                } else {
                    if(freeTunnelClient) {
                        targetHost = TunnelUtil.decryptHost(new String(targetAddressBytes));
                    } else {
                        targetHost = new String(targetAddressBytes);
                    }
                }
                //target port
                int targetPort = byteBuf.readChar();

                logger.info("[{}]命令类型: {}, 地址: {}:{}", ctx.channel(), socks5Cmd, targetHost, targetPort);
                //2.处理CMD消息
                if(Socks5Cmd.CONNECT == socks5Cmd) {
                    TunnelBuildResultListener tunnelBuildResultListener = (status, channel) -> {
                        if(TunnelBuildResult.SUCCEEDED.value == status) {
                            ChannelPromise promise = ctx.newPromise();
                            boolean firstTime = ctx.pipeline().get(NettyHandlerName.SOCKS5_TUNNEL_SERVER_FORWARDING_MESSAGE_HANDLER) == null;
                            //首次建立连接
                            if(firstTime) {
                                if(freeTunnelClient) {
                                    promise.addListener(new Socks5TunnelServerFirstTimeReadyListenerForFreeTunnelClient(
                                        ctx.channel(),
                                        channel,
                                        targetHost,
                                        targetPort,
                                        security
                                    ));

                                } else {
                                    promise.addListener(new Socks5TunnelServerFirstTimeReadyListener(
                                        ctx.channel(),
                                        channel,
                                        targetHost,
                                        targetPort
                                    ));
                                }
                            } else {
                                //复用连接
                                if(freeTunnelClient) {
                                    promise.addListener(new Socks5TunnelServerReuseReadyListenerForFreeTunnelClient(
                                        ctx.channel(),
                                        channel,
                                        targetHost,
                                        targetPort
                                    ));
                                } else {
                                    promise.addListener(new Socks5TunnelServerReuseReadyListener(
                                        ctx.channel(),
                                        channel,
                                        targetHost,
                                        targetPort
                                    ));
                                }
                            }
                            ByteBuf responseBuf = Socks5MessageUtil.buildConnectResponse(ctx.alloc(), version, Socks5CmdExecutionStatus.SUCCEEDED.value, Socks5AddressType.IPV4.value, Socks5TunnelServerConfig.SERVER_HOST_BYTES, Socks5TunnelServerConfig.SERVER_PORT);
                            ctx.writeAndFlush(responseBuf, promise);
                        } else {
                            logger.error("[{}]: 代理客户端失败, remote: {}:{}", ctx.channel().remoteAddress(), targetHost, targetPort);
                            ByteBuf responseBuf = Socks5MessageUtil.buildConnectResponse(ctx.alloc(), version, Socks5CmdExecutionStatus.GENERAL_SOCKS_SERVER_FAILURE.value, Socks5AddressType.IPV4.value, Socks5TunnelServerConfig.SERVER_HOST_BYTES, Socks5TunnelServerConfig.SERVER_PORT);
                            ctx.writeAndFlush(responseBuf);
                        }
                    };
                    if(freeTunnelClient) {
                        UnPooledTunnelProvider.acquireTcpTunnelForSocks5FreeTunnelClient(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
                    } else {
                        UnPooledTunnelProvider.acquireTcpTunnelForSocks5(ctx.channel(), targetHost, targetPort, tunnelBuildResultListener);
                    }
                } else {
                    throw new TunnelException("未实现的命令处理: " + socks5Cmd);
                }
            }
        } else {
            throw new TunnelException("未实现的状态处理: " + state);
        }
    }

    private byte[] getAddressBytes(ByteBuf byteBuf, Socks5AddressType socks5AddressType) {
        if(socks5AddressType == Socks5AddressType.IPV4) {
            if(!ByteBufUtil.enough(byteBuf, Socks5AddressByteLength.ipv4)) {
                return null;
            }
            byte[] bytes = new byte[Socks5AddressByteLength.ipv4];
            byteBuf.readBytes(bytes);
            return bytes;
        } else if(socks5AddressType == Socks5AddressType.IPV6) {
            throw new TunnelException("解析地址失败: 不支持IPV6");
//            if(!ByteBufUtil.enough(byteBuf, Socks5AddressByteLength.ipv6)) {
//                return null;
//            }
//            byte[] bytes = new byte[Socks5AddressByteLength.ipv6];
//            byteBuf.readBytes(bytes);
//            return bytes;
        } else if(socks5AddressType == Socks5AddressType.DOMAIN) {
            if(!ByteBufUtil.enough(byteBuf, Socks5AddressByteLength.domain)) {
                return null;
            }
            byte length = byteBuf.readByte();
            if(!ByteBufUtil.enough(byteBuf, length)) {
                return null;
            }
            byte[] bytes = new byte[length];
            byteBuf.readBytes(bytes);
            return bytes;
        } else {
            throw new TunnelException("解析地址失败");
        }
    }

}
