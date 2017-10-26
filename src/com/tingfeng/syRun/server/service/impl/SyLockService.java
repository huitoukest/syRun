package com.tingfeng.syRun.server.service.impl;

import java.util.Map;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.util.IdWorker;
import com.tingfeng.syRun.server.bean.SyLockStatusBean;

/**
 * 
 * @author huitoukest
 *
 */
public class SyLockService {
	/***************************计划****************************************/
	/**
	 * 3.锁的同步实现中增加自定义的超时设置,替代取消功能
	 * 4.异步的设置中增加取消获取锁的设置
	 */
	/****************************计划***************************************/
	private static final SyLockService syLockService = new SyLockService();
	/**
	 * 锁状态的Map,,key是当前key,value当前线程计数器
	 */
	private final Map<String,SyLockStatusBean> lockCountDownLatchMap = new ConcurrentHashMap<>(5000);

	/**
	 * 是否释放锁的的检查时间间隔,单位毫秒
	 */
	public  final int lockCheckInterval = 5;
	
	private SyLockService(){
				
	}
	
	public static SyLockService getInstance() {
		return syLockService;
	}

	public ResponseBean lockSyLock(String id,final SyLockParam syLockParam) {
		final ResponseBean response = new ResponseBean();
		response.setData(IdWorker.getUUID() + "");
		response.setStatus(ResponseStatus.FAIL.getValue());
		response.setId(id);
		SyLockStatusBean lockStatus = null;
		String key = syLockParam.getKey();
		boolean isOverTime = false;
		try {
			synchronized (key) {
				lockStatus = lockCountDownLatchMap.get(key);
				if (null != lockStatus) {//如果需要等待
					isOverTime = !lockStatus.countDownLatch.await(ConfigEntity.TIME_OUT_RUN, TimeUnit.MILLISECONDS);
				}else{
					lockStatus = new SyLockStatusBean();
				}
				CountDownLatch countDownLatch = new CountDownLatch(1);
				lockStatus.countDownLatch = countDownLatch;
				lockStatus.lockId = response.getData();
				lockCountDownLatchMap.put(key, lockStatus);
			}
			response.setStatus(ResponseStatus.SUCCESS.getValue());
        }catch (Exception e){
			e.printStackTrace();
        	if(e instanceof  InterruptedException || isOverTime){
				response.setStatus(ResponseStatus.OVERRUNTIME.getValue());
				response.setErrorMsg("lock time over max milliseconds:" + ConfigEntity.TIME_OUT_RUN);
			}else{
				response.setStatus(ResponseStatus.FAIL.getValue());
			}
			SyLockStatusBean statusBean = lockCountDownLatchMap.get(key);
			if(null !=statusBean && response.getData().equals(statusBean.lockId)){
				if(null != statusBean.countDownLatch ){
					statusBean.countDownLatch.countDown();
				}
				lockCountDownLatchMap.remove(key);
			}
		}

		return response;		
	}
	/**
	 * 
	 * @param id
	 * @param syLockParam
	 * @return
	 */
	public ResponseBean unlockSyLock(String id,final SyLockParam syLockParam) {
		ResponseBean response = new ResponseBean();
		response.setId(id);
		response.setStatus(ResponseStatus.SUCCESS.getValue());
		response.setData(syLockParam.getLockId());
		String key = syLockParam.getKey();
		SyLockStatusBean lockStatus = null;
		try {
			lockStatus = lockCountDownLatchMap.get(key);
			String localLockId = lockStatus.lockId;
			if(null == key ||localLockId == null || !localLockId.equals(syLockParam.getLockId())){
				if(null == key){
					response.setErrorMsg("null key !");
				}else if(null == localLockId){
					response.setErrorMsg("please lock first!");
				}else{
					response.setErrorMsg("other is lock , wait until you lock success!");
				}
				response.setStatus(ResponseStatus.FAIL.getValue());
			}else {
					CountDownLatch countDownLatch = lockStatus.countDownLatch;
					lockCountDownLatchMap.remove(key);
					if (countDownLatch != null) {
						countDownLatch.countDown();
				    }
			}
		}catch (Exception e){
			response.setStatus(ResponseStatus.FAIL.getValue());
		}
		return response;
	}
	
	
	

}
