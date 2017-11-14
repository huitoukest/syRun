package com.tingfeng.syRun.server.handler;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.WriteHelper;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.ex.RelaseLockException;
import com.tingfeng.syRun.server.service.impl.SyLockService;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.syRun.server.util.SignleRunServerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SyRunSeverHandler  extends SimpleChannelInboundHandler<String>{

	private static final ExecutorService servicePool = Executors.newFixedThreadPool(ConfigEntity.getInstance().getServerHandlPoolSize());
	private static Logger logger = LoggerFactory.getLogger(SyRunSeverHandler.class);
	private static final WriteHelper writeHelper = new WriteHelper();

	public static final ThreadLocal<Channel> channels = new ThreadLocal<>();

    public void messageReceived(Channel channel, String message)throws Exception
    {
    	//logger.debug("Server 收到信息: {}: " , message);
    	//*********************************************** 接收数据  
		String str = null;
		String result = null;
		if(null == message){
			ResponseBean responseBean = new ResponseBean();
			responseBean.setStatus(ResponseStatus.FAIL.getValue());
			responseBean.setErrorMsg("null response msg ");
			sendMessage(channel,"NULL",responseBean);
		}else{
			str = message.toString();
			final String reqMsg = str;
			servicePool.submit(() ->{
				ResponseBean responseBean = null;
				try {
					channels.set(channel);
					responseBean = SignleRunServerUtil.doServerWork(reqMsg);
					sendMessage(channel,reqMsg,responseBean);
				}catch (Exception e) {
					if(!(e instanceof RelaseLockException)){
						e.printStackTrace();
						//logger.info("消息发送失败,ip:{},收到消息:{},异常:{}",channelHandlerContext.getRemoteAddress(),reqMsg,e);
						//记录失败信息
						SignleRunServerUtil.dealFailSendWork(reqMsg,responseBean);
					}
				}
			});
		}
    }

	public static void sendMessage(Channel channel,String reqMsg,ResponseBean responseBean){
    	sendMessage(channel,reqMsg,responseBean,0);
	}

    private static void sendMessage(Channel channel,final String reqMsg,final ResponseBean responseBean,final int sendCount){
		//发送n次后,不再重试发送
		final String respMsg = JSONObject.toJSONString(responseBean);
		//WriteFuture writeFuture = null;
		//synchronized (SyRunSeverHandler.class) {
			//channel.writeAndFlush(respMsg);
		writeHelper.write(channel,respMsg);
		//}
		logger.debug("Server ,收到消息:{},发送消息: {}: " , reqMsg,respMsg);
		/*writeFuture.addListener((IoFuture future) -> {
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
		});*/
    }

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) throws Exception {

		//logger.debug("Server 收到信息: {}: " , message);
		//channelHandlerContext.channel().writeAndFlush("Server 收到信息: {}: " + message);
		String[] msgArray = message.split("\\\r\\\n");
		for(String str:msgArray){
			messageReceived(channelHandlerContext.channel(),str);
		}
		//messageReceived(channelHandlerContext.channel(),message);

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,
								Throwable cause) throws Exception {
		logger.error("Unexpected exception from downstream.", cause);
		ctx.close();
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		logger.info("链接断开:{}",ctx);
		SyLockService.removeBlockLock(ctx.channel().id().toString());
	}
}