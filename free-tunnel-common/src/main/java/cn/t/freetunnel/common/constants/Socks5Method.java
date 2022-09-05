package cn.t.freetunnel.common.constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 方法
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 20:18
 **/
public enum Socks5Method {
    /**
     * 不需要认证
     * */
    NO_AUTHENTICATION_REQUIRED((byte) 0X00, (byte) 0X00),
    /**
     * GSS_API
     */
    GSS_API((byte) 0X01, (byte) 0X01),
    /**
     * 用户名、密码认证
     */
    USERNAME_PASSWORD((byte) 0X02, (byte) 0X02),
    /**
     * 由Internet Assigned Numbers Authority(互联网数字分配机构)分配（保留）
     */
    INTERNET_ASSIGNED_NUMBERS_AUTHORITY_ASSIGNED((byte) 0X03, (byte) 0X07),
    /**
     * 0xFE为私人方法保留
     */
    RESERVED_FOR_PRIVATE_METHODS((byte) 0X80, (byte) 0XFE),
    /**
     * 无可接受的方法
     */
    NO_ACCEPTABLE_METHODS((byte) 0XFF, (byte) 0XFF);

    public final byte rangeStart;
    public final byte rangeEnd;

    Socks5Method(byte rangeStart, byte rangeEnd) {
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }

    public boolean isMe(byte value) {
        return value >= rangeStart && value <= rangeEnd;
    }

    public static Socks5Method getSocks5Method(byte value) {
        for(Socks5Method socks5Method : values()) {
            if(socks5Method.isMe(value)) {
                return socks5Method;
            }
        }
        return null;
    }

    public static List<Socks5Method> convertToMethod(byte[] methodValues) {
        List<Socks5Method> socks5MethodList = new ArrayList<>(methodValues.length);
        for (byte b : methodValues) {
            Socks5Method socks5Method = getSocks5Method(b);
            if(socks5Method != null) {
                socks5MethodList.add(socks5Method);
            }
        }
        return socks5MethodList;
    }
}
