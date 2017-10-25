package com.tingfeng.syRun.common;

public class ConfigEntity {
	public static final int SERVDER_TCP_PORT = 9999;
	public static final String SERVER_IP = "127.0.0.1";

	public static final int OUTTIME_CONNECT = 30000;//连接超时时间,默认30秒
	public static final int RECONNECT_TIME = 1000;//1秒内重连

	public static final int IDLE_TIME = 30000;//心跳包30秒一个
	public static final int HEART_BEAT_TIME = 30000;//心跳包30秒一个

	public static final String KEY_SESSION = "key_session";
}
