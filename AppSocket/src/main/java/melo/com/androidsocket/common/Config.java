package melo.com.androidsocket.common;

/**
 * Created by melo on 2017/11/27.
 */

public class Config {

    public static final String MSG = "msg";
    public static final String HEARTBREAK = "heartbreak";
    public static final String PING = "ping";

    public static final String TCP_IP = "ip";
    public static final String TCP_PORT = "port";

    // 单个CPU线程池大小
    public static final int POOL_SIZE = 5;

    /**
     * 错误处理
     */
    public static class ErrorCode {

        public static final int CREATE_TCP_ERROR = 1;

        public static final int PING_TCP_TIMEOUT = 2;
    }

}
