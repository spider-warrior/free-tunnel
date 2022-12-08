package cn.t.freetunnel.common.constants;

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
    NO_AUTHENTICATION_REQUIRED((byte)0X00),
    /**
     * GSS_API
     */
    GSS_API((byte)0X01),
    /**
     * 用户名、密码认证
     */
    USERNAME_PASSWORD((byte)0X02),
    /*
     * 0X03 - 0X07由Internet Assigned Numbers Authority(互联网数字分配机构)分配（保留）
     */
    /*
     * 0X80-0xFE为私人方法保留
     */
    /**
     * 无可接受的方法
     */
    NO_ACCEPTABLE_METHODS((byte)0XFF);

    public final byte value;

    Socks5Method(byte value) {
        this.value = value;
    }

    public static Socks5Method getSocks5Method(byte value) {
        for (Socks5Method socks5Method : Socks5Method.values()) {
            if(socks5Method.value == value) {
                return socks5Method;
            }
        }
        return null;
    }
}
