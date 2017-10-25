package com.tingfeng.syRun.server.util;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.client.util.MsgType;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.request.CounterParam;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.CodeConstants;
import com.tingfeng.syRun.common.ex.CustomException;
import com.tingfeng.syRun.common.ex.OverResponseException;
import com.tingfeng.syRun.common.ex.OverRunTimeException;
import com.tingfeng.syRun.server.controller.CounterController;
import com.tingfeng.syRun.server.controller.SyLockController;

public class SignleRunServerUtil {
	public static final CounterController syCounterController = new CounterController();
	public static final SyLockController syLockController = new SyLockController();

    public static String doServerWork(String str) throws IOException {
		ResponseBean responseBean = new ResponseBean();
		String result = null;
		try {
			JSONObject jsonObject = JSONObject.parseObject(str);
			int type = jsonObject.getIntValue(CodeConstants.RquestKey.TYPE);
			String id = jsonObject.getString(CodeConstants.RquestKey.ID);
			responseBean.setId(id);
			if(type == MsgType.COUNTER.getValue())
			{
				CounterParam counterRequest = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, CounterParam.class);
				result = doCounter(counterRequest);
			}else if(type == MsgType.LOCK.getValue()){
				SyLockParam requestParam = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, SyLockParam.class);
				RequestBean<SyLockParam> requestBean = new RequestBean<>();
				requestBean.setType(type);
				requestBean.setParams(requestParam);
				result = doSyLock(requestBean);
			}
			responseBean.setStatus(ResponseStatus.SUCCESS.getValue());
		}catch (Exception e){
			if(e instanceof OverRunTimeException){
				responseBean.setStatus(ResponseStatus.OVERRUNTIME.getValue());
			}else if(e instanceof OverResponseException){
				responseBean.setStatus(ResponseStatus.OVERRESPONSETIME.getValue());
			}else if(e instanceof CustomException){
				responseBean.setStatus(ResponseStatus.CUSTOM.getValue());
				responseBean.setErrorMsg(e.getCause().toString());
			}else{
				responseBean.setStatus(ResponseStatus.FAIL.getValue());
				responseBean.setErrorMsg(e.getCause().toString());
			}
			e.printStackTrace();
			System.out.println("error msg is:" + str);
		}
		responseBean.setData(result);
		return JSONObject.toJSONString(responseBean);
	}
	
    
    private static String doSyLock(RequestBean<SyLockParam> requestBean) {
    	String result = "";
    	//String key = requestBean.getParams().getKey();
    	String method = requestBean.getParams().getMethod();
    	String id = requestBean.getId();
    	//String lockId = requestBean.getParams().getLockId();
    	switch(method){
    		case "lock":{
    			result = syLockController.lockSyLock(id, requestBean.getParams());
    			break;
    		}
    		case "unLock":{
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
    	Object result = "";
    	String method = counterRequest.getMethod();
    	switch(method){
    	case "initCounter":{
    		result = syCounterController.initCounter(key, value, expireTime);
    		break;
    	}
		case "setCounterValue":{
			result = syCounterController.setCounterValue(key, value);
			break;
		}
    	case "getCounterExpireTime":{
    		result = syCounterController.getCounterExpireTime(key);
    		break;
    	}
		case "setCounterExpireTime":{
			result = syCounterController.setCounterExpireTime(key,expireTime);
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
		if(null == result){
    		return null;
		}
    	return result.toString();
    }

    
}
