package com.tingfeng.syRun.common;

public class ConfigEntity {
	public static final int SERVDER_TCP_PORT = 9999;
	public static final String SERVER_IP = "127.0.0.1";

	public static final int TIME_OUT_CONNECT = 20000;//连接超时时间,默认30秒.
	public static final int TIME_RECONNECT = 2000;//重连2秒内.

	public static final int TIME_IO_IDLE = 30000;//IO空闲时间30秒一个.
	public static final int TIME_HEART_BEAT = 30000;//心跳包30秒一个.

    public static final int TIME_OUT_RUN = 300000;//300秒的运行超时时间/锁占用阻塞.

	public static final String KEY_SESSION = "key_session";

	public static final int TIME_RESEND_IDLE = 5000;//消息发送失败后的重发间隔时间.

}
