package com.tingfeng.syrun.server.controller;
import java.io.IOException;

import com.tingfeng.syrun.server.bean.CounterBean;
import com.tingfeng.syrun.server.service.impl.CounterService;

public class CounterController {
	private static CounterService counterHelper =  CounterService.getSigleInstance();	
	public  CounterController(){}
	/**
	 * @param key 计数器的key
	 * @param value 初始化的值,为null时默认变为0
	 * @param expireTime 过期时间
	 * @return null
	 * @throws IOException
	 */
    public  String initCounter(final String key,final long value,final long expireTime) throws IOException{
		CounterBean counter = new CounterBean(value, key, expireTime);
		counterHelper.addCounter(counter);
		return null;
    }

	/**
	 *
	 * @param key
	 * @param value
	 * @return null
	 * @throws IOException
	 */
    public  String setCounterValue(final String key,final long value) throws IOException{
		counterHelper.setCounterValue(key, value);
		return null;
    }

	/**
	 *
	 * @param key
	 * @param expireTime
	 * @return null
	 * @throws IOException
	 */
    public String setCounterExpireTime(final String key,final long expireTime) throws IOException{
		counterHelper.setCounterExpireTime(key, expireTime);
		return null;
    }
    
    public  long getCounterExpireTime(final String key) throws IOException{
    	return  counterHelper.getCounterExpireTime(key);
    }
    /**
     * 
     * @param key
     * @return
     * @throws IOException
     */
    public long getCounterValue(String key) throws IOException{
    	return counterHelper.getCounterValue(key) ;
    }
    /**
     * @param key
     * @return return the value
     * @throws IOException
     */
    public long addCounterValue(String key,long value) throws IOException{
    	return counterHelper.addCounterValue(key, value) ;
    }
    
}
