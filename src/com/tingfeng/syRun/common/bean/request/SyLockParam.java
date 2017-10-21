package com.tingfeng.syRun.common.bean.request;

/**
 * 
 * @author huitoukest
 * 
 */
public class SyLockParam extends BaseRequestParam {
    
	/**
	 * 同步锁的key
	 */
	private String key ;
	/**
	 * 最长等待时间,推荐小于和服务器的连接超时时间,单位毫秒
	 */
	/*private Long maxWaitTime;//暂不实现 */
	
	/**
    * 当前加锁的id,只有解锁的时候需要传入
    */
	private String lockId ;
	   
	public String getKey() {
		return key;
	}
	/*public Long getMaxWaitTime() {
		return maxWaitTime;
	}*/
	public void setKey(String key) {
		this.key = key;
	}
	/*public void setMaxWaitTime(Long maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}*/
	public String getLockId() {
		return lockId;
	}
	public void setLockId(String lockId) {
		this.lockId = lockId;
	}
	
}
