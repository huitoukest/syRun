package com.tingfeng.signleRun.server.handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tingfeng.signleRun.server.util.SignleRunServerUtil;

public class SignleRunSeverHandler  extends IoHandlerAdapter{
	private static Logger logger = LoggerFactory.getLogger(SignleRunSeverHandler.class);  
    
	public static final Map<Long, IoSession> minaSessionMap = new ConcurrentHashMap<>();
	
    @Override
	public void sessionCreated(IoSession session) throws Exception {
		super.sessionCreated(session);
	}

	@Override
	public void sessionOpened(IoSession session) throws Exception {
		super.sessionOpened(session);
		minaSessionMap.put(session.getId(), session);
		//session.write(CodeConstants.Result.SUCCESS);
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		super.sessionClosed(session);
		minaSessionMap.remove(session.getId());
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		super.sessionIdle(session, status);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {
		super.messageSent(session, message);
	}

	@Override
	public void inputClosed(IoSession session) throws Exception {
		super.inputClosed(session);
	}

	/**
     * MINA的异常回调方法。
     * <p>
     * 本类中将在异常发生时，立即close当前会话。
     * 
     * @param session 发生异常的会话
     * @param cause 异常内容
     * @see IoSession#close(boolean)
     */
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception 
    {   
        logger.error("[IMCORE]exceptionCaught捕获到错了，原因是："+cause.getMessage(), cause);
        System.out.println(cause.toString());
        session.closeOnFlush();
        minaSessionMap.remove(session.getId());
    }
         
    /**
     * MINA框架中收到客户端消息的回调方法。
     * <p>
     * 本类将在此方法中实现完整的即时通讯数据交互和处理策略。
     * <p>
     * 为了提升并发性能，本方法将运行在独立于MINA的IoProcessor之外的线程池中，
     * 详见 {@link ServerLauncher#initAcceptor()}中的MINA设置代码 。
     * 
     * @param session 收到消息对应的会话引用
     * @param message 收到的MINA的原始消息封装对象，本类中是 {@link IoBuffer}对象
     * @throws Exception 当有错误发生时将抛出异常
     */
    @Override
    public void messageReceived(IoSession session, Object message)throws Exception 
    {
    	//System.out.println(System.currentTimeMillis() + ": Message server re: " + message);
    	//*********************************************** 接收数据  
    	String str = message.toString();
        if(null != str){
        	str = str.trim();
        }
    	try {			
			String result = SignleRunServerUtil.doServerWork(str);
			sendMessage(session,result);
		} catch (Exception e) {
			
		}
    	if (str.trim().equalsIgnoreCase("quit")) {
            session.closeOnFlush();
            return;  
        }

    }
    
    public static void sendMessage(IoSession session,String msg){
    	WriteFuture future = session.write(msg);
    	//session.write("\r\n");
        future.addListener(new IoFutureListener<WriteFuture>() {

			@Override
			public void operationComplete(WriteFuture future) {
				if( future.isWritten() )
	            {
					logger.warn("send sucess ！");
	            }else{
	            	logger.warn("[IMCORE]回复给客户端的数据发送失败！");
	            }
			}
		});
    }
}