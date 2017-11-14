package com.tingfeng.syRun.common;


import com.tingfeng.syRun.server.SyRunTCPServer;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteHelper {
    private  int minBufferSize = 1024;
    private  int maxBufferSize = 4096;
    private  int checkTime = 30;//30毫秒,空闲后会将剩余缓冲区的消息发送
    private  int checkSleepTime = 2;//每隔2毫秒检查一次
    private  int removeTime = 100000;//100秒没有消息就会被移出
    private  final String separators = "\r\n";

    public static final int threadSize = 4;
    private static final ExecutorService servicePool = Executors.newFixedThreadPool(threadSize);

    private static Logger logger = LoggerFactory.getLogger(WriteHelper.class);
    private  final Map<Channel,WriteBean> channelMap = new ConcurrentHashMap<>(2000);
    //private

    class WriteBean{
        private StringBuffer sb = new StringBuffer(maxBufferSize);
        private Long lastSendTime  = System.currentTimeMillis();
    }

    public WriteHelper(){
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
                    synchronized (WriteHelper.this) {
                        if (entry.getValue() == null) {
                            channelMap.put(channel, new WriteBean());
                        }
                        if (System.currentTimeMillis() -writeBean.lastSendTime >= checkTime) {
                            if(writeBean.sb.length() > 0) {
                                channel.writeAndFlush(writeBean.sb.toString());
                                //logger.debug("send a msg is:{}" ,writeBean.sb.toString());
                                writeBean.lastSendTime = System.currentTimeMillis();
                                writeBean.sb.setLength(0);
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

    public void write(Channel channel,String msg){
        servicePool.submit(()->{
            synchronized (WriteHelper.this){
                if(channelMap.get(channel) == null){
                    channelMap.put(channel,new WriteBean());
                }
                WriteBean writeBean = channelMap.get(channel);
                if(writeBean.sb.length() + msg.length() > maxBufferSize){

                    writeBean.sb.append(separators);
                    writeBean.sb.append(msg);
                    channel.writeAndFlush(writeBean.sb.toString());
                    //logger.debug("\r\nsend a big msg is:{},end big msg \r\n" ,writeBean.sb.toString());
                    writeBean.sb.setLength(0);
                    writeBean.lastSendTime = System.currentTimeMillis();
                }else{
                    if(writeBean.sb.length() > 0){
                        writeBean.sb.append(separators);
                    }
                    writeBean.sb.append(msg);

                }
            }
        });
    }

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
