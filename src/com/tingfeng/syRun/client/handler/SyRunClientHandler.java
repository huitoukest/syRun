package com.tingfeng.syRun.client.handler;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.client.SyRunTCPClient;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.bean.request.RequestBean;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.client.util.SyRunMsgAsynchronizeUtil;
import com.tingfeng.syRun.client.util.SyRunMsgSynchronizeUtil;
import com.tingfeng.syRun.common.util.RequestUtil;
import com.tingfeng.syRun.common.ex.OverRunTimeException;
import com.tingfeng.syRun.server.SyRunTCPServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.concurrent.*;


/**
 * 异步handler
 * @author huitoukest
 */
public class SyRunClientHandler extends SimpleChannelInboundHandler<String> {
	private static Logger logger = LoggerFactory.getLogger(SyRunClientHandler.class);

	public static final int SIZE_CORE_POOL = 1;
	public static final long TIME_KEEP_ALIVE = 60L;

	private static final SyRunClientHandler signleRunClientHandler = new SyRunClientHandler();

	/**
	 * 单独开一个线程池来处理接收的数据,不然因为消息的顺序接收的关系,可能导致io接收线程阻塞,
	 * 导致消息无法收到.
	 */
	//public static final int threadSize = 10;
	private static final ExecutorService serviceReceiveMsgPool = new ThreadPoolExecutor(SIZE_CORE_POOL, Integer.MAX_VALUE,
			TIME_KEEP_ALIVE, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());

	private SyRunClientHandler() {}



	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
		//channelHandlerContext.channel().writeAndFlush(msg);
		//System.out.println(msg);
		receiveMsg(msg);
	}




	public static SyRunClientHandler getSigleInstance(){
		return signleRunClientHandler;
	}
/*
	private static final AtomicInteger  reConnectCount = new AtomicInteger(0);
	
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
	}*/
	
	public static void receiveMsg(String msg){
		logger.debug("Client,收到消息: " + msg);
		ResponseBean responseBean = JSONObject.parseObject(msg,ResponseBean.class);
		receiveMsg(responseBean);
	}

	public static void receiveMsg(ResponseBean responseBean){
		new Thread(()->{
			if(RequestUtil.isAsychronizedMsg(responseBean.getId())) {
				SyRunMsgAsynchronizeUtil.receiveMsg(responseBean);
			}else {
				SyRunMsgSynchronizeUtil.receiveMsg(responseBean);
			}
		}).start();
	}



	public static void sendMessage(RequestBean<?> requestBean){
		try {
			SyRunTCPClient.init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
		} catch (Exception e) {
			logger.error("");
		}
		sendMessage(SyRunTCPClient.getChannel(),requestBean,0);
	}

    public static void sendMessage(Channel channel,RequestBean<?> requestBean){
        sendMessage(channel,requestBean,0);
    }

	/**
	 * 通过多线程/唯一id辨识消息唯一性
	 * @param requestBean
	 * @return
	 * @throws InterruptedException
	 * @throws OverRunTimeException
	 */
	private static void sendMessage(Channel channel , RequestBean<?> requestBean, int hasSendCount){
		//synchronized (SyRunSeverHandler.class) {
		//serviceReceiveMsgPool.submit(()-> {
			final String msg = JSONObject.toJSONString(requestBean);
			logger.debug("Client,发送消息: " + msg);
		  channel.writeAndFlush(msg);
			/*WriteFuture writeFuture = null;
					writeFuture = ioSession.write(msg);
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
						if(reConnectCount.get() < 1 && (null == ioSession || null == ioSession.getServiceAddress())){
							try {
								reConnectCount.incrementAndGet();
								SyRunTCPClient.closeConnect();
								SyRunTCPClient.connectToServer(false);
								logger.info("发送消息失败:server:{},msg:{},count{},开始关闭客户端准备重连...",ioSession.getServiceAddress(),msg,hasSendCount);
							} catch (IOException e) {
								logger.info("发送消息失败:server:{},msg:{},count{},开始关闭客户端准备重连异常{}",ioSession.getServiceAddress(),msg,hasSendCount,e.getStackTrace());
							}finally {
								reConnectCount.decrementAndGet();
							}
						}
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
		});*/
		//}
	}
	
}
