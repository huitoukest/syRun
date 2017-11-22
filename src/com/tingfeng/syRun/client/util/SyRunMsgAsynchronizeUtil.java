package com.tingfeng.syRun.client.util;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.client.handler.SyRunClientHandler;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.client.SyRunTCPClient;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步handler,
 * 异步的消息id默认以asy_开头.
 * @author huitoukest
 */
public class SyRunMsgAsynchronizeUtil {

	private static Logger logger = LoggerFactory.getLogger(SyRunMsgAsynchronizeUtil.class);
	public static final int MSG_POOL_SIZE = 20000;

	private static final ConcurrentHashMap<String,MsgHandler> msgHandlerMap = new ConcurrentHashMap<>(MSG_POOL_SIZE);

	public static void sendMsg(RequestBean<?> requestBean,MsgHandler msgHandler) throws UnsupportedEncodingException {
		    sendMsg(SyRunTCPClient.getChannel(),requestBean,msgHandler);
	}

	/**
	 * 消息的异步发送,通过msgHandler回调处理消息
	 * @param requestBean
	 * @param msgHandler
	 */
	public static void sendMsg(Channel channel,RequestBean<?> requestBean, MsgHandler msgHandler) throws UnsupportedEncodingException {
		   msgHandlerMap.put(requestBean.getId(),msgHandler);
		   ClientHandlerUtil.sendMessage(channel,requestBean);
	}

	/**
	 * 收到一条消息后,将此消息通过此处处理
	 * @param responseBean
	 */
	public static void receiveMsg(ResponseBean responseBean){
		   String id = responseBean.getId();
		   MsgHandler msgHandler = msgHandlerMap.get(id);
		   if(null == msgHandler){
			logger.warn("receive a msg which has no msgHanler !,response is {}", JSONObject.toJSON(responseBean));
		   }else{
			   try {
				   msgHandler.handMsg(responseBean);
			   } finally {
				   msgHandlerMap.remove(id);
			   }
		   }
	}
	
}
