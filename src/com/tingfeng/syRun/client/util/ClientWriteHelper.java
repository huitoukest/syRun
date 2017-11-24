package com.tingfeng.syRun.client.util;


import com.tingfeng.syRun.common.ConfigEntity;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class ClientWriteHelper {

    private  final String separators = "\r\n";
    private static Logger logger = LoggerFactory.getLogger(ClientWriteHelper.class);
    private static final AtomicInteger reSendCount = new AtomicInteger();


    public ClientWriteHelper(){
    }



    public void write(Channel channel,String msg){
        writeMsg(msg,channel,0);
    }

    private void writeMsg(String msg,Channel channel,int hasSendCount){
        if(reSendCount.get() > 2){
            logger.error("msg send failed,ready to reConnect...");
            allMsgReSendOverCount();
        }
        if(hasSendCount > 2){
            logger.error("msg send failed,ready to reSend...");
            msgReSendOverCount(msg);
        }
        ChannelFuture channelFuture = channel.writeAndFlush(msg);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    Thread.sleep(ConfigEntity.getInstance().getTimeResendIdle());
                    reSendCount.incrementAndGet();
                    writeMsg(msg,channel,hasSendCount + 1);
                }
            }
        });
    }

    /**
     * 当前消息重发次数超过配置次数时回调
     */
    public abstract void msgReSendOverCount(String sendMsg);

    /**
     * 所有消息的重发次数超过配置次数时的回调
     */
    public abstract void allMsgReSendOverCount();
}
