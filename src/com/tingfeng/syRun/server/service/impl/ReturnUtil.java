package com.tingfeng.syRun.server.service.impl;

import java.util.concurrent.Callable;

import com.tingfeng.syRun.common.CodeConstants;

public class ReturnUtil {
	
	/*
	 * @see CodeConstants.Result
	 * @return success or fail
	 */
	public static String getCounterReturnMsg(Runnable run){	
		try {
			run.run();
		}catch (Exception e) {
			e.printStackTrace();
			return CodeConstants.Result.FAIL;
		}	
		return CodeConstants.Result.SUCCESS;
		
	} 
	
	/**@call 返回CodeConstants.Result定义结果
	 * @see CodeConstants.Result
	 * @return success or fail or CodeConstants.Result.OVERTIME
	 */
	public static String getSyLockReturnMsg(Callable<String> call){		
		try {
			return call.call();
		}catch (Exception e) {
			e.printStackTrace();
			return CodeConstants.Result.FAIL;
		}	
	} 
}
