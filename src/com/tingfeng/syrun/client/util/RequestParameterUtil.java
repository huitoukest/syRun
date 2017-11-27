package com.tingfeng.syrun.client.util;

import com.tingfeng.syrun.common.MsgType;
import com.tingfeng.syrun.common.bean.request.BaseRequestParam;
import com.tingfeng.syrun.common.bean.request.CounterParam;
import com.tingfeng.syrun.common.bean.request.RequestBean;
import com.tingfeng.syrun.common.bean.request.SyLockParam;
import com.tingfeng.syrun.common.RequestType;
import com.tingfeng.syrun.common.util.RequestUtil;

/**
 * 根据参数,构建相应的请求参数
 */
public class RequestParameterUtil {

    private static <T extends BaseRequestParam> RequestBean<T> getParam(RequestType requestType, MsgType msgType, T param){
        RequestBean<T> requestBean = new RequestBean<>();
        String id = "";
        if(RequestType.ASY.equals(requestType)){
            id = RequestUtil.getASychronizedMsgId();
        }else if(RequestType.SY.equals(requestType)){
            id = RequestUtil.getSychronizedMsgId();
        }else{
            throw  new RuntimeException("error requestType!");
        }
        if(null == msgType){
            throw  new RuntimeException("null msgType!");
        }
        requestBean.id  = id;
        requestBean.type = msgType.getValue();
        requestBean.params = param;
        return requestBean;
    }

    /**********************************************************计数器相关参数***********************************************************************/
    private static RequestBean<CounterParam> getCounterParam(RequestType requestType, String method, String key, long value, long expireTime){
        CounterParam counterParam= new CounterParam(method,value, key, expireTime);
        return getParam(requestType,MsgType.COUNTER,counterParam);
    }

    public static RequestBean<CounterParam> getParamOfInitCounter(RequestType requestType, final String key, final long value, final long expireTime){
        return getCounterParam(requestType,"initCounter", key,value, expireTime);
    }

    public static  RequestBean<CounterParam> getParamOfSetCounterValue(RequestType requestType, final String key, final long value){
        return getCounterParam(requestType,"setCounterValue", key,value, 0);
    }

    public static RequestBean<CounterParam> getParamOfSetCounterExpireTime(RequestType requestType, final String key, final long expireTime){
        return getCounterParam(requestType,"setCounterExpireTime", key,0, expireTime);
    }

    public static RequestBean<CounterParam> getParamOfGetCounterExpireTime(RequestType requestType, final String key){
        return getCounterParam(requestType,"getCounterExpireTime", key,0, 0);
    }

    public static RequestBean<CounterParam> getParamOfGetCounterValue(RequestType requestType, String key){
        return getCounterParam(requestType,"getCounterValue", key,0, 0);
    }

    public static RequestBean<CounterParam> getParamOfAddCounterValue(RequestType requestType, String key, long value){
        return getCounterParam(requestType,"addCounterValue", key,value, 0);
    }

    /*********************************************************同步锁相关参数******************************************************************/

    private static RequestBean<SyLockParam> getParamOfLock(RequestType requestType,String method,String key,String lockId){
        SyLockParam syLockParam = new SyLockParam();
        syLockParam.setKey(key);
        syLockParam.setMethod(method);
        syLockParam.setLockId(lockId);
        return getParam(requestType,MsgType.LOCK,syLockParam);
    }

    public static RequestBean<SyLockParam> getParamOfGetLock(RequestType requestType,String key){
        return getParamOfLock(requestType,"lock",key,null);
    }

    public static RequestBean<SyLockParam> getParamOfReleaseLock(RequestType requestType,String key,String unLockId){
        return getParamOfLock(requestType,"unLock",key,unLockId);
    }


}
