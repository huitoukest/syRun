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
        if(hasSendCount > 2){
            logger.error("msg send failed,ready to reSend...");
            msgReSendOverCount(reqMsg,true,responseBean,channel,respMsg);
        }
        final String sendMsg = respMsg == null ? JSONObject.toJSONString(responseBean) : respMsg;
        ChannelFuture channelFuture = channel.writeAndFlush(sendMsg);
        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    Thread.sleep(ConfigEntity.getInstance().getTimeResendIdle());
                    writeMsg(responseBean,reqMsg,channel,hasSendCount + 1,sendMsg);
                }
            }
        });
    }

    /**
     * 当前消息重发次数超过配置次数时回调
     */
    public abstract void msgReSendOverCount(String reqMsg,boolean needSplit,ResponseBean responseBean,Channel channel,String respMsg);
}
