package com.tingfeng.signleRun.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.tingfeng.signleRun.bean.FrequencyBean;
import com.tingfeng.signleRun.util.SignleRunClientUtil;
import com.tingfeng.signleRun.util.SingeStepException;


public class FrequencyControlHelper {
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
	 * @throws T 
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public <T extends Exception> void startDoWork(T e,String key) throws T, IOException, InterruptedException{
		
		for(int i= 0 ;i < this.frequencyBeans.size();i++){
			FrequencyBean fb = this.frequencyBeans.get(i);
			Long interval = fb.getInterval();
			Integer configCount = fb.getCount();
				Long count = SignleRunClientUtil.addCounterValue(key, 1);
				Long expire = SignleRunClientUtil.getCounterExpireTime(key);
				if (expire == null || expire <= 0) {
					SignleRunClientUtil.setCounterExpireTime(key, System.currentTimeMillis() + interval.longValue());
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
	 * @param key redis中保存频率信息的数据唯一性的Key ,如hsh:ticketUse:15236524123L
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void endDoWork(String key) throws IOException, InterruptedException{
		for(int i= 0 ;i < this.frequencyBeans.size();i++){
			SignleRunClientUtil.addCounterValue(key, -1);
		}
	}
	
}
