package com.tingfeng.syRun.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoEventType;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.executor.OrderedThreadPoolExecutor;
import org.apache.mina.filter.executor.UnorderedThreadPoolExecutor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.server.handler.SyRunSeverHandler;

public class SyRunTCPServer {
	public static final int min_threadSize = 12;
	public static final int max_threadSize = 1024;
	public static final int time_keepAlive = 600;//秒

	private static boolean isInited = false;
	  
    public static void main(String[] args) throws IOException {  
    	init(ConfigEntity.SERVER_IP,ConfigEntity.SERVDER_TCP_PORT);
    }
    
    public static void init(String serverIP,int serverPort) throws IOException{
    	if(!isInited){
    		isInited = true;
	    	 //首先，我们为服务端创建IoAcceptor，NioSocketAcceptor是基于NIO的服务端监听器  
	        IoAcceptor acceptor = new NioSocketAcceptor(); 
	        //接着，如结构图示，在Acceptor和IoHandler之间将设置一系列的Fliter  
	        //包括记录过滤器和编解码过滤器。其中TextLineCodecFactory是mina自带的文本解编码器  
	        //acceptor.getFilterChain().addLast("logger", new LoggingFilter());
			acceptor.getFilterChain().addLast("codec",
	                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),"\r\n", "\r\n")));
			//系统默认是有序线程队列,采用无序目前会使收到的被拆分的消息无序,从而使消息不完整.
			//acceptor.getFilterChain().addLast("threadPool",new ExecutorFilter(Executors.newFixedThreadPool(threadSize)));
			//acceptor.getFilterChain().addLast("exec",getUnorderedExecutorFilter());
			acceptor.getFilterChain().addLast("exec",getOrderedExecutorFilter());
	        //配置事务处理Handler，将请求转由TimeServerHandler处理。
	        acceptor.setHandler(new SyRunSeverHandler());
	        //配置Buffer的缓冲区大小
	        acceptor.getSessionConfig().setReadBufferSize(10240);
	        //设置等待时间，每隔IdleTime将调用一次handler.sessionIdle()方法  
	        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE,ConfigEntity.IDLE_TIME);//10秒
	        //绑定端口
	        acceptor.bind(new InetSocketAddress(serverIP,serverPort));
    	}
    }

	/**
	 * 无序消息处理线程池
	 * @return
	 */
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
	}
}
