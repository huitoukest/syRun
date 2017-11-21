package com.tingfeng.syRun.server.handler;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.HeartBeatHelper;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.WriteHelper;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.ex.ReleaseLockException;
import com.tingfeng.syRun.server.service.impl.SyLockService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.syRun.server.util.SignleRunServerUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 1.首先跟服务器操作类一样继承ChannelHandlerAdapter类，重写channelRead和channelActive两个方法
 *其中channelActive方法是用来发送客户端信息的，channelRead方法客户端是接收服务器数据的
 *2.先声明一个全局变量firstMessage，用来接收客户端发出去的信息的值
 */
@ChannelHandler.Sharable
public class SyRunSeverHandler  extends SimpleChannelInboundHandler<String>{
	private static final SyRunSeverHandler syRunSeverHandler = new SyRunSeverHandler();
    private static boolean isInit = false;
	private static final ExecutorService servicePool = Executors.newFixedThreadPool(ConfigEntity.getInstance().getServerHandlPoolSize());
	private static Logger logger = LoggerFactory.getLogger(SyRunSeverHandler.class);
	private static final WriteHelper writeHelper = new WriteHelper();
    private static HeartBeatHelper hearBeatHelper = null;

	public static final ThreadLocal<Channel> channels = new ThreadLocal<>();

	private SyRunSeverHandler(){
	}

	public static SyRunSeverHandler getInstance(){
		return syRunSeverHandler;
	}


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
		}else if (HeartBeatHelper.isHeartBeatMessage(message)){
		     sendMessage(channel,message,HeartBeatHelper.getHeartBeatMessage(message));
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
					if(!(e instanceof ReleaseLockException)){
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
    public static void sendMessage(Channel channel,String reqMsg,String respMsg){
        writeHelper.write(channel,respMsg);
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
		try {
			String[] msgArray = message.split("\\\r\\\n");
			for (String str : msgArray) {
				messageReceived(channelHandlerContext.channel(), str);
			}
		}finally {
			ReferenceCountUtil.release(message);
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
		ctx.close();
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                /*case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;*/
                default:
                    break;
            }
        }
	}

    private void handleReaderIdle(ChannelHandlerContext ctx) {
        logger.info("链接空闲,server主动断开:{}",ctx);
        ctx.close();
        SyLockService.removeBlockLock(ctx.channel().id().toString());
    }


}