package com.tingfeng.syRun.client;

import com.tingfeng.syRun.client.handler.SyRunClientHandler;
import com.tingfeng.syRun.common.ConfigEntity;
import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.filter.executor.ExecutorFilter;
import org.apache.mina.filter.keepalive.KeepAliveFilter;
import org.apache.mina.filter.keepalive.KeepAliveMessageFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

public class SyRunTCPClient {

	public static final int bufferSize = 2048;
	private static boolean isInited = false;
	public static final int threadSize = 512;

	private static Logger logger = LoggerFactory.getLogger(SyRunTCPClient.class);
	private static IoSession session = null;
	private static NioSocketConnector connector = null;
	private static boolean customCloseConnect  = false;

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
		chain.addLast("codec",
				new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"),"\r\n", "\r\n")));
		//设定服务器端的消息处理器:一个SamplMinaServerHandler对象,
		connector.setHandler(SyRunClientHandler.getSigleInstance());
		// Set connect timeout.
		connector.setConnectTimeoutMillis(ConfigEntity.OUTTIME_CONNECT);
		connector.getSessionConfig().setTcpNoDelay(true);
		connector.getSessionConfig().setReceiveBufferSize(bufferSize);
		connector.getSessionConfig().setSendBufferSize(bufferSize);
		connector.getSessionConfig().setReadBufferSize(bufferSize);
		connector.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, ConfigEntity.IDLE_TIME);//30秒读写空闲
		/*//连结到服务器:
		ConnectFuture cf = connector.connect(new
				InetSocketAddress(serverIP,serverPort));
		// Wait for the connection attempt to be finished.
		cf.awaitUninterruptibly();
		session =  cf.getSession();*/
		connector.setDefaultRemoteAddress(new InetSocketAddress(serverIP,serverPort));// 设置默认访问地址
		setHearBeat();
		// 添加重连监听
		connector.addListener(new IoServiceListener() {
			@Override
			public void serviceActivated(IoService service) throws Exception {

			}

			@Override
			public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {

			}

			@Override
			public void serviceDeactivated(IoService service) throws Exception {

			}

			@Override
			public void sessionCreated(IoSession session) throws Exception {

			}

			@Override
			public void sessionClosed(IoSession session) throws Exception {

			}

			@Override
			public void sessionDestroyed(IoSession arg0) throws Exception {
				connectToServer(true);
			}
		});
		connectToServer(false);
		isInited = true;
	}

	/**
	 * 设置心跳包
	 */
	private static synchronized void setHearBeat(){
		/** 主角登场 */
		KeepAliveMessageFactory heartBeatFactory = new KeepAliveMessageFactory() {
			@Override
			public boolean isRequest(IoSession session, Object message) {
				return false;
			}

			@Override
			public boolean isResponse(IoSession session, Object message) {
				return false;
			}

			@Override
			public Object getRequest(IoSession session) {
				return null;
			}

			@Override
			public Object getResponse(IoSession session, Object request) {
				return null;
			}
		};
		KeepAliveFilter heartBeat = new KeepAliveFilter(heartBeatFactory);
		/** 是否回发 */
		heartBeat.setForwardEvent(true);
		/** 发送频率 */
		heartBeat.setRequestInterval(ConfigEntity.HEART_BEAT_TIME);
		//connector.getSessionConfig().setKeepAlive(true);
		connector.getFilterChain().addLast("heartbeat", heartBeat);
	}

	public static synchronized void connectToServer(boolean isReConnected){
		int connectCount = 1;
		while(!(isReConnected && customCloseConnect)) {//如果重连时是用户自定义关闭则不再重连
			try {
				if(isReConnected){
					Thread.sleep(ConfigEntity.RECONNECT_TIME * connectCount);
				}
				ConnectFuture future = connector.connect();
				future.awaitUninterruptibly();// 等待连接创建成功
				session = future.getSession();// 获取会话
				if (session.isConnected()) {
					logger.info("连接服务器 [" + connector.getDefaultRemoteAddress().getHostName() + ":" + connector.getDefaultRemoteAddress().getPort() + "]成功,时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					break;
				}
			} catch (Exception ex) {
				logger.info("服务器登录失败,"+ (ConfigEntity.RECONNECT_TIME /1000.0 * connectCount) + " 秒再连接一次:" + ex.getMessage() + ",时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			}
		}
	}

	public static synchronized void closeConnect() throws IOException{
		//session.getCloseFuture().awaitUninterruptibly();
		customCloseConnect = true;
		if(session != null) {
			session.closeNow();
			session.getService().dispose();
		}
		if(null != connector) {
			connector.dispose();
		}
		logger.info("服务器关闭成功,时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
	}

	public static synchronized IoSession getSession(){
			return session;
	}



}
