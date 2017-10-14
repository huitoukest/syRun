package com.tingfeng.signleRun.server.util;

import java.util.Map;
import com.tingfeng.signleRun.bean.CounterParam;
import com.tingfeng.signleRun.bean.CounterParam;

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
