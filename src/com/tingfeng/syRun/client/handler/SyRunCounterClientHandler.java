package com.tingfeng.syRun.client.handler;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.bean.RequestBean;
import com.tingfeng.syRun.client.SyRunCounterTCPClient;
import com.tingfeng.syRun.common.ex.OutTimeException;
/**
 * 
 * @author huitoukest
 *
 */
public class SyRunCounterClientHandler extends IoHandlerAdapter {
	
	private static final  SyRunCounterClientHandler signleRunClientHandler = new SyRunCounterClientHandler();
	
	private SyRunCounterClientHandler() {}
	
	public static SyRunCounterClientHandler getSigleInstance(){
		return signleRunClientHandler;
	}
	
	//private static Logger logger = LoggerFactory.getLogger(SignleRunClientHandler.class);  	
	//public static IoSession minaSession = null;
	
	//当一个客端端连结进入时
	@Override
	public void sessionOpened(IoSession session) throws Exception {
		//minaSession = session;
		System.out.println("incomming client :" + session.getRemoteAddress());
		//session.write("我来啦........");
		//session.closeOnFlush();
	}
	//当一个客户端关闭时
	@Override
	public void sessionClosed(IoSession session) {
		System.out.println("one Clinet Disconnect !");
		//minaSession = null;
	}
	//当客户端发送的消息到达时:
	@Override
	public synchronized void messageReceived(IoSession session, Object message)
	throws Exception {
		//我们己设定了服务器解析消息的规则是一行一行读取,这里就可转为String:
		String s=(String)message;
		receiveMsg(s);
		/*// Write the received data back to remote peer
		System.out.println("服务器发来的收到消息: "+s);
		//测试将消息回送给客户端
		session.write(s);*/
	}
	@Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}
	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
	}
	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
		super.exceptionCaught(session, cause);
	}
	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}
	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
	}
	
	public static void receiveMsg(String msg){
	}
	
	public  static String sendMessage(RequestBean<?> requestBean) throws InterruptedException, OutTimeException{
		return sendMessage(SyRunCounterTCPClient.getSession(),requestBean);
    }
	/**
	 * 目前必须使发送的消息和接收的消息互斥才能保证有序,以后考虑
	 * 通过多线程/唯一id辨识消息唯一性
	 * @param session
	 * @param msg
	 * @return
	 * @throws InterruptedException
	 * @throws OutTimeException
	 */
	public  synchronized static String sendMessage(IoSession session,RequestBean<?> requestBean) throws InterruptedException, OutTimeException{			
		String msg = JSONObject.toJSONString(requestBean);
		String returnMsg = "";
		WriteFuture future =null;
	    	future = session.write(msg);
	    	returnMsg = readReturnMsg(future,session);         
		return returnMsg;
    }
	
	public static String readReturnMsg(WriteFuture future,IoSession session) throws OutTimeException {
		future.awaitUninterruptibly();
 	    String returnMsg = "";
		// 接收
        ReadFuture readFuture = session.read();
        if (readFuture.awaitUninterruptibly(300, TimeUnit.SECONDS)) {
        	returnMsg = (String) readFuture.getMessage();
        } else {
           throw new OutTimeException("返回等待消息连接超时!");
        }
        return returnMsg;
	} 
	
}
