package com.tingfeng.signleRun.controller;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import com.tingfeng.signleRun.common.ConfigEntity;

import handler.SignleRunSeverHandler;

public class SignleRunTCPServer {
	private static boolean isInited = false;
	private static final int PORT = ConfigEntity.SERVDER_TCP_PORT;
	  
    public static void main(String[] args) throws IOException {  
    	init();
    }
    
    public static void init() throws IOException{
    	if(!isInited){
    		isInited = true;
	    	 //首先，我们为服务端创建IoAcceptor，NioSocketAcceptor是基于NIO的服务端监听器  
	        IoAcceptor acceptor = new NioSocketAcceptor(); 
	        //接着，如结构图示，在Acceptor和IoHandler之间将设置一系列的Fliter  
	        //包括记录过滤器和编解码过滤器。其中TextLineCodecFactory是mina自带的文本解编码器  
	        //acceptor.getFilterChain().addLast("logger", new LoggingFilter());  
	        acceptor.getFilterChain().addLast("codec",  
	                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));  
	        //配置事务处理Handler，将请求转由TimeServerHandler处理。  
	        acceptor.setHandler(new SignleRunSeverHandler());
	        //配置Buffer的缓冲区大小  
	        acceptor.getSessionConfig().setReadBufferSize(1024);
	        //设置等待时间，每隔IdleTime将调用一次handler.sessionIdle()方法  
	        acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10); 
	        //绑定端口  
	        acceptor.bind(new InetSocketAddress(ConfigEntity.SERVER_IP,PORT));   
    	}
    }
}
