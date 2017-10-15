package com.tingfeng.syRun.bean;

import com.tingfeng.syRun.common.CodeConstants;

/**
 * 
 * @author huitoukest
 *
 */
public class SyLockResponse {
   /**
    * 回应的消息的id
    */
   private String id = "";
   /**
    * 结果
    */
   private String result  = "";
   /**
    * 当前加锁的id
    */
   private String lockId  = "";
   
   
	public String getId() {
		return id;
	}
	public String getResult() {
		return result;
	}
	public String getLockId() {
		return lockId;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public void setLockId(String lockId) {
		this.lockId = lockId;
	}
	/**
	 * 
	 * @param lockId 唯一锁id
	 * @param id 唯一消息id
	 * @return
	 */
	public static SyLockResponse getFaildSyLock(String lockId,String id) {
		SyLockResponse response = new SyLockResponse();
		response.setLockId(lockId);
		response.setResult(CodeConstants.Result.FAIL);
		response.setId(id);
		return response;
	}
}
