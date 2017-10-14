package com.tingfeng.signleRun.server.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.tingfeng.signleRun.bean.SyLockParam;
import com.tingfeng.signleRun.bean.SyLockResponse;
import com.tingfeng.signleRun.common.CodeConstants;
import com.tingfeng.signleRun.common.util.IdWorker;
/**
 * 
 * @author huitoukest
 *
 */
public class SyLockService {
	/***************************计划****************************************/
	/**
	 * 1.可以考虑增加定时器定时移除一些无用的超时锁key
	 */
	/****************************计划***************************************/
	private static final SyLockService syLockService = new SyLockService();
	private static final int threadPoolSize = 10;
	private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadPoolSize);//此线程仅仅作为一个补充移除功能
	//指定方法完成的执行器
	private static final ExecutorCompletionService<SyLockResponse> completion
														= new ExecutorCompletionService<>(servicePool);
	/**
	 * 锁状态的Map,true表示被锁,false表示释放锁
	 */
	private static final Map<String,Boolean> lockStatusMap = new ConcurrentHashMap<>(5000);
	/**
	 * 是否释放锁的的检查时间间隔,单位毫秒
	 */
	public static final int lockCheckInterval = 5;
	
	private SyLockService(){
				
	}
	
	public static SyLockService getInstance() {
		return syLockService;
	}

	public SyLockResponse lockSyLock(String id,final SyLockParam syLockParam) {
		final SyLockResponse response = new SyLockResponse();
		response.setLockId(IdWorker.getUUID() + "");
		response.setResult(CodeConstants.Result.FAIL);
		
		Future<SyLockResponse> future = completion.submit(() ->{
			String key = syLockParam.getKey();
			synchronized (key) {			
				Boolean status = null;
				do {
					status = lockStatusMap.get(response.getLockId());
					if(null == status) {
						status = false;
					}else if(false == status) {
						response.setResult(CodeConstants.Result.SUCCESS);
					}else {
						Thread.sleep(lockCheckInterval);
					}	
				}while(status);				
				lockStatusMap.put(response.getLockId(), true);
			}
			return response;
		});	
		try {  
			future.get(36000 * 1000, TimeUnit.MILLISECONDS);// 设定在10小时的时间内完成   
        } catch (InterruptedException e) {
            System.out.println("线程中断出错。");
            e.printStackTrace();
            future.cancel(true);// 中断执行此任务的线程
            lockStatusMap.remove(syLockParam.getLockId());
        } catch (ExecutionException e) {
            System.out.println("线程服务出错。");
            e.printStackTrace();
            future.cancel(true);// 中断执行此任务的线程
            lockStatusMap.remove(syLockParam.getLockId());
        } catch (TimeoutException e) {// 超时异常 
            System.out.println("超时。");
            e.printStackTrace();
            future.cancel(true);// 中断执行此任务的线程 
            lockStatusMap.remove(syLockParam.getLockId());
        }
		return response;		
	}
	/**
	 * 
	 * @param id
	 * @param syLockParam
	 * @return
	 */
	public SyLockResponse unlockSyLock(String id,final SyLockParam syLockParam) {
		SyLockResponse response = new SyLockResponse();
		response.setLockId(IdWorker.maxWorkerId + "");
		response.setResult(CodeConstants.Result.SUCCESS);
		response.setLockId(syLockParam.getLockId());
		lockStatusMap.remove(syLockParam.getLockId());	
		return response;
	}
	
	
	

}
