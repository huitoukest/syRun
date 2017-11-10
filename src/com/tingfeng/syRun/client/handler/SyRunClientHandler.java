package com.tingfeng.syRun.client.handler;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.client.util.SyRunMsgAsynchronizeUtil;
import com.tingfeng.syRun.client.util.SyRunMsgSynchronizeUtil;
import com.tingfeng.syRun.common.util.RequestUtil;
import com.tingfeng.syRun.common.ex.OverRunTimeException;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.WriteFuture;
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

/*	public static final int threadSize = 512;
	private static final ExecutorService serviceReceiveMsgPool = Executors.newFixedThreadPool(threadSize);*/

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
		//super.exceptionCaught(session, cause);
		if(null != session){
			logger.info("捕获到异常,客户端信息:{},异常信息:{}",session.getRemoteAddress(),cause.getCause());
			session.closeNow();
		}else{
			logger.info("捕获到异常异常信息:{}",cause.getCause());
		}
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
		receiveMsg(responseBean);
	}

	public static void receiveMsg(ResponseBean responseBean){
		//serviceReceiveMsgPool.submit(()->{
			if(RequestUtil.isAsychronizedMsg(responseBean.getId())) {
				SyRunMsgAsynchronizeUtil.receiveMsg(responseBean);
			}else {
				SyRunMsgSynchronizeUtil.receiveMsg(responseBean);
			}
		//});
	}


    public static void sendMessage(IoSession ioSession,RequestBean<?> requestBean){
        sendMessage(ioSession,requestBean,0);
    }

	/**
	 * 通过多线程/唯一id辨识消息唯一性
	 * @param requestBean
	 * @return
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 */
	private static void sendMessage(IoSession ioSession,RequestBean<?> requestBean,int hasSendCount){
        final String msg = JSONObject.toJSONString(requestBean);
		//System.out.println("发送消息: " + msg);
		WriteFuture writeFuture = null;
		//synchronized (SyRunSeverHandler.class) {
			writeFuture = ioSession.write(msg);
		//}
		writeFuture.addListener((IoFuture future) -> {
				WriteFuture wfuture=(WriteFuture)future;
				// 写入失败则处理数据
				if(!wfuture.isWritten()){
                    if(hasSendCount > 2){
                        ResponseBean responseBean = new  ResponseBean();
                        responseBean.setId(requestBean.getId());
                        responseBean.setErrorMsg("send failed");
                        responseBean.setStatus(ResponseStatus.CUSTOM.getValue());
                        logger.info("发送消息失败:server:{},msg:{},count{}",ioSession.getServiceAddress(),msg,hasSendCount);
                        receiveMsg(responseBean);
                    }else{
                        try {
                            Thread.sleep(ConfigEntity.getInstance().getTimeResendIdle());
                        } catch (InterruptedException e) {
                            logger.info("sleep fail!",e);
                        }
                        sendMessage(ioSession,requestBean,hasSendCount + 1);
                    }
				}
		});
	}
	
}
