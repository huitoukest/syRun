package com.tingfeng.syRun.client;

import com.tingfeng.syRun.client.handler.SyRunClientHandler;
import com.tingfeng.syRun.common.ConfigEntity;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.io.IOException;


public class SyRunTCPClient {
    private static Logger logger = LoggerFactory.getLogger(SyRunTCPClient.class);

    private static boolean isInited = false;

    private static String serverIP = null;
    private static int serverPort = 0;

    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    public static Bootstrap bootstrap = getBootstrap();
    public static Channel channel = null; //getChannel(HOST,PORT);
    /**
     * 初始化Bootstrap
     * @return
     */
    private synchronized static final Bootstrap getBootstrap(){
        Bootstrap b = null;
        try {
            b = new Bootstrap();
            b.group(workerGroup).channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    //pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 2, 0, 2));
                    //pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
                    pipeline.addLast("frameDecoder",new DelimiterBasedFrameDecoder(81920, Delimiters.lineDelimiter()));
                    //pipeline.addLast("frameEncoder",new DelimiterBasedFrameDecoder(81920, Delimiters.lineDelimiter()));
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    pipeline.addLast("handler", SyRunClientHandler.getSigleInstance());
                }
            });
            b.option(ChannelOption.SO_KEEPALIVE, true);
        }catch (Exception e){
            closeConnect();
        }
        return b;
    }

    public synchronized static final Channel getChannel(String host,int port){
        try {
            channel = bootstrap.connect(host, port).sync().channel();
        } catch (Exception e) {
            logger.error(String.format("连接Server(IP[%s],PORT[%s])失败", host,port),e);
            return null;
        }
        return channel;
    }

    public synchronized static final Channel getChannel(){
        if(null == channel){
            try {
                init();
            } catch (Exception e) {
                logger.error("连接Server失败!",e);
            }
        }
        return channel;
    }

    public synchronized static final Channel reConnectChannel(){
        return getChannel(serverIP,serverPort);
    }

    /*public static void sendMsg(String msg) throws Exception {
        if(channel!=null){
            channel.writeAndFlush(msg).sync();
        }else{
            logger.warn("消息发送失败,连接尚未建立!");
        }
    }*/

    /* public static void main(String[] args) throws Exception {
       try {
            long t0 = System.nanoTime();
            for (int i = 0; i < 100000; i++) {
                //Thread.sleep(1000);
                SyRunTCPClient.sendMsg(i+"你好1");
            }
            long t1 = System.nanoTime();
            System.out.println((t1-t0)/1000000.0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public static void main(String[] args) throws Exception
    {


    }

    public static synchronized void init(String serverIP,int serverPort) throws IOException, InterruptedException{
        if(!isInited){
            getChannel(serverIP,serverPort);
        }
    }

    public static synchronized void init() throws IOException, InterruptedException{
        init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
    }

    public static void closeConnect() {
        workerGroup.shutdownGracefully();
    }
}
