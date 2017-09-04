package com.tingfeng.signleRun.util;

import java.util.Map;
import com.tingfeng.signleRun.bean.Counter;

public class CounterUtil {
	/**
	 * 容器中为null的时候返回key相同的初始化counter
	 * @param key
	 * @param counterMap
	 * @return
	 */
	public static  Counter getInitCounterIfNull(String key,Map<String, Counter>  counterMap){
		Counter counter = counterMap.get(key);
		if(null == counter)
		{
			counter = new Counter(key);
		}
		return counter;
	}
}
