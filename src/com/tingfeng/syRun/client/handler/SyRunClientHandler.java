package com.tingfeng.syRun.client.handler;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.client.util.SyRunMsgAsynchronizeUtil;
import com.tingfeng.syRun.client.util.SyRunMsgSynchronizeUtil;
import com.tingfeng.syRun.common.util.RequestUtil;
import com.tingfeng.syRun.common.ex.OverRunTimeException;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 异步handler
 * @author huitoukest
 */
public class SyRunClientHandler extends IoHandlerAdapter {

	private static final SyRunClientHandler signleRunClientHandler = new SyRunClientHandler();

	private SyRunClientHandler() {}
	
	public static SyRunClientHandler getSigleInstance(){
		return signleRunClientHandler;
	}
	private static Logger logger = LoggerFactory.getLogger(SyRunClientHandler.class);
	
	//当一个客端端连结进入时
	@Override
	public void sessionOpened(IoSession session) throws Exception {
	}
	//当一个客户端关闭时
	@Override
	public void sessionClosed(IoSession session) {
	}
	//当客户端发送的消息到达时:
	@Override
	public synchronized void messageReceived(IoSession session, Object message)
	throws Exception {
		//我们己设定了服务器解析消息的规则是一行一行读取,这里就可转为String:
		String s=(String)message;
		receiveMsg(s);
	}
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
		if(null != session){
			session.closeNow();
		}
		logger.info("客户端空闲,被关闭......");
	}
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		super.exceptionCaught(session, cause);
	}
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}
	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
	}
	
	public static void receiveMsg(String msg){
			   ResponseBean responseBean = JSONObject.parseObject(msg,ResponseBean.class);
			   if(RequestUtil.isAsychronizedMsg(responseBean.getId())) {
				   SyRunMsgAsynchronizeUtil.receiveMsg(responseBean);
			   }else {
				   SyRunMsgSynchronizeUtil.receiveMsg(responseBean);
			   }
	}
	/**
	 * 目前必须使发送的消息和接收的消息互斥才能保证有序,以后考虑
	 * 通过多线程/唯一id辨识消息唯一性
	 * @param requestBean
	 * @return
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 */
	public static void sendMessage(IoSession ioSession,RequestBean<?> requestBean){
		String msg = JSONObject.toJSONString(requestBean);
		//System.out.println("发送消息: " + msg);
		ioSession.write(msg );
	}
	
}
