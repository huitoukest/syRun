package com.tingfeng.syRun.server.util;

import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.common.HeartBeatHelper;
import com.tingfeng.syRun.common.ResponseStatus;
import com.tingfeng.syRun.common.bean.response.ResponseBean;
import com.tingfeng.syRun.common.ex.ReleaseLockException;
import com.tingfeng.syRun.server.service.impl.SyLockService;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerHandleUtil {
    public static final String MSG_SPLIT = "\\\r\\\n";
    private static Logger logger = LoggerFactory.getLogger(ServerHandleUtil.class);
    private static final ExecutorService servicePool = Executors.newFixedThreadPool(ConfigEntity.getInstance().getServerHandlPoolSize());
    public static final ThreadLocal<Channel> channels = new ThreadLocal<>();

    private static ServerWriteHelper writeHelper = null;
    //private static HeartBeatHelper hearBeatHelper = null;

    static {
        writeHelper = new ServerWriteHelper() {

            /**
             * 当前消息重发次数超过配置次数时回调
             *
             * @param reqMsg
             * @param needSplit
             * @param responseBean
             * @param channel
             */
            @Override
            public void msgReSendOverCount(String reqMsg, boolean needSplit, ResponseBean responseBean, Channel channel,String respMsg) {
                if(responseBean != null) {
                    SignleRunServerUtil.dealFailSendWork(reqMsg, responseBean);
                }
            }
        };
    }

    public static void receiveMsg(Channel channel, String message,boolean needSplit)
    {
        servicePool.submit(() ->{
            String str = null;
            String result = null;
            if(null == message){
                ResponseBean responseBean = new ResponseBean();
                responseBean.setStatus(ResponseStatus.FAIL.getValue());
                responseBean.setErrorMsg("null response msg ");
                sendMessage(channel,"NULL",responseBean);
            }else if(needSplit){
                String[] msgArray = message.split(MSG_SPLIT);
                for (String tmp : msgArray) {
                    receiveMsg(channel,tmp,false);
                }
            }else if (HeartBeatHelper.isHeartBeatMessage(message)){
                sendMessage(channel,message,HeartBeatHelper.getHeartBeatMessage(message));
            }else{
                str = message.toString();
                final String reqMsg = str;
                    ResponseBean responseBean = null;
                    try {
                        channels.set(channel);
                        responseBean = SignleRunServerUtil.doServerWork(reqMsg);
                        sendMessage(channel,reqMsg,responseBean);
                    }catch (Exception e) {
                        if(!(e instanceof ReleaseLockException)){
                            e.printStackTrace();
                            //logger.info("消息发送失败,ip:{},收到消息:{},异常:{}",channelHandlerContext.getRemoteAddress(),reqMsg,e);
                            //记录失败信息
                            SignleRunServerUtil.dealFailSendWork(reqMsg,responseBean);
                        }
                    }
            }
        });
    }

    public static void sendMessage(Channel channel,String reqMsg,String respMsg){
        writeHelper.writeMsg(respMsg,channel,reqMsg);
    }

    public static void sendMessage(Channel channel,final String reqMsg,final ResponseBean responseBean){
        writeHelper.writeMsg(responseBean,channel,reqMsg);
    }

    public static void handleReaderIdle(ChannelHandlerContext ctx) {
        ctx.close();
        SyLockService.removeBlockLock(ctx.channel().id().toString());
    }
}
