package com.tingfeng.syRun.client.util;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.client.handler.SyRunClientHandler;
import com.tingfeng.syRun.common.bean.request.BaseRequestParam;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.client.SyRunTCPClient;
import com.tingfeng.syRun.common.util.Base64Util;
import com.tingfeng.syRun.common.util.RequestUtil;
import org.apache.mina.core.session.IoSession;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.*;

/**
 *  同步handler,
 *  同步的消息id默认以sy_开头
 * @author huitoukest
 */
public class SyRunMsgSynchronizeUtil {
	/**
	 * 初始的消息和线程池计数器Map大小
	 */
	public static final int DATA_INIT_SIZE = 50000;

	/**
	 * 消息池
	 */
    private static final ConcurrentHashMap<String,ResponseBean> msgResPonseMap = new ConcurrentHashMap<>(DATA_INIT_SIZE);
	private static final ConcurrentHashMap<String,CountDownLatch> countDownLatchMap = new ConcurrentHashMap<>(DATA_INIT_SIZE);

	/*private static final ExecutorService services = Executors.newFixedThreadPool(2);
	//指定方法完成的执行器
	private static final ExecutorCompletionService<ResponseBean> completionServices = new ExecutorCompletionService<>(services);*/
	CountDownLatch countDownLatch = new CountDownLatch(1);

	/**
	 * 收到一条消息后,将此消息通过此处处理
	 * @param responseBean
	 */
	public static void receiveMsg(ResponseBean responseBean){
		   String id = responseBean.getId();
		   if(RequestUtil.isSychronizedMsg(id)) {
			   msgResPonseMap.put(id, responseBean);
			   CountDownLatch countDownLatch = countDownLatchMap.get(id);
			   if (null != countDownLatch) {
				   countDownLatch.countDown();
				   countDownLatchMap.remove(id);
			   }
		   }
	}

    /**
     * 同步的消息发送,效率低下
     * @param ioSession
     * @param requestBean
     * @return
     */
	public static ResponseBean sendMsg(IoSession ioSession,RequestBean<?> requestBean) throws TimeoutException, ExecutionException, InterruptedException, UnsupportedEncodingException {
		final String id = requestBean.getId();
		msgResPonseMap.remove(id);
		CountDownLatch countDownLatch = new CountDownLatch(1);
		countDownLatchMap.put(id,countDownLatch);

		SyRunClientHandler.sendMessage(ioSession,requestBean);
		countDownLatch.await();
		ResponseBean responseBean = msgResPonseMap.get(id);
		return responseBean;
    }

    /**
     * 同步的消息发送,效率低下
     * @param requestBean
     * @return
     */
    public static <T extends BaseRequestParam> ResponseBean sendMsg(RequestBean<?> requestBean) throws TimeoutException, ExecutionException, InterruptedException, UnsupportedEncodingException {
        return sendMsg(SyRunTCPClient.getSession(),requestBean);
    }
	
}