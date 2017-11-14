package com.tingfeng.syRun.server.util;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.MsgType;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.request.CounterParam;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.CodeConstants;
import com.tingfeng.syRun.common.ex.*;
import com.tingfeng.syRun.common.util.CheckUtil;
import com.tingfeng.syRun.server.controller.CounterController;
import com.tingfeng.syRun.server.controller.SyLockController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignleRunServerUtil {
	private static Logger logger = LoggerFactory.getLogger(SignleRunServerUtil.class);
	public static final CounterController syCounterController = new CounterController();
	public static final SyLockController syLockController = new SyLockController();

    public static ResponseBean doServerWork(String str) throws IOException {
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
				requestBean.setId(id);
				requestBean.setParams(requestParam);
				resultData = doSyLock(requestBean);
			}
			responseBean.setStatus(ResponseStatus.SUCCESS.getValue());
		}catch (Exception e){
			 if(!(e instanceof RelaseLockException)) {//RelaseLockException是锁异常后的释放,不需要发送消息
				 if (e instanceof OverRunTimeException) {
					 responseBean.setStatus(ResponseStatus.OVERRUNTIME.getValue());
					 logger.debug("运行超时", e);
				 } else if (e instanceof OverResponseException) {
					 responseBean.setStatus(ResponseStatus.OVERRESPONSETIME.getValue());
					 logger.debug("超时响应", e);
				 } else if (e instanceof CustomException) {
					 responseBean.setStatus(ResponseStatus.CUSTOM.getValue());
					 logger.debug("自定义错误", e);
				 } else if (e instanceof InfoException) {
					 responseBean.setStatus(ResponseStatus.CUSTOM.getValue());
				 } else {
					 responseBean.setStatus(ResponseStatus.FAIL.getValue());
					 logger.error("系统错误:", e);
				 }
				 if (!CheckUtil.isNull(e.getMessage())) {
					 responseBean.setErrorMsg(e.getMessage());
				 }
			 }else{
			 	throw  e;
			 }
		}
		responseBean.setData(resultData);
		return responseBean;
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
    		default:break;
    	}
    	return result;
	}

	private static String doCounter(CounterParam counterRequest) throws IOException{
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
			default:break;
	}
		if(null == result){
    		return null;
		}
    	return result.toString();
    }

	/**
	 * 处理发送失败的消息
	 * @param responseBean
	 */
	public static void dealFailSendWork(String reqMsg,ResponseBean responseBean) {
		try {
			JSONObject jsonObject = JSONObject.parseObject(reqMsg);
			Integer type = jsonObject.getInteger(CodeConstants.RquestKey.TYPE);
			if(type == MsgType.COUNTER.getValue())
			{
				CounterParam counterRequest = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, CounterParam.class);
				dealFailSendCounter(counterRequest,responseBean);
			}else if(type == MsgType.LOCK.getValue()){
				SyLockParam requestParam = jsonObject.getObject(CodeConstants.RquestKey.PARAMS, SyLockParam.class);
				RequestBean<SyLockParam> requestBean = new RequestBean<>();
				requestBean.setType(type);
				requestBean.setParams(requestParam);
				dealFailSendSyLock(requestBean,responseBean);
			}
		}catch (Exception e){
				responseBean.setStatus(ResponseStatus.FAIL.getValue());
				logger.error("发送消息失败后,系统处理失败:{},{}" ,e.getCause(),e.getMessage());

		}
	}

	/**
	 * 处理lock相关的发送失败的消息
	 * @param requestBean
	 * @param responseBean
	 */
	private static void dealFailSendSyLock(RequestBean<SyLockParam> requestBean, ResponseBean responseBean) {
		String method = requestBean.getParams().getMethod();
		String id = requestBean.getId();
		switch(method) {
			case "lock": {//只需要对加锁的消息释放锁
				requestBean.getParams().setLockId(responseBean.getData());
				syLockController.unlockSyLock(id, requestBean.getParams());
				break;
			}
			default:break;
		}
	}

	private static void dealFailSendCounter(CounterParam counterRequest, ResponseBean responseBean) {

	}
}
