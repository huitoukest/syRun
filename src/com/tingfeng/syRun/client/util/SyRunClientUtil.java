package com.tingfeng.syRun.client.util;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.client.SyRunTCPClient;
import com.tingfeng.syRun.common.bean.request.BaseRequestParam;
import com.tingfeng.syRun.common.bean.request.CounterParam;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.request.SyLockParam;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.FrequencyControlHelper;
import com.tingfeng.syRun.common.RequestType;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.ex.OutTimeException;

public class SyRunClientUtil {
	
	/**
	 * 通过计数器模拟的同步执行方法
	 * 对指定key做跨服务器的单步单线程任务
	 * 保证执行的事务性,提供并发执行功能
	 * 可以减少数据库事务冲突,提供数据库性能.
	 * @param key 传入同步执行的key
	 * @param fc 传入同步执行中,并发执行的数量等信息
	 * @param sleepTime 每次并发冲突后检查的缓冲时间,此时间和计数器执行之间之和为实际检查的时间间隔,推荐数值5
	 * @return
	 * @throws Exception 
	 */
	public static <T> T doSingeStepWorkByCounter(final FrequencyControlHelper<T> fc,String key,long sleepTime) throws Exception{
		T t  = null;
		int i = 1;
		long tempTime = 0;
		while(i-- > 0){
			try {
				tempTime = System.currentTimeMillis();
				fc.startDoWork(new SingeStepException(),key);
				t  = fc.doWork();
			}catch (SingeStepException e) {
				tempTime = System.currentTimeMillis() - tempTime;
				tempTime = Math.round(tempTime + sleepTime);
				i++;
			} catch (Exception e) {
				e.printStackTrace();
			}finally {
				fc.endDoWork(key);//释放数器
				if(i > 0){//如果单步异常,那么随眠等待
					try {
						Thread.sleep(tempTime);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return t;
	}
	/*************************************************计数器相关********************************************************************/
	/**
	 *
	 * @param key
	 * @param value
	 * @param expireTime
	 * @return see the com.tingfeng.syRun.common.ResponseStatus value
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OutTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static int initCounter(final String key,final long value,final long expireTime) throws IOException, InterruptedException, OutTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfInitCounter(RequestType.SY,key,value,expireTime);
		ResponseBean responseBean = sendMsgToServer(requestBean);
    	return responseBean.getStatus();
    }

	/**
	 *
	 * @param key
	 * @param value
	 * @return see the  com.tingfeng.syRun.common.ResponseStatus value
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OutTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static  int setCounterValue(final String key,final long value) throws IOException, InterruptedException, OutTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfSetCounterValue(RequestType.SY,key,value);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return responseBean.getStatus();
    }

	/**
	 *
	 * @param key
	 * @param expireTime
	 * @return see the  com.tingfeng.syRun.common.ResponseStatus value
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OutTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static int setCounterExpireTime(final String key,final long expireTime) throws IOException, InterruptedException, OutTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfSetCounterExpireTime(RequestType.SY,key,expireTime);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return responseBean.getStatus();
    }
    
    public static long getCounterExpireTime(final String key) throws IOException, InterruptedException, OutTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfGetCounterExpireTime(RequestType.SY,key);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return Long.valueOf(responseBean.getData());
    }

    
    public static long getCounterValue(String key) throws IOException, InterruptedException, OutTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfGetCounterValue(RequestType.SY,key);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return Long.valueOf(responseBean.getData());
    }
    /**
     * 
     * @param key
	 * @param value
     * @return return the value
     * @throws IOException
     * @throws InterruptedException
     * @throws OutTimeException 
     */
    public static long addCounterValue(String key,long value) throws IOException, InterruptedException, OutTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfAddCounterValue(RequestType.SY,key,value);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return Long.valueOf(responseBean.getData());
    }
    
    /*************************************************计数器相关********************************************************************/
    
    /*************************************************同步锁相关 ********************************************************************/
	/**
	 * if fial throw exception
	 * @param key
	 * @return lockId
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OutTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static String getLock(String key) throws InterruptedException, IOException, OutTimeException, TimeoutException, ExecutionException {
		RequestBean<SyLockParam> requestBean = RequestParameterUtil.getParamOfGetLock(RequestType.SY,key);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		if(ResponseStatus.SUCCESS.getValue() == responseBean.getStatus()){
			return responseBean.getData();
		}
    	throw  new RuntimeException(JSONObject.toJSONString(requestBean));
    }

	/**
	 *
	 * @param key
	 * @param lockId
	 * @return see the com.tingfeng.syRun.common.CodeConstants.ResponseStatus
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OutTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static int releaseLock(String key,String lockId) throws InterruptedException, IOException, OutTimeException, TimeoutException, ExecutionException {
		RequestBean<SyLockParam> requestBean = RequestParameterUtil.getParamOfReleaseLock(RequestType.SY,key,lockId);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return responseBean.getStatus();
    }
    
    
    /*************************************************同步锁相关********************************************************************/



	/**
	 * 统一的同步消息发送
	 * @param requestBean
	 * @param <T>
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OutTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static <T extends BaseRequestParam> ResponseBean sendMsgToServer(RequestBean<T> requestBean) throws InterruptedException, IOException, OutTimeException, TimeoutException, ExecutionException {
		SyRunTCPClient.init();
		return SyRunMsgSynchronizeUtil.sendMsg(requestBean);
	}
}
