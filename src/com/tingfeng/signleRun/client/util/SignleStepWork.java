package com.tingfeng.signleRun.client.util;

import com.tingfeng.signleRun.client.bean.FrequencyBean;

/**
 *
 * @param <T>
 */
public interface SignleStepWork<T> {
	public T doWork(FrequencyBean frequencyBean);
}
