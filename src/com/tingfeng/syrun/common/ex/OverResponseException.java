package com.tingfeng.syrun.common.ex;

/**
 * 超时异常,超过最大响应时间
 * @author huitoukest
 *
 */
public class OverResponseException extends MyException{

	public OverResponseException(String msg) {
		super(msg);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
