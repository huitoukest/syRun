package com.tingfeng.syRun.client.util;
/**
 *
 */
public class SingeStepException extends Exception {

	private long concurrency = 0;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SingeStepException( ){
	}
	public SingeStepException(long concurrency){
		this.concurrency = concurrency;
	}

	public long getConcurrency() {
		return concurrency;
	}

	public void setConcurrency(long concurrency) {
		this.concurrency = concurrency;
	}
	
	
}
