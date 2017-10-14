package com.tingfeng.signleRun.client.handler;

import java.util.concurrent.TimeUnit;

import org.apache.mina.core.future.ReadFuture;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.signleRun.client.SignleRunTCPClient;
import com.tingfeng.signleRun.common.ex.OutTimeException;

public class SignleRunClientHandler extends IoHandlerAdapter {
	
	private static final  SignleRunClientHandler signleRunClientHandler = new SignleRunClientHandler();
	
	private SignleRunClientHandler() {}
	
	public static SignleRunClientHandler getSigleInstance(){
		return signleRunClientHandler;
	}
	
	private static String reMsg = "";
	
	private static Logger logger = LoggerFactory.getLogger(SignleRunClientHandler.class);  	
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
		reMsg = msg;
	}
	
	public  static String sendMessage(String msg) throws InterruptedException, OutTimeException{
		return sendMessage(SignleRunTCPClient.getSession(),msg);
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
	public  synchronized static String sendMessage(IoSession session,String msg) throws InterruptedException, OutTimeException{
		String returnMsg = "";
		WriteFuture future =null;
		/*int sleepInteval = 5;//每x毫秒检查一次
		int sleepCount = 0;
		if(session == null){	
			while (null == session) {
				session = minaSession;
				sleepCount ++ ;
				Thread.sleep(sleepInteval);
				if(sleepCount > 1500){
					logger.error("there is no mina's session!");
					break;
				}
			}
			
		}*/
		    /*if(session != null){*/
	    	future = session.write(msg); 
	    	returnMsg = readReturnMsg(future,session);
		       /* if (future.getException() != null) {
		            System.out.println(future.getException().getMessage());
		        } else if (future.isWritten()) {
		            System.out.println("msg was sent!");
		            // 发送、接受
		            ReadFuture readF = session.read();
		            readF.awaitUninterruptibly(3000);
		            if (readF.getException() != null) {
		                System.out.println(readF.getException().getMessage());
		            } else {
		            	returnMsg = (String) readF.getMessage();
		            }
		        } else {
		            System.out.println("error!");
		        }*/
		    	
		        /*future.addListener(new IoFutureListener<WriteFuture>() {	
					@Override
					public void operationComplete(WriteFuture future) {
						if( future.isWritten() )
			            {
							logger.warn("send sucess ！");
			            }else{
			            	logger.warn("[IMCORE]回复给客户端的数据发送失败！");
			            }
					}
				});*/
		       // future.awaitUninterruptibly();
		       // session.getCloseFuture().awaitUninterruptibly();
		        // 发送、接受
	           // ReadFuture readF = session.read();
	            /*readF.addListener(new IoFutureListener<ReadFuture>() {	
					@Override
					public void operationComplete(ReadFuture future) {
						if( future.isRead())
			            {
							returnMsg = (String) future.getMessage();
							logger.warn("send sucess ！");
			            }else{
			            	logger.warn("[IMCORE]回复给客户端的数据发送失败！");
			            }
					}
				});*/
	            //readF.awaitUninterruptibly();
	            
		        /*sleepInteval = 2;//每x毫秒检查一次
				sleepCount = 0;
				while (null == reMsg) {
					sleepCount ++ ;
					Thread.sleep(sleepInteval);
					if(sleepCount % 50 ==0){
						System.out.println("waiting for server result msg...");
					}
				}
			}*/
	    //System.out.print("send msg is :" + msg );
	    //System.out.println("\t returnMsg is :" + returnMsg);         
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
