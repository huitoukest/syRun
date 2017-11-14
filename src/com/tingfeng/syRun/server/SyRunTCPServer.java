package com.tingfeng.syRun.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import com.tingfeng.syRun.common.ConfigEntity;
import com.tingfeng.syRun.server.handler.SyRunSeverHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyRunTCPServer {
    /**用于分配处理业务线程的线程组个数 */
    protected static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2; //默认
    /** 业务出现线程大小*/
    protected static final int BIZTHREADSIZE = 4;
    /*
    * NioEventLoopGroup实际上就是个线程池,
    * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
    * 每一个NioEventLoop负责处理m个Channel,
    * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
    */
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);

	public static final int min_threadSize = 12;
	public static final int max_threadSize = 512;
	public static final int time_keepAlive = 600;//秒
	private static Logger logger = LoggerFactory.getLogger(SyRunTCPServer.class);

	private static boolean isInited = false;

    public static void main(String[] args) throws InterruptedException {
    	init(ConfigEntity.getInstance().getServerIp(),ConfigEntity.getInstance().getServerTcpPort());
    }

    public synchronized static void init(String serverIp,int serverPort) throws InterruptedException {
        if(!isInited){
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup);
            b.channel(NioServerSocketChannel.class);
            b.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 2, 0, 2));
                    pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));//此对象为 netty默认支持protocolbuf的编解码器
                    pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                    pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                    //pipeline.addLast("timeout", new IdleStateHandler(timer, 10, 10, 0));//此两项为添加心跳机制 10秒查看一次在线的客户端channel是否空闲，IdleStateHandler为netty jar包中提供的类
                    //pipeline.addLast("hearbeat", new Heartbeat());//此类 实现了IdleStateAwareChannelHandler接口
                    pipeline.addLast(new SyRunSeverHandler());
                }
            });

            b.bind(serverIp, serverPort).sync();
            logger.info("TCP服务器已启动");
            isInited = true;
        }
    }

	public static void shutdown() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}
}
