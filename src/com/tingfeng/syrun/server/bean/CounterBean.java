package com.tingfeng.syrun.server.bean;

public class CounterBean {
	/**
	 * 10分钟
	 */
	public static final long DEFAULT_EXPIRE_TIME =10 *60 * 1000;
	
	/**
	 * 值
	 */
	public long value = 0;
	/**
	 * key
	 */
	public String key = "";
	/**
	 * 过期间的时间的毫秒数
	 */
	public long expireTime = System.currentTimeMillis() + DEFAULT_EXPIRE_TIME;
	/**
	 * 
	 * @param key
	 */
	public CounterBean (String key){
		this.key = key;
	}
	/**
	 * 
	 * @param value
	 * @param key
	 * @param expireTime
	 */
	public CounterBean(long value, String key, long expireTime) {
		super();
		this.value = value;
		this.key = key;
		this.expireTime = expireTime;
	}
	
	
}	
