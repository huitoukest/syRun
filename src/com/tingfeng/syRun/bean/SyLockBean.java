package com.tingfeng.syRun.bean;
/**
 * 
 * @author huitoukest
 *
 */
public class SyLockBean {
   private String key ;
   private Long expireTime;
   
	public String getKey() {
		return key;
	}
	public Long getExpireTime() {
		return expireTime;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public void setExpireTime(Long expireTime) {
		this.expireTime = expireTime;
	}
	   
}
