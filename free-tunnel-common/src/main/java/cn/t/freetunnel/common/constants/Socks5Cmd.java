package cn.t.freetunnel.common.constants;

/**
 * 命令
 * @author <a href="mailto:jian.yang@liby.ltd">野生程序员-杨建</a>
 * @version V1.0
 * @since 2020-02-20 23:14
 **/
public enum Socks5Cmd {

    CONNECT((byte) 0X01),
    BIND((byte) 0X02),
    UDP_ASSOCIATE((byte) 0X03);

    public final byte value;

    Socks5Cmd(byte value) {
        this.value = value;
    }

    public static Socks5Cmd getCmd(byte value) {
        for(Socks5Cmd socks5Cmd : values()) {
            if(socks5Cmd.value == value) {
                return socks5Cmd;
            }
        }
        return null;
    }
}
