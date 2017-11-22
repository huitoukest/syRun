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
    private  int minBufferSize = 1024;
    private  int maxBufferSize = 4096;
    private  int checkTime = 25;//毫秒,空闲后会将剩余缓冲区的消息发送
    private  int checkSleepTime = 3;//每隔X毫秒检查一次
    private  int removeTime = 100000;//100秒没有消息就会被移出
    private  final String separators = "\r\n";

    public static final int threadSize = 2;
    private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadSize);

    private static Logger logger = LoggerFactory.getLogger(ClientWriteHelper.class);
    private  final Map<Channel,WriteBean> channelMap = new ConcurrentHashMap<>(2000);

    private static final AtomicInteger reSendCount = new AtomicInteger();
    //private

    class WriteBean{
        private StringBuffer reqMsgBuffer = new StringBuffer(maxBufferSize);//客户端发向服务器的消息
        private Long lastSendTime  = System.currentTimeMillis();
    }

    public ClientWriteHelper(){
        init();
    }

    private void init(){
        new Thread(()->{
            while(true) {
                Iterator<Map.Entry<Channel, WriteBean>> iterator = channelMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<Channel, WriteBean> entry = iterator.next();
                    Channel channel = entry.getKey();
                    WriteBean writeBean =  entry.getValue();
                    synchronized (ClientWriteHelper.this) {
                        if (entry.getValue() == null) {
                            channelMap.put(channel, new WriteBean());
                        }
                        if (System.currentTimeMillis() -writeBean.lastSendTime >= checkTime) {
                            if(writeBean.reqMsgBuffer.length() > 0) {
                                //channel.writeAndFlush(writeBean.sb.toString());
                                writeMsg(writeBean.reqMsgBuffer.toString(),channel,0);
                                //logger.debug("send a msg is:{}" ,writeBean.sb.toString());
                                writeBean.lastSendTime = System.currentTimeMillis();
                                writeBean.reqMsgBuffer.setLength(0);
                                channelMap.put(channel, writeBean);
                            }
                        }else if(System.currentTimeMillis() - writeBean.lastSendTime >= removeTime){
                            channelMap.remove(channel);
                        }
                    }
                    try {
                        Thread.sleep(checkSleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void write(Channel channel,String msg ){
        write(channel,msg,true);
    }

    public void write(Channel channel,String msg,boolean useCache){
        if(!useCache){
                writeMsg(msg, channel, 0);
        }else{
            servicePool.submit(()->{
                synchronized (ClientWriteHelper.this){
                    if(channelMap.get(channel) == null){
                        channelMap.put(channel,new WriteBean());
                    }
                    WriteBean writeBean = channelMap.get(channel);
                    if(writeBean.reqMsgBuffer.length() + msg.length() > maxBufferSize){

                        writeBean.reqMsgBuffer.append(separators);
                        writeBean.reqMsgBuffer.append(msg);
                        //channel.writeAndFlush(writeBean.sb.toString());
                        writeMsg(writeBean.reqMsgBuffer.toString(),channel,0);
                        //logger.debug("\r\nsend a big msg is:{},end big msg \r\n" ,writeBean.sb.toString());
                        writeBean.reqMsgBuffer.setLength(0);
                        writeBean.lastSendTime = System.currentTimeMillis();
                    }else{
                        if(writeBean.reqMsgBuffer.length() > 0){
                            writeBean.reqMsgBuffer.append(separators);
                        }
                        writeBean.reqMsgBuffer.append(msg);
                    }
                }
            });
        }
    }

    private void writeMsg(String msg,Channel channel,int hasSendCount){
        if(reSendCount.get() > 2){
            logger.error("msg send failed,ready to reConnect...");
            allMsgReSendOverCount();
        }
        if(hasSendCount > 2){
            logger.error("msg send failed,ready to reSend...");
            msgReSendOverCount(msg,true);
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
    public abstract void msgReSendOverCount(String sendMsg,boolean needSplit);

    /**
     * 所有消息的重发次数超过配置次数时的回调
     */
    public abstract void allMsgReSendOverCount();


    public int getMinBufferSize() {
        return minBufferSize;
    }

    public void setMinBufferSize(int minBufferSize) {
        this.minBufferSize = minBufferSize;
    }

    public int getMaxBufferSize() {
        return maxBufferSize;
    }

    public void setMaxBufferSize(int maxBufferSize) {
        this.maxBufferSize = maxBufferSize;
    }
}
