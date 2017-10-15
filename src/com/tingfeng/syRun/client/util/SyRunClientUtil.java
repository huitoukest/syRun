package com.tingfeng.syRun.client.util;

import java.io.IOException;
import com.tingfeng.syRun.bean.CounterParam;
import com.tingfeng.syRun.bean.RequestBean;
import com.tingfeng.syRun.bean.SyLockParam;
import com.tingfeng.syRun.client.SyRunLockTCPClient;
import com.tingfeng.syRun.client.handler.SyRunLockClientHandler;
import com.tingfeng.syRun.common.CodeConstants;
import com.tingfeng.syRun.common.FrequencyControlHelper;
import com.tingfeng.syRun.common.ex.OutTimeException;
import com.tingfeng.syRun.common.util.IdWorker;

public class SyRunClientUtil {
	
	/**
	 * 通过计数器模拟的同步执行方法
	 * 对指定key做跨服务器的单步单线程任务
	 * 保证执行的事务性,提供并发执行功能
	 * 可以减少数据库事务冲突,提供数据库性能.
	 * @param signWork 传入同步执行的数据信息
	 * @param key 传入同步执行的key
	 * @param frequencyBean 传入同步执行中,并发执行的数量等信息
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
	
    public static String initCounter(final String key,final long value,final Long expireTime) throws IOException, InterruptedException, OutTimeException{
    	CounterParam counterRequest= new CounterParam("initCounter",value, key, expireTime);
    	String result = sendMsgToServer(counterRequest);
    	return result;
    }
    
    public static  String setCounterValue(final String key,final long value) throws IOException, InterruptedException, OutTimeException{
    	CounterParam counterRequest= new CounterParam("setCounterValue",value, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return result;
    }
    
    public static String setCounterExpireTime(final String key,final long expireTime) throws IOException, InterruptedException, OutTimeException{
    	CounterParam counterRequest= new CounterParam("setCounterExpireTime",0, key, expireTime);
    	String result = sendMsgToServer(counterRequest);
    	return result;
    }
    
    public static long getCounterExpireTime(final String key) throws IOException, InterruptedException, OutTimeException{
    	CounterParam counterRequest= new CounterParam("getCounterExpireTime",0, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return Long.valueOf(result);
    }

    
    public static long getCounterValue(String key) throws IOException, InterruptedException, OutTimeException{
    	CounterParam counterRequest= new CounterParam("getCounterValue",0, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return Long.valueOf(result);
    }
    /**
     * 
     * @param key
     * @param addValue if null ,convert to 0
     * @param request
     * @param response
     * @return return the value
     * @throws IOException
     * @throws InterruptedException 
     * @throws OutTimeException 
     */
    public static long addCounterValue(String key,long value) throws IOException, InterruptedException, OutTimeException{ 	
    	CounterParam counterRequest= new CounterParam("addCounterValue",value, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return Long.valueOf(result);
    }
    private static String sendMsgToServer(CounterParam counterRequest) throws InterruptedException, IOException, OutTimeException{
    	RequestBean<CounterParam>  requestBean = new RequestBean<>();
    	requestBean.setType(CodeConstants.RquestType.COUNTER);
    	requestBean.setParams(counterRequest);
    	long id = IdWorker.getUUID();
    	requestBean.setId(String.valueOf(id));
    	return sendCounterMsgToServer(requestBean);
    }
    
    /*************************************************计数器相关********************************************************************/
    
    /*************************************************同步锁相关
     * @throws OutTimeException 
     * @throws IOException 
     * @throws InterruptedException ********************************************************************/
    
    public static String lockSyLock(String key) throws InterruptedException, IOException, OutTimeException {
    	RequestBean<SyLockParam>  requestBean = new RequestBean<SyLockParam>();
    	requestBean.setType(CodeConstants.RquestType.SYNLOCK);
    	long id = IdWorker.getUUID();
    	requestBean.setId(String.valueOf(id));    	
    	SyLockParam syLockParam = new SyLockParam();
    	syLockParam.setKey(key);
    	syLockParam.setMethod("lockSyLock");
    	requestBean.setParams(syLockParam);
    	return sendSyLockMsgToServer(requestBean);
    	
    }
    
    public static String unlockSyLock(String key,String lockId) throws InterruptedException, IOException, OutTimeException {
    	RequestBean<SyLockParam>  requestBean = new RequestBean<SyLockParam>();
    	requestBean.setType(CodeConstants.RquestType.SYNLOCK);
    	long id = IdWorker.getUUID();
    	requestBean.setId(String.valueOf(id));    	
    	SyLockParam syLockParam = new SyLockParam();
    	syLockParam.setKey(key);
    	syLockParam.setLockId(lockId);
    	syLockParam.setMethod("unlockSyLock");
    	requestBean.setParams(syLockParam);
    	return sendSyLockMsgToServer(requestBean);
    }
    
    
    /*************************************************同步锁相关********************************************************************/
    
    private static String sendCounterMsgToServer(RequestBean<CounterParam>  requestBean) throws InterruptedException, IOException, OutTimeException{
    	new SyRunLockTCPClient().initClientConnect();
    	return SyRunLockClientHandler.sendMessage(requestBean);
    }
    
    private static String sendSyLockMsgToServer(RequestBean<SyLockParam>  requestBean) throws InterruptedException, IOException, OutTimeException{
    	new SyRunLockTCPClient().initClientConnect();
    	return SyRunLockClientHandler.sendMessage(requestBean);
    }
}
