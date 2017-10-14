package com.tingfeng.signleRun.server.controller;
import java.io.IOException;
import com.tingfeng.signleRun.bean.CounterBean;
import com.tingfeng.signleRun.server.service.impl.CounterHelper;
import com.tingfeng.signleRun.server.service.impl.ReturnUtil;

public class SignleRunController {
	private static CounterHelper counterHelper =  CounterHelper.getSigleInstance();	
	public  SignleRunController(){}
	/**
	 * 
	 * @param key 计数器的key
	 * @param value 初始化的值,为null时默认变为0
	 * @param expireTime 过期时间
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */

    public  String initCounter(final String key,final long value,final long expireTime) throws IOException{
    	return ReturnUtil.getReturnMsg(new Runnable() {
			@Override
			public void run() {
				CounterBean counter = new CounterBean(value, key, expireTime);
		    	 counterHelper.addCounter(counter);
			}
		});
    }
    

    public  String setCounterValue(final String key,final long value) throws IOException{
    	return ReturnUtil.getReturnMsg(new Runnable() {
			@Override
			public void run() {
		    	 counterHelper.setCounterValue(key, value);
			}
		});
    }
    

    public String setCounterExpireTime(final String key,final long expireTime) throws IOException{
    	return ReturnUtil.getReturnMsg(new Runnable() {		
			@Override
			public void run() {
		    	 counterHelper.setCounterExpireTime(key, expireTime);
			}
		});
    }
    
    public  String getCounterExpireTime(final String key) throws IOException{
    	return  counterHelper.getCounterExpireTime(key)  + "";
    }
    /**
     * 
     * @param key
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    public String getCounterValue(String key) throws IOException{
    	return counterHelper.getCounterValue(key) + "";
    }
    /**
     * 
     * @param key
     * @param addValue if null ,convert to 0
     * @param request
     * @param response
     * @return return the value
     * @throws IOException
     */
    public String addCounterValue(String key,long value) throws IOException{
    	return counterHelper.addCounterValue(key, value)  + "";
    }
    
}
