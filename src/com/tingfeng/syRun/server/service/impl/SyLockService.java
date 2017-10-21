package com.tingfeng.syRun.server.service.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.CodeConstants;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.util.IdWorker;
/**
 * 
 * @author huitoukest
 *
 */
public class SyLockService {
	/***************************计划****************************************/
	/**
	 * 1.可以考虑增加定时器定时移除一些无用的超时锁key
	 * 2.每个线程检查锁的状态可以用一个线程统一回调
	 * 3.锁的同步实现中增加超时设置,替代取消功能
	 * 4.异步的设置中增加取消获取锁的设置
	 */
	/****************************计划***************************************/
	private static final SyLockService syLockService = new SyLockService();
	private static final int threadPoolSize = 1000;
	private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadPoolSize);//此线程仅仅作为一个补充移除功能
	//指定方法完成的执行器
	private static final ExecutorCompletionService<ResponseBean> completion
														= new ExecutorCompletionService<>(servicePool);
	/**
	 * 锁状态的Map,,key是当前key,value是存放当前加锁的锁id
	 */
	private static final Map<String,String> lockStatusMap = new ConcurrentHashMap<>(5000);
	/**
	 * 是否释放锁的的检查时间间隔,单位毫秒
	 */
	public static final int lockCheckInterval = 5;
	
	private SyLockService(){
				
	}
	
	public static SyLockService getInstance() {
		return syLockService;
	}

	public ResponseBean lockSyLock(String id,final SyLockParam syLockParam) {
		final ResponseBean response = new ResponseBean();
		response.setData(IdWorker.getUUID() + "");
		response.setStatus(ResponseStatus.FAIL.getValue());
		
		Future<ResponseBean> future = completion.submit(() ->{
			String key = syLockParam.getKey();						
				String lockId = null;
				boolean isSleep = false;
				String oldLockId = response.getData();
				do {
					
					synchronized (key) {
						lockId = lockStatusMap.get(key);
						if(null == lockId) {
							response.setStatus(ResponseStatus.SUCCESS.getValue());
							lockStatusMap.put(key, response.getData());
						}else {
							isSleep = true;
						}
					}
					if(isSleep)
					{
						Thread.sleep(lockCheckInterval);
					}
				}while(lockId != null && lockId != oldLockId );				 
			
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
        }catch (Exception e) {
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
	public ResponseBean unlockSyLock(String id,final SyLockParam syLockParam) {
		ResponseBean response = new ResponseBean();
		response.setId(id);
		response.setStatus(ResponseStatus.SUCCESS.getValue());
		response.setData(syLockParam.getLockId());
		
		String key = syLockParam.getKey();
		synchronized (key) {
			String	lockId = lockStatusMap.get(key);
			if(lockId == null || !lockId.equals(syLockParam.getLockId())) {
				response.setStatus(ResponseStatus.FAIL.getValue());
			}else {
				lockStatusMap.remove(syLockParam.getKey());
			}
		}
		return response;
	}
	
	
	

}
