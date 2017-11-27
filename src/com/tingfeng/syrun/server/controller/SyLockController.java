package com.tingfeng.syrun.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syrun.common.ResponseStatus;
import com.tingfeng.syrun.common.bean.request.SyLockParam;
import com.tingfeng.syrun.common.bean.response.ResponseBean;
import com.tingfeng.syrun.common.util.IdWorker;
import com.tingfeng.syrun.server.handler.SyRunSeverHandler;
import com.tingfeng.syrun.server.service.impl.SyLockService;
import com.tingfeng.syrun.server.util.ServerHandleUtil;
import io.netty.channel.Channel;

/**
 * 对外处理他同步锁
 * @author huitoukest
 *
 */
public class SyLockController {

	SyLockService syLockService = SyLockService.getInstance();
	
	public SyLockController() {
	
	}
	
	/**
	 * 获取同步锁,成功返回锁的唯一id和结果信息
	 * @param syLockParam
	 * @return 
	 */
	public String lockSyLock(String id,SyLockParam syLockParam) {
		Channel channel = ServerHandleUtil.channels.get();
		String lockId =  syLockService.lockSyLock(channel.id().toString(),id,syLockParam);
		return lockId;
	}
	/**
	 * 释放同步锁
	 * @param syLockParam
	 * @return
	 */
	public void unlockSyLock(String id,SyLockParam syLockParam) {
		Channel channel = ServerHandleUtil.channels.get();
		syLockService.unlockSyLock(channel.id().toString(),id,syLockParam);
	}

	/**
	 * 检查key的合法性,合法返回true,否则返回false 
	 * @param key
	 * @return
	 */
	public boolean checkSyLockKey(String key) {
		if(null == key || key.trim().length() < 1 ) {
			return false;
		}
		return true;
	}
	/**
	 * 
	 * @param syLockParam
	 * @return
	 */
	public boolean checkSyLockKey(SyLockParam syLockParam) {
		if(null == syLockParam ) {
			return false;
		}
		return checkSyLockKey(syLockParam.getLockId());
	}
	
	public String toJSONString(Object obj) {
		return JSONObject.toJSONString(obj);
	}
}
