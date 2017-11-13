package com.tingfeng.syRun.server.service.impl;

import java.util.Map;
import java.util.concurrent.*;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.ex.CustomException;
import com.tingfeng.syRun.common.ex.OverRunTimeException;
import com.tingfeng.syRun.common.util.CheckUtil;
import com.tingfeng.syRun.common.util.IdWorker;
import com.tingfeng.syRun.server.SyRunTCPServer;
import com.tingfeng.syRun.server.bean.SyLockStatusBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author huitoukest
 *
 */
public class SyLockService {
	private static Logger logger = LoggerFactory.getLogger(SyLockService.class);
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
	private static final Map<String,SyLockStatusBean> lockCountDownLatchMap = new ConcurrentHashMap<>(5000);

	/**
	 * 是否释放锁的的检查时间间隔,单位毫秒
	 */
	public  static final int lockCheckInterval = 5;
	
	private SyLockService(){
				
	}
	
	public static SyLockService getInstance() {
		return syLockService;
	}

    /**
     * 成功返回锁id,失败抛出异常
     * @param id
     * @param syLockParam
     * @return
     */
	public String lockSyLock(String id,final SyLockParam syLockParam) {
		logger.debug("server:lockSyLock:id:{},lock:re:{}" ,id, JSONObject.toJSONString(syLockParam));
        String lockId = IdWorker.getUUID() + "";
		SyLockStatusBean lockStatus = null;
		String key = syLockParam.getKey();
		boolean isOverTime = false;
		try {
			synchronized (key.intern()) {
				//synchronized使用的是对象的引用,而不是对象的值,所以每次构建新的字符串会导致同步锁失效.
				//Stirng.intern()在jdk6,7,8下的存储位置可能不一致.但是都是从常量池中取出的统一对象
				lockStatus = lockCountDownLatchMap.get(key);
				if (null != lockStatus) {//如果需要等待
					isOverTime = !lockStatus.countDownLatch.await(ConfigEntity.getInstance().getTimeOutRun(), TimeUnit.MILLISECONDS);
				} else {
					lockStatus = new SyLockStatusBean();
				}
				CountDownLatch countDownLatch = new CountDownLatch(1);
				lockStatus.countDownLatch = countDownLatch;
				lockStatus.lockId = lockId;
				lockCountDownLatchMap.put(key, lockStatus);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof InterruptedException || isOverTime) {
				throw  new OverRunTimeException("lock time over max milliseconds:" + ConfigEntity.getInstance().getTimeOutRun());
			}
			SyLockStatusBean statusBean = lockCountDownLatchMap.get(key);
			if (null != statusBean && lockId.equals(statusBean.lockId)) {
				if (null != statusBean.countDownLatch) {
					statusBean.countDownLatch.countDown();
				}
				lockCountDownLatchMap.remove(key);
			}
		}
		logger.debug("server:lockSyLock:id:{},lockId:{},se:{}",id,lockId ,JSONObject.toJSONString(syLockParam));
		return lockId;
	}
	/**
	 * 失败抛出异常
	 * @param id
	 * @param syLockParam
	 * @return
	 */
	public void unlockSyLock(String id,final SyLockParam syLockParam) {
		logger.debug("server:unlockSyLock:id:{},unlock:re:{}",id,JSONObject.toJSONString(syLockParam));
		String key = syLockParam.getKey();
		SyLockStatusBean lockStatus = null;
		lockStatus = lockCountDownLatchMap.get(key);
		String localLockId = lockStatus.lockId;
		if(CheckUtil.isNull(localLockId)|| !localLockId.equals(syLockParam.getLockId())){
			if(CheckUtil.isNull(localLockId)){
				throw  new CustomException("please lock first!");
			}else{
				throw  new CustomException("other is lock , wait until you lock success!");
			}
		}else {
				CountDownLatch countDownLatch = lockStatus.countDownLatch;
				lockCountDownLatchMap.remove(key);
				if (countDownLatch != null) {
					countDownLatch.countDown();
				}
		}
		logger.debug("server:unlockSyLock:id:{},unlock:se:{}" ,id, JSONObject.toJSONString(syLockParam));
	}

}
