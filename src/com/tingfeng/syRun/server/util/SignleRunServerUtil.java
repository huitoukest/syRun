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
import com.tingfeng.syRun.common.util.CheckUtil;
import com.tingfeng.syRun.server.SyRunTCPServer;
import com.tingfeng.syRun.server.controller.CounterController;
import com.tingfeng.syRun.server.controller.SyLockController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignleRunServerUtil {
	private static Logger logger = LoggerFactory.getLogger(SignleRunServerUtil.class);
	public static final CounterController syCounterController = new CounterController();
	public static final SyLockController syLockController = new SyLockController();

    public static String doServerWork(String str) throws IOException {
		ResponseBean responseBean = new ResponseBean();
		String resultData = null;
		try {
			if(CheckUtil.isNull(str)){
				throw new CustomException("msg is null");
			}
			JSONObject jsonObject = JSONObject.parseObject(str);
			Integer type = jsonObject.getInteger(CodeConstants.RquestKey.TYPE);
			String id = jsonObject.getString(CodeConstants.RquestKey.ID);

			if(CheckUtil.isNull(id) || CheckUtil.isNull(type))
			{
				throw new CustomException("id or type is null");
			}
			responseBean.setId(id);
		    if(type == MsgType.COUNTER.getValue())
			{
				CounterParam counterRequest = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, CounterParam.class);
				resultData = doCounter(counterRequest);
			}else if(type == MsgType.LOCK.getValue()){
				SyLockParam requestParam = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, SyLockParam.class);
				RequestBean<SyLockParam> requestBean = new RequestBean<>();
				requestBean.setType(type);
				requestBean.setParams(requestParam);
				resultData = doSyLock(requestBean);
			}
			responseBean.setStatus(ResponseStatus.SUCCESS.getValue());
		}catch (Exception e){
			if(e instanceof OverRunTimeException){
				responseBean.setStatus(ResponseStatus.OVERRUNTIME.getValue());
				logger.debug(e.getCause().toString());
			}else if(e instanceof OverResponseException){
				responseBean.setStatus(ResponseStatus.OVERRESPONSETIME.getValue());
				logger.debug(e.getCause().toString());
			}else if(e instanceof CustomException){
				responseBean.setStatus(ResponseStatus.CUSTOM.getValue());
				logger.debug(e.getCause().toString());
			}else{
				responseBean.setStatus(ResponseStatus.FAIL.getValue());
				logger.error("系统错误:" + e.getCause().toString());
			}
			if(!CheckUtil.isNull(e.getMessage())){
				responseBean.setErrorMsg(e.getMessage());
			}
			System.out.println("error msg is:" + str);
		}
		responseBean.setData(resultData);
		return JSONObject.toJSONString(responseBean);
	}
	
    
    private static String doSyLock(RequestBean<SyLockParam> requestBean) {
    	if(CheckUtil.isNull(requestBean.getParams().getKey()))
		{
			throw new CustomException("null key");
		}
    	String method = requestBean.getParams().getMethod();
		if(CheckUtil.isNull(method))
		{
			throw new CustomException("null method");
		}
    	String id = requestBean.getId();
		String result = null;
    	switch(method){
    		case "lock":{
    			result = syLockController.lockSyLock(id, requestBean.getParams());
    			break;
    		}
    		case "unLock":{
    			syLockController.unlockSyLock(id, requestBean.getParams());
    			break;
    		}
    	}
    	return result;
	}

	public static String doCounter(CounterParam counterRequest) throws IOException{
    	String key = counterRequest.key;
		if(CheckUtil.isNull(key))
		{
			throw new CustomException("null key");
		}
    	long value = counterRequest.value;
    	long expireTime = counterRequest.expireTime;
    	Object result = "";
    	String method = counterRequest.getMethod();
		if(CheckUtil.isNull(key))
		{
			throw new CustomException("null method");
		}
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
