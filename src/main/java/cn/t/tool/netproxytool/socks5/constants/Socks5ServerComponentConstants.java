package cn.t.tool.netproxytool.socks5.constants;

import cn.t.tool.netproxytool.socks5.server.analyse.MethodRequestAnalyse;
import cn.t.tool.netproxytool.socks5.server.encoder.CmdResponseEncoder;
import cn.t.tool.netproxytool.socks5.server.encoder.MethodResponseEncoder;
import cn.t.tool.netproxytool.socks5.server.encoder.UsernamePasswordAuthenticationResponseEncoder;

import java.util.function.Supplier;

/**
 * @author <a href="mailto:yangjian@ifenxi.com">研发部-杨建</a>
 * @version V1.0
 * @since 2021-07-16 17:19
 **/
public class Socks5ServerComponentConstants {
    private static final MethodRequestAnalyse METHOD_REQUEST_ANALYSE = new MethodRequestAnalyse();
    private static final MethodResponseEncoder METHOD_RESPONSE_ENCODER = new MethodResponseEncoder();
    private static final UsernamePasswordAuthenticationResponseEncoder USERNAME_PASSWORD_AUTHENTICATION_RESPONSE_ENCODER = new UsernamePasswordAuthenticationResponseEncoder();
    private static final CmdResponseEncoder CMD_RESPONSE_ENCODER = new CmdResponseEncoder();
    public static final Supplier<MethodRequestAnalyse> METHOD_REQUEST_ANALYSE_SUPPLIER = () -> METHOD_REQUEST_ANALYSE;
    public static final Supplier<MethodResponseEncoder> METHOD_RESPONSE_ENCODER_SUPPLIER = () -> METHOD_RESPONSE_ENCODER;
    public static final Supplier<UsernamePasswordAuthenticationResponseEncoder> USERNAME_PASSWORD_AUTHENTICATION_RESPONSE_ENCODER_SUPPLIER = () -> USERNAME_PASSWORD_AUTHENTICATION_RESPONSE_ENCODER;
    public static final Supplier<CmdResponseEncoder> CMD_RESPONSE_ENCODER_SUPPLIER = () -> CMD_RESPONSE_ENCODER;
}
