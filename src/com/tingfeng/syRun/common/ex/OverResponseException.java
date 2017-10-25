package com.tingfeng.syRun.common.ex;

/**
 * 超时异常,超过最大响应时间
 * @author huitoukest
 *
 */
public class OverResponseException extends RuntimeException{

	public OverResponseException(String msg) {
		super(msg);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
