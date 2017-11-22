package com.tingfeng.syRun.server.util;


import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ServerWriteHelper {
    private  int minBufferSize = 1024;
    private  int maxBufferSize = 4096;
    private  int checkTime = 25;//毫秒,空闲后会将剩余缓冲区的消息发送
    private  int checkSleepTime = 3;//每隔X毫秒检查一次
    private  int removeTime = 100000;//100秒没有消息就会被移出
    private  final String separators = "\r\n";

    public static final int threadSize = 2;
    private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadSize);

    private static Logger logger = LoggerFactory.getLogger(ServerWriteHelper.class);

    //private static final AtomicInteger reSendCount = new AtomicInteger();


    public ServerWriteHelper(){

    }

    /**
     *
     * @param responseBean can not be null
     * @param reqMsg
     * @param channel
     */
    public void writeMsg(ResponseBean responseBean,Channel channel,String reqMsg){
        writeMsg(responseBean,reqMsg,channel,0,null);
    }

    /**
     *
     * @param reqMsg can not be null
     * @param channel
     * @param respMsg
     */
    public void writeMsg(String respMsg,Channel channel,String reqMsg){
        writeMsg(null,reqMsg,channel,0,respMsg);
    }

    /**
     *
     * @param responseBean 当respMsg!=null,将使用respMsg值
     * @param reqMsg
     * @param channel
     * @param hasSendCount
     * @param respMsg
     */
    private void writeMsg(ResponseBean responseBean,String reqMsg,Channel channel,int hasSendCount,String respMsg){
/*        if(reSendCount.get() > 2){
            logger.error("msg send failed,ready to reConnect...");
            allMsgReSendOverCount(channel);
        }*/
        if(hasSendCount > 2){
            logger.error("msg send failed,ready to reSend...");
            msgReSendOverCount(reqMsg,true,responseBean,channel,respMsg);
        }
        ChannelFuture channelFuture = channel.writeAndFlush(respMsg);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    Thread.sleep(ConfigEntity.getInstance().getTimeResendIdle());
                    //reSendCount.incrementAndGet();
                    writeMsg(responseBean,reqMsg,channel,hasSendCount + 1,respMsg == null ? JSONObject.toJSONString(responseBean) : respMsg);
                }
            }
        });
    }

    /**
     * 当前消息重发次数超过配置次数时回调
     */
    public abstract void msgReSendOverCount(String reqMsg,boolean needSplit,ResponseBean responseBean,Channel channel,String respMsg);



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
