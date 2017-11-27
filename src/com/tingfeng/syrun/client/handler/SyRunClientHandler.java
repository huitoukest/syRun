package com.tingfeng.syrun.client.handler;

import com.tingfeng.syrun.client.util.ClientHandlerUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;


/**
 * 异步handler
 * @author huitoukest
 */
@ChannelHandler.Sharable
public class SyRunClientHandler extends SimpleChannelInboundHandler<String> {
	private static Logger logger = LoggerFactory.getLogger(SyRunClientHandler.class);
	private static boolean isInit = false;
	public static final int SIZE_CORE_POOL = 1;
	public static final long TIME_KEEP_ALIVE = 60L;

	private static final SyRunClientHandler signleRunClientHandler = new SyRunClientHandler();

	//private static final WriteHelper writeHelper = new WriteHelper();
	//private static HeartBeatHelper hearBeatHelper = null;

	/**
	 * 单独开一个线程池来处理接收的数据,不然因为消息的顺序接收的关系,可能导致io接收线程阻塞,
	 * 导致消息无法收到.
	 */
	//public static final int threadSize = 10;
	private static final ExecutorService serviceReceiveMsgPool = new ThreadPoolExecutor(SIZE_CORE_POOL, Integer.MAX_VALUE,
			TIME_KEEP_ALIVE, TimeUnit.SECONDS,
			new SynchronousQueue<Runnable>());

	private SyRunClientHandler() {}

	public static SyRunClientHandler getSigleInstance(){
		return signleRunClientHandler;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, String msg) throws Exception {
		try {
			ClientHandlerUtil.receiveMsg(msg);
		}finally {
			ReferenceCountUtil.release(msg);
		}

	}


	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		super.userEventTriggered(ctx, evt);
		if (evt instanceof IdleStateEvent) {
			IdleStateEvent e = (IdleStateEvent) evt;
			switch (e.state()) {
				case READER_IDLE:{
						logger.info("和服务器连接空闲,准备重连:{}",ctx);
						ClientHandlerUtil.handleReaderIdle(ctx);
					}
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

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		super.channelUnregistered(ctx);
		logger.info("和服务器连接断开,准备重连:{}",ctx);
		ClientHandlerUtil.handleReaderIdle(ctx);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
	}
}
