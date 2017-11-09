package com.tingfeng.syRun.server.handler;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.ex.SendFailException;
import com.tingfeng.syRun.common.util.Base64Util;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.syRun.server.util.SignleRunServerUtil;
import sun.security.provider.certpath.OCSPResponse;

public class SyRunSeverHandler  extends IoHandlerAdapter{

	public static final int threadSize = 1024;
	private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadSize);
	private static Logger logger = LoggerFactory.getLogger(SyRunSeverHandler.class);
	
    @Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
		session.closeOnFlush();
		logger.info("客户端空闲,关闭......" + session.getRemoteAddress());
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
	}

	/**
     * MINA的异常回调方法。
     * <p>
     * 本类中将在异常发生时，立即close当前会话。
     * 
     * @param session 发生异常的会话
     * @param cause 异常内容
     * @see IoSession#close(boolean)
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        logger.error("{}捕获到错了，原因是：{},{}",session.getRemoteAddress(),cause.getMessage(), cause);
		cause.printStackTrace();
        session.closeOnFlush();
    }
         
    /**
     * MINA框架中收到客户端消息的回调方法。
     * <p>
     * 本类将在此方法中实现完整的即时通讯数据交互和处理策略。
     * <p>
     * 为了提升并发性能，本方法将运行在独立于MINA的IoProcessor之外的线程池中，
     * 
     * @param ioSession 收到消息对应的会话引用
     * @param message 收到的MINA的原始消息封装对象，本类中是 {@link IoBuffer}对象
     * @throws Exception 当有错误发生时将抛出异常
     */
    @Override
    public void messageReceived(IoSession ioSession, Object message)throws Exception
    {
    	//System.out.println(System.currentTimeMillis() + ": Message server re: " + message);
    	//*********************************************** 接收数据  
		String str = null;
		String result = null;
		if(null == message){
			ResponseBean responseBean = new ResponseBean();
			responseBean.setStatus(ResponseStatus.FAIL.getValue());
			responseBean.setErrorMsg("null response msg ");
			sendMessage(ioSession,"NULL",responseBean);
		}else{
			str = message.toString();
			final String reqMsg = str;
			servicePool.submit(() ->{
				ResponseBean responseBean = null;
				try {
					responseBean = SignleRunServerUtil.doServerWork(reqMsg);
					sendMessage(ioSession,reqMsg,responseBean);
				}catch (Exception e) {
					logger.info("消息发送失败,ip:{},收到消息:{},异常:{}",ioSession.getRemoteAddress(),reqMsg,e);
					//记录失败信息
					SignleRunServerUtil.dealFailSendWork(reqMsg,responseBean);
				}
			});
		}
    }

	public static void sendMessage(IoSession ioSession,String reqMsg,ResponseBean responseBean){
    	sendMessage(ioSession,reqMsg,responseBean,0);
	}

    private static void sendMessage(final IoSession ioSession,final String reqMsg,final ResponseBean responseBean,final int sendCount){
		//发送n次后,不再重试发送
		final String respMsg = JSONObject.toJSONString(responseBean);
		WriteFuture writeFuture = ioSession.write(respMsg);
		writeFuture.addListener((IoFuture future) -> {
			WriteFuture wfuture=(WriteFuture)future;
			// 写入失败则处理数据
			if(!wfuture.isWritten()){
				if(sendCount > 2){
					logger.info("消息发送失败,ip:{},消息:{},次数{}",ioSession.getRemoteAddress(),reqMsg,sendCount);
					SignleRunServerUtil.dealFailSendWork(reqMsg,responseBean);
				}else{
					try {
						Thread.sleep(ConfigEntity.getInstance().getTimeResendIdle());
					} catch (InterruptedException e) {
						logger.info("sleep fail!",e);
					}
					sendMessage(ioSession,reqMsg,responseBean,sendCount + 1);
				}
			}
		});
    }
}