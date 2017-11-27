package com.tingfeng.syrun.client.util;

import com.alibaba.fastjson.JSONObject;
import com.tingfeng.syrun.client.SyRunTCPClient;
import com.tingfeng.syrun.common.*;
import com.tingfeng.syrun.common.bean.request.RequestBean;
import com.tingfeng.syrun.common.bean.response.ResponseBean;
import com.tingfeng.syrun.common.ex.OverRunTimeException;
import com.tingfeng.syrun.common.util.RequestUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ClientHandlerUtil {
    private static Logger logger = LoggerFactory.getLogger(ClientHandlerUtil.class);
    private static boolean isInit = false;
    public static final int SIZE_CORE_POOL = 5;
    public static final long TIME_KEEP_ALIVE = 60L;//线程的空闲存活时间1分钟
    public static final String MSG_SPLIT = "\\\r\\\n";

    private static ClientWriteHelper writeHelper = null ;
    private static HeartBeatHelper   hearBeatHelper = null;
    /**
     * 消息接收的线程池
     */
    private static final ExecutorService serviceMsgPool = new ThreadPoolExecutor(SIZE_CORE_POOL, Integer.MAX_VALUE,
            TIME_KEEP_ALIVE, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    static {

            writeHelper = new ClientWriteHelper() {
                /**
                 * 当前消息重发次数超过配置次数时回调
                 *
                 * @param sendMsg
                 */
                @Override
                public void msgReSendOverCount(String sendMsg) {
                            receiveSendFailMsg(sendMsg);
                }

                /**
                 * 所有消息的重发次数超过配置次数时的回调
                 */
                @Override
                public void allMsgReSendOverCount() {
                        SyRunTCPClient.doConnect();
                }
            };

            hearBeatHelper = new HeartBeatHelper(ConfigEntity.getInstance().getTimeHeartBeat()) {
                @Override
                public void writeMsg(Channel channel, String msg) {
                    writeHelper.write(channel,msg);
                }
            };
        }


    public static void startSendHeartBeatMsg(){
        hearBeatHelper.startSendHeartBeatMessage(SyRunTCPClient.getChannel());
    }

    /**
     *
     * @param reqMsg
     */
    public static void receiveSendFailMsg(String reqMsg){
            serviceMsgPool.submit(()-> {
                    logger.debug("Client,收到消息: " + reqMsg);
                    if (!HeartBeatHelper.isHeartBeatMessage(reqMsg)) {
                        JSONObject requestObject = JSONObject.parseObject(reqMsg);
                        ResponseBean responseBean = new  ResponseBean();
                        responseBean.setId(requestObject.getString(CodeConstants.RquestKey.ID));
                        responseBean.setErrorMsg("send failed");
                        responseBean.setStatus(ResponseStatus.CUSTOM.getValue());
                        handleMsg(responseBean);
                    }
                }
            );
    }

    /**
     *
     * @param msg
     */
    public static void receiveMsg(String msg ) {
        serviceMsgPool.submit(()-> {
                logger.debug("Client,收到消息: " + msg);
                if (!HeartBeatHelper.isHeartBeatMessage(msg)) {
                    ResponseBean responseBean = JSONObject.parseObject(msg, ResponseBean.class);
                    handleMsg(responseBean);
                }
        });
    }

    public static void receiveMsg(ResponseBean responseBean){
        serviceMsgPool.submit(()->{
            handleMsg(responseBean);
        });
    }

    /**
     * 具体的消息分发
     * @param responseBean
     */
    private static void handleMsg(ResponseBean responseBean){
        if(RequestUtil.isAsychronizedMsg(responseBean.getId())) {
            SyRunMsgAsynchronizeUtil.receiveMsg(responseBean);
        }else {
            SyRunMsgSynchronizeUtil.receiveMsg(responseBean);
        }
    }


    public static void sendMessage(RequestBean<?> requestBean){
        try {
            SyRunTCPClient.doConnect(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
        } catch (Exception e) {
            logger.error("");
        }
        sendMessage(SyRunTCPClient.getChannel(),requestBean);
    }

    /**
     * 通过多线程/唯一id辨识消息唯一性
     * @param requestBean
     * @return
     * @throws InterruptedException
     * @throws OverRunTimeException
     */
    public static void sendMessage(Channel channel , RequestBean<?> requestBean){
            final String msg = JSONObject.toJSONString(requestBean);
            logger.debug("Client,发送消息: " + msg);
            writeHelper.write(channel,msg);

    }

    public static void handleReaderIdle(ChannelHandlerContext ctx){
        SyRunTCPClient.doConnect();
    }
}
