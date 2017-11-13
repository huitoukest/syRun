package com.tingfeng.syRun.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.server.handler.SyRunSeverHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyRunTCPServer {
	/*public static final int min_threadSize = 12;
	public static final int max_threadSize = 512;
	public static final int time_keepAlive = 600;//秒
	private static Logger logger = LoggerFactory.getLogger(SyRunTCPServer.class);

	private static boolean isInited = false;

    public static void main(String[] args) throws IOException {  
    	init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
    }
    
    public static void init(String serverIP,int serverPort) throws IOException{
    	if(!isInited){
			logger.info("开始启动服务器,ip地址和端口是{}:{}",serverIP,serverPort);
    		isInited = true;
	    	 //首先，我们为服务端创建IoAcceptor，NioSocketAcceptor是基于NIO的服务端监听器  
	        IoAcceptor acceptor = new NioSocketAcceptor(); 
	        //接着，如结构图示，在Acceptor和IoHandler之间将设置一系列的Fliter  
	        //包括记录过滤器和编解码过滤器。其中TextLineCodecFactory是mina自带的文本解编码器  
	        //acceptor.getFilterChain().addLast("logger", new LoggingFilter());

			//acceptor.getFilterChain().addLast("exec",getOrderedExecutorFilter());
			acceptor.getFilterChain().addLast("codec",
	                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),"\r\n", "\r\n")));
			//系统默认是有序线程队列,采用无序目前会使收到的被拆分的消息无序,从而使消息不完整.
			//acceptor.getFilterChain().addLast("threadPool",new ExecutorFilter(Executors.newFixedThreadPool(threadSize)));
			//acceptor.getFilterChain().addLast("exec",getUnorderedExecutorFilter());
	        //配置事务处理Handler，将请求转由TimeServerHandler处理。
	        acceptor.setHandler(new SyRunSeverHandler());
	        //配置Buffer的缓冲区大小
			acceptor.getSessionConfig().setMinReadBufferSize(4196);
			acceptor.getSessionConfig().setMaxReadBufferSize(204800);
	        //设置等待时间，每隔IdleTime将调用一次handler.sessionIdle()方法  
	        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,ConfigEntity.getInstance().getTimeIoIdle());//10秒
	        //绑定端口
	        acceptor.bind(new InetSocketAddress(serverIP,serverPort));
			logger.info("启动服务器成功,ip地址和端口是{}:{}",serverIP,serverPort);
    	}
    }

	*//**
	 * 无序消息处理线程池
	 * @return
	 *//*
    protected static  ExecutorFilter getUnorderedExecutorFilter(){
		// 添加执行线程池
		UnorderedThreadPoolExecutor executor = new UnorderedThreadPoolExecutor(min_threadSize, max_threadSize,time_keepAlive, TimeUnit.SECONDS);
		// 这里是预先启动corePoolSize个处理线程
		executor.prestartAllCoreThreads();
		return new ExecutorFilter(executor,
				IoEventType.EXCEPTION_CAUGHT, IoEventType.MESSAGE_RECEIVED,
				IoEventType.SESSION_CLOSED, IoEventType.SESSION_IDLE,
				IoEventType.SESSION_OPENED);
	}

	protected static  ExecutorFilter getOrderedExecutorFilter(){
		// 添加执行线程池
		OrderedThreadPoolExecutor executor = new OrderedThreadPoolExecutor(min_threadSize, max_threadSize,time_keepAlive, TimeUnit.SECONDS);
		// 这里是预先启动corePoolSize个处理线程
		executor.prestartAllCoreThreads();
		return new ExecutorFilter(executor,
				IoEventType.EXCEPTION_CAUGHT, IoEventType.MESSAGE_RECEIVED,
				IoEventType.SESSION_CLOSED, IoEventType.SESSION_IDLE,
				IoEventType.SESSION_OPENED);
	}*/

	private static final Logger logger = LoggerFactory.getLogger(SyRunTCPServer.class);
	private static final String IP = "127.0.0.1";
	private static final int PORT = 9999;
	/**用于分配处理业务线程的线程组个数 */
	protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2; //默认
	/** 业务出现线程大小*/
	protected static final int BIZTHREADSIZE = 4;
	/*
 * NioEventLoopGroup实际上就是个线程池,
 * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
 * 每一个NioEventLoop负责处理m个Channel,
 * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
 */
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
	private static final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);

	protected static void run() throws Exception {
		ServerBootstrap b = new ServerBootstrap();
		b.group(bossGroup, workerGroup);
		b.channel(NioServerSocketChannel.class);
		b.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
				pipeline.addLast("frameEncoder", new LengthFieldPrepender(4));
				pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
				pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
				pipeline.addLast(new SyRunSeverHandler());
			}
		});

		b.bind(IP, PORT).sync();
		logger.info("TCP服务器已启动");
	}

	protected static void shutdown() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public static void main(String[] args) throws Exception {
		logger.info("开始启动TCP服务器...");
		SyRunTCPServer.run();
//      TcpServer.shutdown();
	}
}
