package com.tingfeng.syrun.client.bean;

/**
 * 频率信息bean
 *
 */
public class FrequencyBean {
	/**
	 * 时间间隔长度,单位豪秒
	 */
	private Long interval ;
	/**
	 *次数 
	 */
	private Integer count ;
	
	/**
	 * 
	 * @param interval 时间间隔长度,单位豪秒
	 * @param count 次数 
	 */
	public FrequencyBean(Long interval, Integer count) {
		super();
		this.interval = interval;
		this.count = count;
	}

	public long getInterval() {
		return interval == null ? 0 : interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	public int getCount() {
		return count == null ? 0 : count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}
	
	
	
 }
