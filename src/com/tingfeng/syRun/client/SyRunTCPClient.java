package com.tingfeng.syRun.client;

import com.tingfeng.syRun.client.handler.SyRunClientHandler;
import com.tingfeng.syRun.common.ConfigEntity;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;

public class SyRunTCPClient {

	private static boolean isInited = false;
	public static final int threadSize = 512;

	//private static Logger logger = LoggerFactory.getLogger(SignleRunTCPClient.class);
	private static IoSession session = null;
	private static NioSocketConnector connector = null;

	public static void main(String[] args) throws Exception
	{
		init(ConfigEntity.SERVER_IP,ConfigEntity.SERVDER_TCP_PORT);

	}

	public static synchronized void init(String serverIP,int serverPort) throws IOException, InterruptedException{
		if(!isInited){
			initClientConnect(serverIP,serverPort);
		}
	}


	private static void initClientConnect(String serverIP,int serverPort) {
		// Create TCP/IP connector.
		connector = new NioSocketConnector();
		// 创建接收数据的过滤器
		DefaultIoFilterChainBuilder chain = connector.getFilterChain();
		//设定这个过滤器将一行一行(/r/n)的读取数据
		/*chain.addLast("myChin", new ProtocolCodecFilter(new
		TextLineCodecFactory()));*/
		chain.addLast("codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),"\r\n", "\r\n")));
		//设定服务器端的消息处理器:一个SamplMinaServerHandler对象,
		connector.setHandler(SyRunClientHandler.getSigleInstance());
		// Set connect timeout.
		connector.setConnectTimeoutMillis(150 * 1000);
		connector.getSessionConfig().setTcpNoDelay(true);
		connector.getSessionConfig().setReceiveBufferSize(1024);
		connector.getSessionConfig().setSendBufferSize(1024);
		connector.getSessionConfig().setReadBufferSize(1024);
		//connector.getFilterChain().addLast("threadPool",new ExecutorFilter(Executors.newFixedThreadPool(threadSize)));
		//connector.getSessionConfig().setUseReadOperation(true);//设置消息可同步读
		//连结到服务器:
		ConnectFuture cf = connector.connect(new
				InetSocketAddress(serverIP,serverPort));
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
