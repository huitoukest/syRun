package com.tingfeng.signleRun.server.service.impl;

public class ReturnUtil {
	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	
	/**
	 * @return success or fail
	 */
	public static String getReturnMsg(Runnable runnable){		
		try {
			runnable.run();
		} catch (Exception e) {
			e.printStackTrace();
			return FAIL;
		}	
		return SUCCESS;
		
	} 
}
