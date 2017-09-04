package com.tingfeng.signleRun.util;

import java.io.IOException;
import com.alibaba.fastjson.JSONObject;
import com.tingfeng.signleRun.bean.CounterRequest;
import com.tingfeng.signleRun.bean.FrequencyBean;
import com.tingfeng.signleRun.common.CodeConstants;
import com.tingfeng.signleRun.common.FrequencyControlHelper;
import com.tingfeng.signleRun.controller.SignleRunController;
import com.tingfeng.signleRun.controller.SignleRunTCPClient;

import handler.SignleRunClientHandler;

public class SignleRunClientUtil {
	public static SignleRunController signleRunController = new SignleRunController();
	
	/**
	 * 对redis中指定key做跨服务器的单步单线程任务
	 * 但是无法完全保证执行的事务性,但是可以大幅度减少并发,最后由数据库事务校验数据一致性即可.
	 * 可以减少数据库事务冲突,提供数据库性能.
	 * @param redisWork
	 * @param redisTemplate
	 * @param key
	 * @param frequencyBean
	 * @parma sleepTime 单步执行每次的等待时间
	 * @return
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static <T> T doSingeStepWork(final SignleStepWork<T> redisWork,String key,FrequencyBean frequencyBean,long sleepTime) throws IOException, InterruptedException{
		FrequencyControlHelper fc = new FrequencyControlHelper(frequencyBean);
		T t  = null;
		int i = 1;
		long sleepTimeAdd = 0;
		long tempTime = 0;
		while(i-- > 0){
			try {
				tempTime = System.currentTimeMillis();
				fc.startDoWork(new SingeStepException(),key);
				t  = redisWork.doWork(frequencyBean);
			}catch (SingeStepException e) {
				tempTime = System.currentTimeMillis() - tempTime;
				long consurrency = e.getConcurrency();
				//System.out.println("consurrencyCount:" + consurrency);
				if(consurrency > 0){
					sleepTimeAdd = ( consurrency - 1 ) * tempTime; 
				}			
				i++;
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				fc.endDoWork(key);//释放数器
				if(i > 0){//如果单步异常,那么随眠等待
					try {
						Thread.sleep(sleepTime + sleepTimeAdd);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
		return t;
	}
	
    public static String initCounter(final String key,final long value,final Long expireTime) throws IOException, InterruptedException{
    	CounterRequest counterRequest= new CounterRequest("initCounter",value, key, expireTime);
    	String result = sendMsgToServer(counterRequest);
    	return result;
    }
    
    public static  String setCounterValue(final String key,final long value) throws IOException, InterruptedException{
    	CounterRequest counterRequest= new CounterRequest("setCounterValue",value, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return result;
    }
    
    public static String setCounterExpireTime(final String key,final long expireTime) throws IOException, InterruptedException{
    	CounterRequest counterRequest= new CounterRequest("setCounterExpireTime",0, key, expireTime);
    	String result = sendMsgToServer(counterRequest);
    	return result;
    }
    
    public static long getCounterExpireTime(final String key) throws IOException, InterruptedException{
    	CounterRequest counterRequest= new CounterRequest("getCounterExpireTime",0, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return Long.valueOf(result);
    }

    
    public static long getCounterValue(String key) throws IOException, InterruptedException{
    	CounterRequest counterRequest= new CounterRequest("getCounterValue",0, key, 0);
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
     */
    public static long addCounterValue(String key,long value) throws IOException, InterruptedException{ 	
    	CounterRequest counterRequest= new CounterRequest("addCounterValue",value, key, 0);
    	String result = sendMsgToServer(counterRequest);
    	return Long.valueOf(result);
    }
    
    public static String sendMsgToServer(CounterRequest counterRequest) throws InterruptedException, IOException{
    	SignleRunTCPClient.init();
    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put(CodeConstants.RquestKey.TYPE,CodeConstants.RquestType.COUNTER);
    	jsonObject.put(CodeConstants.RquestKey.PARAMS,counterRequest);
    	//IoSession session = SignleRunTCPClient.getSession();
    	String msg = jsonObject.toJSONString();
    	return SignleRunClientHandler.sendMessage(msg);
    }
    
}
