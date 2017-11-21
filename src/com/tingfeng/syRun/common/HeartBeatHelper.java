package com.tingfeng.syRun.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class HeartBeatHelper {
    private static Logger logger = LoggerFactory.getLogger(HeartBeatHelper.class);
    public static final String heartMsg = "_";
    public static final String heartMsgKey = "heartMsgKey";
    private final List<Channel> channelList = new ArrayList<>(5);
    private boolean isInit = false;
    private int sendInterval = 10000;//默认心跳数据发送间隔

    public HeartBeatHelper(int sendInterval){
        this.sendInterval = sendInterval;
    }

    /**
     * 初始化定时心跳数据发送器
     * @param  sendInterval 发送间隔的毫秒数
     */
    protected void initMsgSender(int sendInterval){
       synchronized (heartMsgKey){
           if(!isInit){
               new Thread(()->{
                   while (true){
                       synchronized (heartMsgKey){
                           for(Channel channel: channelList){
                               writeMsg(channel,heartMsg);
                           }
                       }
                       try {
                           Thread.sleep(sendInterval);
                       } catch (InterruptedException e) {
                           logger.error("send hearbeat msg Interrupted",e);
                       }
                   }
               }).start();
           }
       }
    }

    /**
     * 将会定时发送心跳数据
     * @param channel
     */
    public synchronized void startSendHeartBeatMessage(Channel channel){
           channelList.add(channel);
           initMsgSender(sendInterval);
    }

    public synchronized void endSendHeartBeatMessage(Channel channel){
          channelList.remove(channelList);
    }

    public static boolean isHeartBeatMessage(String message){
           if(heartMsg.equals(message)){
               return true;
           }
           return false;
    }

    /**
     * 收到的请求消息,非心跳消息将会返回null
     * @param reqMessage
     */
    public static String  getHeartBeatMessage(String reqMessage){
       if(isHeartBeatMessage(reqMessage)){
           return heartMsg;
       }
       return null;
    }

    public void userEventTriggered(ChannelHandlerContext ctx, Object evt )
    {
        // IdleStateHandler 所产生的 IdleStateEvent 的处理逻辑.
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
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

    /**
     * 空闲的时候进行处理
     * @param ctx
     */
    abstract  public void handleReaderIdle(ChannelHandlerContext ctx) ;
    abstract  public void writeMsg(Channel channel,String msg) ;
}
