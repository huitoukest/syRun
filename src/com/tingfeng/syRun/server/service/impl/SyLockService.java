package com.tingfeng.syRun.server.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.ex.InfoException;
import com.tingfeng.syRun.common.ex.OverRunTimeException;
import com.tingfeng.syRun.common.ex.ReleaseLockException;
import com.tingfeng.syRun.common.util.CheckUtil;
import com.tingfeng.syRun.common.util.IdWorker;
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
	 * 被lock阻塞的线程的数量
	 */
	private static final AtomicInteger atomicInteger = new AtomicInteger(0);

	/**
	 * 锁状态的Map,,key是当前key,value当前线程计数器
	 */
	private static final Map<String,SyLockStatusBean> lockCountDownLatchMap = new ConcurrentHashMap<>(5000);

    /**
     * 每个sesssion中被阻塞的线程数量
     */
    private static final Map<String,Integer> sessionBlockSizeMap = new ConcurrentHashMap<>(5000);

    /**
     * 每个sesssion中被阻塞的key信息
     */
    private static final Map<String,String> sessionAndKeyMap = new ConcurrentHashMap<>(5000);

    /**
     * 需要移出的session阻塞数据
     */
    private static final List<String> sessionRemoveBlockList = new ArrayList<>(100);


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
     * @param sessionKey 等同于一个客户端连接的标志,当客户端断开时,用来处理其lock住的锁信息,不然其lock将只能等待自动超时断开
     * @param id
     * @param syLockParam
     * @return
     */
	public String lockSyLock(String sessionKey,String id,final SyLockParam syLockParam) {
		if(atomicInteger.get() >= ConfigEntity.getInstance().getServerMaxLockSize()){
			throw  new InfoException("lock size reach the limit count : " + atomicInteger.get());
		}
		atomicInteger.incrementAndGet();
        String lockId = IdWorker.getUUID() + "";
		SyLockStatusBean lockStatus = null;
		String key = syLockParam.getKey();
		boolean isOverTime = false;
		try {
			logger.debug("server:lockSyLock:id:{},lock:re:{}" ,id, JSONObject.toJSONString(syLockParam));
            addBlockSize(sessionKey,1);
			synchronized (key.intern()) {
                sessionAndKeyMap.put(sessionKey,key);
                if(sessionRemoveBlockList.contains(sessionKey)){//如果是
                    addBlockSize(sessionKey,-1);
                    throw new ReleaseLockException("释放锁");
                }
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
			logger.error("server lock error",e);
			if (e instanceof InterruptedException || isOverTime) {
				throw  new OverRunTimeException("lock time over max milliseconds:" + ConfigEntity.getInstance().getTimeOutRun());
			}
			SyLockStatusBean statusBean = lockCountDownLatchMap.get(key);
			if (null != statusBean && lockId.equals(statusBean.lockId)) {
				if (null != statusBean.countDownLatch) {
					statusBean.countDownLatch.countDown();
                    addBlockSize(sessionKey,-1);
				}
				lockCountDownLatchMap.remove(key);
			}
		}finally {
			atomicInteger.decrementAndGet();
		}
        sessionAndKeyMap.remove(key);
		logger.debug("server:lockSyLock:id:{},lockId:{},se:{}",id,lockId ,JSONObject.toJSONString(syLockParam));
		return lockId;
	}
	/**
	 * 失败抛出异常
	 * @param id
	 * @param syLockParam
	 * @return
	 */
	public void unlockSyLock(String sessionKey,String id,final SyLockParam syLockParam) {
		logger.debug("server:unlockSyLock:id:{},unlock:re:{}",id,JSONObject.toJSONString(syLockParam));
		String key = syLockParam.getKey();
		SyLockStatusBean lockStatus = null;
		lockStatus = lockCountDownLatchMap.get(key);
		String localLockId = lockStatus.lockId;
		if(CheckUtil.isNull(localLockId)|| !localLockId.equals(syLockParam.getLockId())){
			if(CheckUtil.isNull(localLockId)){
				throw  new InfoException("please lock first!");
			}else{
				throw  new InfoException("other is lock , wait until you lock success!");
			}
		}else {
				CountDownLatch countDownLatch = lockStatus.countDownLatch;
				lockCountDownLatchMap.remove(key);
				if (countDownLatch != null) {
					countDownLatch.countDown();
                    addBlockSize(sessionKey,-1);
				}
		}
		logger.debug("server:unlockSyLock:id:{},unlock:se:{}" ,id, JSONObject.toJSONString(syLockParam));
	}

    /**
     * 增加sessionKey向下阻塞的线程值,同时在阻塞值为0时移出Map和List中的阻塞信息.
     */
	private void  addBlockSize(String sessionKey,Integer addvalue){
        synchronized (SyLockService.this) {
            Integer blockSize = sessionBlockSizeMap.get(sessionKey);
            if(addvalue > 0){
                if(null == blockSize ){
                    blockSize = 0;
                }
                sessionBlockSizeMap.put(sessionKey, blockSize + addvalue);
            }else if(null != blockSize){
                if(blockSize + addvalue == 0){
                    sessionRemoveBlockList.remove(sessionKey);
                    sessionBlockSizeMap.remove(sessionKey);
                    sessionAndKeyMap.remove(sessionKey);
                }else {
                    sessionBlockSizeMap.put(sessionKey, blockSize + addvalue);
                }
            }
        }
    }


    /**
    ** 移出同一客户端下所有的阻塞线程*/
    public static void removeBlockLock(String sessionKey){
        sessionRemoveBlockList.add(sessionKey);
        String lockKey = sessionAndKeyMap.get(sessionKey);
        if(null != lockKey){
            SyLockStatusBean lockStatus = lockCountDownLatchMap.get(lockKey);
            CountDownLatch countDownLatch = lockStatus.countDownLatch;
            lockCountDownLatchMap.remove(lockKey);
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }
    }


}
