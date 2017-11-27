package com.tingfeng.syrun.common.ex;

/**
 * 超时异常,超过最大运行时间
 * @author huitoukest
 *
 */
public class OverRunTimeException extends MyException{

	public OverRunTimeException(String msg) {
		super(msg);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
