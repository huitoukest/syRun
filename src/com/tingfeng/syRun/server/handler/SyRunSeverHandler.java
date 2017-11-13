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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.syRun.server.util.SignleRunServerUtil;
import sun.security.provider.certpath.OCSPResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SyRunSeverHandler  extends SimpleChannelInboundHandler<String>{

	public static final int threadSize = 512;
	private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadSize);
	private static Logger logger = LoggerFactory.getLogger(SyRunSeverHandler.class);
	

         

    public void messageReceived(ChannelHandlerContext channelHandlerContext, Object message)throws Exception
    {
    	logger.debug("Server 收到信息: {}: " , message);
    	//*********************************************** 接收数据  
		String str = null;
		String result = null;
		if(null == message){
			ResponseBean responseBean = new ResponseBean();
			responseBean.setStatus(ResponseStatus.FAIL.getValue());
			responseBean.setErrorMsg("null response msg ");
			sendMessage(channelHandlerContext,"NULL",responseBean);
		}else{
			str = message.toString();
			final String reqMsg = str;
			servicePool.submit(() ->{
				ResponseBean responseBean = null;
				try {
					responseBean = SignleRunServerUtil.doServerWork(reqMsg);
					sendMessage(channelHandlerContext,reqMsg,responseBean);
				}catch (Exception e) {
					e.printStackTrace();
					//logger.info("消息发送失败,ip:{},收到消息:{},异常:{}",channelHandlerContext.getRemoteAddress(),reqMsg,e);
					//记录失败信息
					SignleRunServerUtil.dealFailSendWork(reqMsg,responseBean);
				}
			});
		}
    }

	public static void sendMessage(ChannelHandlerContext channelHandlerContext,String reqMsg,ResponseBean responseBean){
    	sendMessage(channelHandlerContext,reqMsg,responseBean,0);
	}

    private static void sendMessage(ChannelHandlerContext channelHandlerContext,final String reqMsg,final ResponseBean responseBean,final int sendCount){
		//发送n次后,不再重试发送
		final String respMsg = JSONObject.toJSONString(responseBean);
		//WriteFuture writeFuture = null;
		//synchronized (SyRunSeverHandler.class) {
			channelHandlerContext.channel().writeAndFlush(reqMsg);
		//}
		logger.debug("Server 发送消息: {}: " , respMsg);
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

		logger.debug("Server 收到信息: {}: " , message);
		channelHandlerContext.channel().writeAndFlush("Server 收到信息: {}: " + message);
		//messageReceived(channelHandlerContext,channelHandlerContext);
		//*********************************************** 接收数据
		/*String str = null;
		String result = null;
		if(null == message){
			ResponseBean responseBean = new ResponseBean();
			responseBean.setStatus(ResponseStatus.FAIL.getValue());
			responseBean.setErrorMsg("null response msg ");
			sendMessage(channelHandlerContext,"NULL",responseBean);
		}else{
			str = message.toString();
			final String reqMsg = str;
			servicePool.submit(() ->{
				ResponseBean responseBean = null;
				try {
					responseBean = SignleRunServerUtil.doServerWork(reqMsg);
					sendMessage(channelHandlerContext,reqMsg,responseBean);
				}catch (Exception e) {
					logger.info("消息发送失败,ip:{},收到消息:{},异常:{}",channelHandlerContext.channel().id(),reqMsg,e);
					//记录失败信息
					SignleRunServerUtil.dealFailSendWork(reqMsg,responseBean);
				}
			});
		}*/
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,
								Throwable cause) throws Exception {
		logger.warn("Unexpected exception from downstream.", cause);
		ctx.close();
	}
}