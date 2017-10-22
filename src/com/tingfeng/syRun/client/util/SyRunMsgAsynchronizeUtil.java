package com.tingfeng.syRun.client.util;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.client.handler.SyRunClientHandler;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.client.SyRunTCPClient;
import com.tingfeng.syRun.common.util.Base64Util;
import com.tingfeng.syRun.common.util.RequestUtil;
import org.apache.mina.core.session.IoSession;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 异步handler,
 * 异步的消息id默认以asy_开头.
 * @author huitoukest
 */
public class SyRunMsgAsynchronizeUtil {

	private static final ConcurrentHashMap<String,MsgHandler<?>> msgHandlerMap = new ConcurrentHashMap<>(20000);

	public static void sendMsg(RequestBean<?> requestBean,MsgHandler<?> msgHandler) throws UnsupportedEncodingException {
		    sendMsg(SyRunTCPClient.getSession(),requestBean,msgHandler);
	}

	/**
	 * 消息的异步发送,通过msgHandler回调处理消息
	 * @param ioSession
	 * @param requestBean
	 * @param msgHandler
	 */
	public static void sendMsg(IoSession ioSession,RequestBean<?> requestBean,MsgHandler<?> msgHandler) throws UnsupportedEncodingException {
		   msgHandlerMap.put(requestBean.getId(),msgHandler);
		   SyRunClientHandler.sendMessage(ioSession,requestBean);
	}

	/**
	 * 收到一条消息后,将此消息通过此处处理
	 * @param responseBean
	 */
	public static void receiveMsg(ResponseBean responseBean){
		   String id = responseBean.getId();
		   if(RequestUtil.isAsychronizedMsg(id)) {
			   MsgHandler<?> msgHandler = msgHandlerMap.get(id);
			   try {
				   msgHandler.handMsg(responseBean);
			   } finally {
				   msgHandlerMap.remove(id);
			   }
		   }
	}
	
}
