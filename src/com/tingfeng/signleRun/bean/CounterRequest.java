package com.tingfeng.signleRun.bean;

public class CounterRequest extends BaseRequest{
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
	public CounterRequest (String key){
		this.key = key;
	}
	/**
	 * 
	 * @param value
	 * @param key
	 * @param expireTime
	 */
	public CounterRequest(long value, String key, long expireTime) {
		super();
		this.value = value;
		this.key = key;
		this.expireTime = expireTime;
	}
	
	/**
	 * 
	 * @param value
	 * @param key
	 * @param expireTime
	 */
	public CounterRequest(String method,long value, String key, long expireTime) {
		super();
		setMethod(method);
		this.value = value;
		this.key = key;
		this.expireTime = expireTime;
	}
}
