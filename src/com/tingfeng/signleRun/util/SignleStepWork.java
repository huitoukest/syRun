package com.tingfeng.signleRun.util;

import com.tingfeng.signleRun.bean.FrequencyBean;

/**
 * redis任务
 *
 * @param <T>
 */
public interface SignleStepWork<T> {
	public T doWork(FrequencyBean frequencyBean);
}
