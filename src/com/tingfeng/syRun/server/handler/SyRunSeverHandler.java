package com.tingfeng.syrun.server.handler;


import com.tingfeng.syrun.server.service.impl.SyLockService;
import com.tingfeng.syrun.server.util.ServerHandleUtil;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	private static Logger logger = LoggerFactory.getLogger(SyRunSeverHandler.class);

	private SyRunSeverHandler(){
	}

	public static SyRunSeverHandler getInstance(){
		return syRunSeverHandler;
	}



	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, String message) throws Exception {
		//logger.debug("Server 收到信息: {}: " , message);
		try {
			ServerHandleUtil.receiveMsg(channelHandlerContext.channel(),message,true);
		}finally {
			ReferenceCountUtil.release(message);
		}
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