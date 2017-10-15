package com.tingfeng.syRun.server.util;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.bean.CounterParam;
import com.tingfeng.syRun.bean.RequestBean;
import com.tingfeng.syRun.bean.SyLockParam;
import com.tingfeng.syRun.common.CodeConstants;
import com.tingfeng.syRun.server.controller.CounterController;
import com.tingfeng.syRun.server.controller.SyLockController;

public class SignleRunServerUtil {
	public static final CounterController syCounterController = new CounterController();
	public static final SyLockController syLockController = new SyLockController();

    public static String doServerWork(String str) throws IOException {
		JSONObject jsonObject = JSONObject.parseObject(str);
		int type = jsonObject.getIntValue(CodeConstants.RquestKey.TYPE);
		String result = null;
		switch (type) {
		case CodeConstants.RquestType.COUNTER:
			 {
				 CounterParam counterRequest = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, CounterParam.class);
				 result = doCounter(counterRequest);
				 break;
			 }
		case CodeConstants.RquestType.SYNLOCK:
			{
				SyLockParam requestParam = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, SyLockParam.class);
				RequestBean<SyLockParam> requestBean = new RequestBean<>();
				requestBean.setId(jsonObject.getString(CodeConstants.RquestKey.ID));
				requestBean.setType(type);
				requestBean.setParams(requestParam);
				result = doSyLock(requestBean);
				break;
			}
		default:
			break;
		}		
		return result;
	}
	
    
    private static String doSyLock(RequestBean<SyLockParam> requestBean) {
    	String result = CodeConstants.Result.FAIL;
    	//String key = requestBean.getParams().getKey();
    	String method = requestBean.getParams().getMethod();
    	String id = requestBean.getId();
    	//String lockId = requestBean.getParams().getLockId();
    	switch(method){
    		case "lockSyLock":{
    			result = syLockController.lockSyLock(id, requestBean.getParams());
    			break;
    		}
    		case "unlockSyLock":{
    			result = syLockController.unlockSyLock(id, requestBean.getParams());
    			break;
    		}
    	}
    	return result;
	}

	public static String doCounter(CounterParam counterRequest) throws IOException{
    	String key = counterRequest.key;
    	long value = counterRequest.value;
    	long expireTime = counterRequest.expireTime;
    	String result = CodeConstants.Result.FAIL;
    	String method = counterRequest.getMethod();
    	switch(method){
    	case "initCounter":{
    		result = syCounterController.initCounter(key, value, expireTime);
    		break;
    	}
    	case "getCounterExpireTime":{
    		result = syCounterController.getCounterExpireTime(key);
    		break;
    	}
    	case "getCounterValue":{
    		result = syCounterController.getCounterValue(key);
    		break;
    	}
    	case "addCounterValue":{
    		result = syCounterController.addCounterValue(key, value);
    		break;
    	}
	}
    	return result;
    }

    
}
