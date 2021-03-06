package com.tingfeng.syrun.server.util;

import java.util.Map;

import com.tingfeng.syrun.common.bean.request.CounterParam;

public class CounterUtil {
	/**
	 * 容器中为null的时候返回key相同的初始化counter
	 * @param key
	 * @param counterMap
	 * @return
	 */
	public static  CounterParam getInitCounterIfNull(String key,Map<String, CounterParam>  counterMap){
		CounterParam counter = counterMap.get(key);
		if(null == counter)
		{
			counter = new CounterParam(key);
		}
		return counter;
	}
}
