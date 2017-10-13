package com.tingfeng.signleRun.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.signleRun.client.handler.SignleRunClientHandler;
import com.tingfeng.signleRun.common.ConfigEntity;

public class SignleRunTCPClient {
	
	private static boolean isInited = false;
	
	private static Logger logger = LoggerFactory.getLogger(SignleRunTCPClient.class);
	private static IoSession session = null;
	private static NioSocketConnector connector = null;
	
	public static void main(String[] args) throws Exception
    {
		init();
		
    }
	
	 public static synchronized void init() throws IOException, InterruptedException{	
		 if(!isInited){				
				initClientConnect();		
	    	}
		/* new Thread(new Runnable() {
			public void run() {
				if(!isInited){				
					initClientConnect();		
		    	}
			}
		}).start();*/	
		/* do {
			 if(connector != null) {
				 System.out.println("isDisposing" + connector.isDisposing());
				 System.out.println("isDisposed" + connector.isDisposed());
				 System.out.println("isActive" + connector.isActive());
			 }//else {
				 Thread.sleep(500);
			 //}
		 }while(true);*/
		 
	}
	
	
	public static void initClientConnect() {
		// Create TCP/IP connector.
		connector = new NioSocketConnector();
		// 创建接收数据的过滤器
		DefaultIoFilterChainBuilder chain = connector.getFilterChain();
		//设定这个过滤器将一行一行(/r/n)的读取数据
		/*chain.addLast("myChin", new ProtocolCodecFilter(new
		TextLineCodecFactory()));*/
		chain.addLast("codec",  
                new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));  
		//设定服务器端的消息处理器:一个SamplMinaServerHandler对象,
		connector.setHandler(SignleRunClientHandler.getSigleInstance());
		// Set connect timeout.
		connector.setConnectTimeoutMillis(150 * 1000);
		connector.getSessionConfig().setTcpNoDelay(true);
		connector.getSessionConfig().setUseReadOperation(true);//设置消息可同步读
		//连结到服务器:
		ConnectFuture cf = connector.connect(new
		InetSocketAddress(ConfigEntity.SERVER_IP, ConfigEntity.SERVDER_TCP_PORT));
		// Wait for the connection attempt to be finished.
		cf.awaitUninterruptibly();
		session =  cf.getSession();
		isInited = true;
		/*System.out.println("client init :" + isInited);
		session.getCloseFuture().awaitUninterruptibly();								
		connector.dispose();*/						
	}
	 
	 public static synchronized void closeConnect() throws IOException{
		 
		//session.getCloseFuture().awaitUninterruptibly();
		 if(session != null) {
			 session.closeNow();
			 session.getService().dispose();
		 }
		 if(null != connector) {
			 connector.dispose();
		 }
	}
	
	public static synchronized IoSession getSession(){	
		return session;
	}
	
	 
	 
}
