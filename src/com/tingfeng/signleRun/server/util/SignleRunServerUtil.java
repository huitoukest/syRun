package com.tingfeng.signleRun.server.util;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import com.tingfeng.signleRun.bean.CounterParam;
import com.tingfeng.signleRun.common.CodeConstants;
import com.tingfeng.signleRun.server.controller.CounterController;

public class SignleRunServerUtil {
	public static final CounterController signleRunController = new CounterController();
    

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
		default:
			break;
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
    		result = signleRunController.initCounter(key, value, expireTime);
    		break;
    	}
    	case "getCounterExpireTime":{
    		result = signleRunController.getCounterExpireTime(key);
    		break;
    	}
    	case "getCounterValue":{
    		result = signleRunController.getCounterValue(key);
    		break;
    	}
    	case "addCounterValue":{
    		result = signleRunController.addCounterValue(key, value);
    		break;
    	}
	}
    	return result;
    }

    
}
