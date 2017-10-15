package com.tingfeng.syRun.server.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.tingfeng.syRun.bean.CounterBean;

/**
 * 
 *	1. 后期考虑将System.currentMills更换为高性能的取时间计数器
 *  2. 定时移除过期counter,并且每次取数前检查是否过期
 */
public class CounterService {
	private static CounterService counterHelper = new CounterService();
	private Map<String, CounterBean>  counterMap = new HashMap<>(5000);
	private  int threadPoolSize = 1;
	private  long counterExpireRemoveInteval = 5 * 1000;//每5秒钟循环一次counter过期移出器;
	private List<String> counterKeys = new ArrayList<>(5000);
	
	private CounterService(){
		startRemoveExpiredCounter();	
	}
	/**
	 * 移出过时counter
	 */
	public void startRemoveExpiredCounter(){
		ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);//此线程仅仅作为一个补充移除功能
        service.submit(new Callable<CounterService>() {
			@Override
			public CounterService call() throws Exception {
				while(true){
					counterKeys.clear();
					Iterator<Map.Entry<String, CounterBean>> it = null;
						it = counterMap.entrySet().iterator();
					  while (it.hasNext()) {//得到超时的key
						  Map.Entry<String, CounterBean> entry = it.next();
						  if(System.currentTimeMillis() > entry.getValue().expireTime){
							  counterKeys.add(entry.getKey());
						  }
					  }
					  //移除超时的counter
					  for(int i = 0; i<counterKeys.size() ;i++){
						  counterHelper.removeCounter(counterKeys.get(i));
					  }
					  Thread.sleep(counterExpireRemoveInteval);
				}
			}
		});
	}
	
	public synchronized static CounterService getSigleInstance(){
		return counterHelper;
	}
	
	
	public synchronized long addCounter(CounterBean counter){
		if(null != counter && counter.expireTime > System.currentTimeMillis()){
			counterMap.put(counter.key, counter);
		}
		return counter.value;
	}
	
	public synchronized long addCounterValue(String key,long value){
		CounterBean counter = counterMap.get(key);
		if(null == counter || counter.expireTime < System.currentTimeMillis()){
			return  this.setCounterValue(key, value);
		}else{
			return  this.setCounterValue(key,counter.value + value);
		}
		
	}
	
	public synchronized void removeCounter(String key){
		if(null != key){
			counterMap.remove(key);
		}
	}
	
	public synchronized void removeCounter(CounterBean counter){
		if(null != counter){
			counterMap.remove(counter.key);
		}
	}
	
	public synchronized long setCounterValue(String key,long value){
		CounterBean counter = counterMap.get(key);
		if(null == counter || counter.expireTime < System.currentTimeMillis()){
			counter = new CounterBean(key);
			counterMap.put(key, counter);
		}	
		counter.value = value;
		return counter.value;
	}
	/**
	 * 
	 * @param oldKey 为空时,返回false
	 * @param newKey
	 */
	public synchronized boolean replaceCounterKey(String oldKey,String newKey){
		CounterBean counter = counterMap.get(oldKey);
		if(null != counter && counter.expireTime > System.currentTimeMillis()){
			counterMap.remove(oldKey);
			counterMap.put(newKey, counter);
			return true;
		}
		return false;
	}
	
	public synchronized CounterBean getCounter(String key){
		CounterBean counter = counterMap.get(key);
		if(null == counter || counter.expireTime < System.currentTimeMillis()){
			return null;
		}
		return counter;
	}
	

	public synchronized long getCounterValue(String key){
		CounterBean counter = counterMap.get(key);
		if(counter == null || counter.expireTime < System.currentTimeMillis())
		{
			return 0;
		}
		return counter.value;
	}
	
	public synchronized long getCounterExpireTime(String key){
		CounterBean counter = counterMap.get(key);
		if(null == counter || counter.expireTime < System.currentTimeMillis()){
			return 0;
		}
		return counter.expireTime;
	}
	
	public synchronized void setCounterExpireTime(String key,long expireTime){
		CounterBean counter = counterMap.get(key);
		if(null == counter){
			counter = new CounterBean(key);
			counterMap.put(key, counter);
		}
		counter.expireTime = expireTime;
	}
}
