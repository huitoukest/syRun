package com.tingfeng.syrun.client;

import com.tingfeng.syrun.client.handler.SyRunClientHandler;
import com.tingfeng.syrun.client.util.ClientHandlerUtil;
import com.tingfeng.syrun.common.ConfigEntity;
import io.netty.channel.*;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.timeout.IdleStateHandler;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;


public class SyRunTCPClient {
    private static Logger logger = LoggerFactory.getLogger(SyRunTCPClient.class);

    //private static boolean isInited = false;

    private static String serverIP = null;
    private static int serverPort = 0;

    private static EventLoopGroup workerGroup = new NioEventLoopGroup();
    public static Bootstrap bootstrap = getBootstrap();
    public static Channel channel = null; //getChannel(HOST,PORT);

    /**
     * 初始化Bootstrap
     * @return
     */
    private static final Bootstrap getBootstrap(){
        Bootstrap bootstrap = null;
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(workerGroup).channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    //pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 2, 0, 2));
                    //pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));
                    //pipeline.addLast("frameDecoder",new DelimiterBasedFrameDecoder(81920, Delimiters.lineDelimiter()));
                    //pipeline.addLast("frameEncoder",new DelimiterBasedFrameDecoder(81920, Delimiters.lineDelimiter()));

                    pipeline.addLast(new IdleStateHandler(ConfigEntity.getInstance().getTimeClientIdle()/1000, 0, 0));//心跳包
                    pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(81920));
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    pipeline.addLast("frameEncoder", new LineEncoder(LineSeparator.WINDOWS,CharsetUtil.UTF_8));
                    pipeline.addLast("handler", SyRunClientHandler.getSigleInstance());
                }
            });
            //bootstrap.remoteAddress(host, port);//服务器主机地址端口号
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        }catch (Exception e){
            closeConnect();
        }
        return bootstrap;
    }
    public  static final Channel doConnect(){
        return doConnect(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
    }
    public  static final Channel doConnect(String host,int port){
        return doConnect(host,port,ConfigEntity.getInstance().getTimeReconnect());
    }
    public  synchronized static final Channel doConnect(String host,int port,int timeInteval){
            if (channel != null && channel.isActive()) {
                return channel;
            }
        if(timeInteval > 1000000){
            throw new RuntimeException("failed to connect to server !");
        }
       try {
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();
            if(channelFuture.isSuccess()){
                channel = channelFuture.channel();
                logger.info("Connect to server:{}:{} successfully!",host,port);
                ClientHandlerUtil.startSendHeartBeatMsg();
                return channel;
            }else{
                throw new Exception("client connect to server fail!");
            }
        } catch (Exception e) {
            logger.error(String.format("连接Server(IP[%s],PORT[%s])失败", host,port),e);
            try {
                Thread.sleep(timeInteval);
            }catch (Exception e1){
                logger.error("等待链接失败!",e1);
            }
            return doConnect(host,port,timeInteval + 1);
        }
    }

    public synchronized static final Channel getChannel(){
            try {
             return   doConnect();
            } catch (Exception e) {
                logger.error("连接Server失败!",e);
            }
        return channel;
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
        doConnect();
    }

   /* public static synchronized void init(String serverIP,int serverPort) throws IOException, InterruptedException{
            if(!isInited){
                doConnect(serverIP,serverPort);
            }
    }

    public static synchronized void init() throws IOException, InterruptedException{
        init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
    }*/

    public static void closeConnect() {
        workerGroup.shutdownGracefully();
    }
}
