package com.tingfeng.syrun.client.util;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syrun.client.SyRunTCPClient;
import com.tingfeng.syrun.common.ConfigEntity;
import com.tingfeng.syrun.common.bean.request.BaseRequestParam;
import com.tingfeng.syrun.common.bean.request.CounterParam;
import com.tingfeng.syrun.common.bean.request.RequestBean;
import com.tingfeng.syrun.common.bean.request.SyLockParam;
import com.tingfeng.syrun.common.bean.response.ResponseBean;
import com.tingfeng.syrun.common.FrequencyControlHelper;
import com.tingfeng.syrun.common.RequestType;
import com.tingfeng.syrun.common.ResponseStatus;
import com.tingfeng.syrun.common.ex.OverRunTimeException;
import com.tingfeng.syrun.common.util.StringUtil;
import com.tingfeng.syrun.server.SyRunTCPServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyRunClientUtil {

	private static Logger logger = LoggerFactory.getLogger(SyRunClientUtil.class);
	
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
				float seed = (float)(tempTime + sleepTime);
				tempTime = (long) Math.round(seed);
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
	 * @return see the com.tingfeng.syrun.common.ResponseStatus value
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static int initCounter(final String key,final long value,final long expireTime) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfInitCounter(RequestType.SY,key,value,expireTime);
		ResponseBean responseBean = sendMsgToServer(requestBean);
    	return responseBean.getStatus();
    }

	/**
	 *
	 * @param key
	 * @param value
	 * @return see the  com.tingfeng.syrun.common.ResponseStatus value
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static  int setCounterValue(final String key,final long value) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfSetCounterValue(RequestType.SY,key,value);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return responseBean.getStatus();
    }

	/**
	 *
	 * @param key
	 * @param expireTime
	 * @return see the  com.tingfeng.syrun.common.ResponseStatus value
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static int setCounterExpireTime(final String key,final long expireTime) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfSetCounterExpireTime(RequestType.SY,key,expireTime);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return responseBean.getStatus();
    }
    
    public static long getCounterExpireTime(final String key) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfGetCounterExpireTime(RequestType.SY,key);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return Long.parseLong(responseBean.getData());
    }

    
    public static long getCounterValue(String key) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfGetCounterValue(RequestType.SY,key);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return Long.parseLong(responseBean.getData());
    }
    /**
     * 
     * @param key
	 * @param value
     * @return return the value
     * @throws IOException
     * @throws InterruptedException
     * @throws OverRunTimeException
     */
    public static long addCounterValue(String key,long value) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfAddCounterValue(RequestType.SY,key,value);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return getLong(responseBean,logger);
	}

    /**************************************** 异步的counter方法,名称和同步的方法名相同,增加一个回调参数,无返回值*************************************************************/

	/**
	 *
	 * @param key
	 * @param value
	 * @param expireTime
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static void initCounter(final String key,final long value,final long expireTime,MsgHandler msgHandler) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfInitCounter(RequestType.ASY,key,value,expireTime);
		sendMsgToServer(requestBean,msgHandler);
	}

	/**
	 *
	 * @param key
	 * @param value
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static  void setCounterValue(final String key,final long value,MsgHandler msgHandler) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfSetCounterValue(RequestType.ASY,key,value);
		sendMsgToServer(requestBean,msgHandler);
	}

	/**
	 *
	 * @param key
	 * @param expireTime
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static void setCounterExpireTime(final String key,final long expireTime,MsgHandler msgHandler) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfSetCounterExpireTime(RequestType.ASY,key,expireTime);
		sendMsgToServer(requestBean,msgHandler);
	}

	/**
	 *
	 * @param key
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static void getCounterExpireTime(final String key,MsgHandler msgHandler) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfGetCounterExpireTime(RequestType.ASY,key);
		sendMsgToServer(requestBean,msgHandler);
	}

	/**
	 *
	 * @param key
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static void getCounterValue(String key,MsgHandler msgHandler) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException{
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfGetCounterValue(RequestType.ASY,key);
		sendMsgToServer(requestBean,msgHandler);
	}
	/**
	 *
	 * @param key
	 * @param value
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 */
	public static void addCounterValue(String key,long value,MsgHandler msgHandler) throws IOException, InterruptedException, OverRunTimeException, TimeoutException, ExecutionException {
		RequestBean<CounterParam> requestBean =  RequestParameterUtil.getParamOfAddCounterValue(RequestType.ASY,key,value);
		sendMsgToServer(requestBean,msgHandler);
	}
    
    /*************************************************计数器相关********************************************************************/
    
    /*************************************************同步锁相关 ********************************************************************/
	/**
	 * if fial throw exception
	 * @param key
	 * @return lockId
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static String getLock(String key) throws InterruptedException, IOException, OverRunTimeException, TimeoutException, ExecutionException {
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
	 * @return see the com.tingfeng.syrun.common.CodeConstants.ResponseStatus
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
    public static int releaseLock(String key,String lockId) throws InterruptedException, IOException, OverRunTimeException, TimeoutException, ExecutionException {
		RequestBean<SyLockParam> requestBean = RequestParameterUtil.getParamOfReleaseLock(RequestType.SY,key,lockId);
		ResponseBean responseBean = sendMsgToServer(requestBean);
		return responseBean.getStatus();
    }

	/**
	 * if fial throw exception
	 * @param key
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static void getLock(String key,MsgHandler msgHandler) throws InterruptedException, IOException, OverRunTimeException, TimeoutException, ExecutionException {
		RequestBean<SyLockParam> requestBean = RequestParameterUtil.getParamOfGetLock(RequestType.ASY,key);
		sendMsgToServer(requestBean,msgHandler);
	}

	/**
	 *
	 * @param key
	 * @param lockId
	 * @param msgHandler handle server msg , msg is com.tingfeng.syrun.common.bean.response.ResponseBean
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	public static void releaseLock(String key,String lockId,MsgHandler msgHandler) throws InterruptedException, IOException, OverRunTimeException, TimeoutException, ExecutionException {
		RequestBean<SyLockParam> requestBean = RequestParameterUtil.getParamOfReleaseLock(RequestType.ASY,key,lockId);
		sendMsgToServer(requestBean,msgHandler);
	}
    
    
    /*************************************************同步锁相关********************************************************************/



	/**
	 * 统一的同步消息发送
	 * @param requestBean
	 * @param <T>
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static <T extends BaseRequestParam> ResponseBean sendMsgToServer(RequestBean<T> requestBean) throws InterruptedException, IOException, OverRunTimeException, TimeoutException, ExecutionException {
		//SyRunTCPClient.init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
		return SyRunMsgSynchronizeUtil.sendMsg(requestBean);
	}

	/**
	 * 统一的异步消息发送
	 * @param requestBean
	 * @param <T>
	 * @param  msgHandler 异步的消息回调类
	 * @return
	 * @throws InterruptedException
	 * @throws IOException
	 * @throws OverRunTimeException
	 * @throws TimeoutException
	 * @throws ExecutionException
	 */
	private static <T extends BaseRequestParam> void sendMsgToServer(RequestBean<T> requestBean,MsgHandler msgHandler) throws InterruptedException, IOException, OverRunTimeException, TimeoutException, ExecutionException {
		//SyRunTCPClient.init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
		SyRunMsgAsynchronizeUtil.sendMsg(requestBean,msgHandler);
	}

	public static long getLong(ResponseBean responseBean, Logger logger) throws NumberFormatException{
		long result = 0;
		try{
			result = Long.parseLong(responseBean.getData());
		}catch (Exception e){
			logger.error("解析结果出错.",e);
			throw e;
		}
		return result;
	}

}
