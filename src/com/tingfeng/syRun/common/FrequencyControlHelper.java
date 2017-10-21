package com.tingfeng.syRun.common;

import java.util.ArrayList;
import java.util.List;

import com.tingfeng.syRun.client.bean.FrequencyBean;
import com.tingfeng.syRun.client.util.SingeStepException;

/**
 *  频率检查类
 *  在指定的的计数器有效时间段内允许的最大值检测
 * @author huitoukest
 * @param T doWork的返回值
 */
public abstract class FrequencyControlHelper<T> {
	private List<FrequencyBean> frequencyBeans = null;
	
	public FrequencyControlHelper(List<FrequencyBean> frequencyBeans){
		this.frequencyBeans = frequencyBeans;
	}
	public FrequencyControlHelper(FrequencyBean frequencyBean){
		this.frequencyBeans = new ArrayList<FrequencyBean>();
		frequencyBeans.add(frequencyBean);
	}
	/**
	 * 检查访问频率,当超过设定的频率后,抛出此异常
	 * @param e
	 * @param key redis中保存频率信息的数据唯一性的Key,如hsh:ticketUse:15236524123L
	 * @throws Exception 
	 */
	public <T extends Exception> void startDoWork(T e,String key) throws Exception{
		
		for(int i= 0 ;i < this.frequencyBeans.size();i++){
			FrequencyBean fb = this.frequencyBeans.get(i);
			Long interval = fb.getInterval();
			Integer configCount = fb.getCount();
				Long count = this.addCounterValue(key, 1);
				Long expire = this.getExpireTime(key);
				if (expire == null || expire <= 0) {
					this.setExpireTime(key, System.currentTimeMillis() + interval.longValue());
				}
				if(count > configCount){
					if(e instanceof SingeStepException){
						((SingeStepException)e).setConcurrency(count);
					}
					throw e;
				}
	   }
	}
	/**
	 * 检查合格之后做你的工作
	 * @return
	 */
	public abstract  T doWork() throws Exception ;
	/**
	 * 根据key获取其过期时间
	 * @return
	 * @throws Exception 
	 */
	public abstract long getExpireTime(String key) throws Exception;
	/**
	 * 计数器的加
	 * @param key
	 * @param value
	 * @return
	 */
	public abstract long addCounterValue(String key,int value) throws Exception ;
	/**
	 * 设置key的过期时间
	 * @param key
	 * @param expireTime
	 * @return
	 */
	public abstract void setExpireTime(String key,long expireTime) throws Exception ;
	
	
	
	/**
	 * @param key redis中保存频率信息的数据唯一性的Key ,如hsh:ticketUse:15236524123L
	 * @throws Exception 
	 */
	public void endDoWork(String key) throws Exception{
		for(int i= 0 ;i < this.frequencyBeans.size();i++){
			this.addCounterValue(key, -1);
		}
	}
	
}
