package com.tingfeng.syRun.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.*;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
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
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);//NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器
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
            try {
               // EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
               // EventLoopGroup workerGroup = new NioEventLoopGroup();
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup);
                b.channel(NioServerSocketChannel.class);
                b.childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {//ChannelInitializer 是一个特殊的处理类，他的目的是帮助使用者配置一个新的 Channel
                        ChannelPipeline pipeline = ch.pipeline();
                        //pipeline.addLast("frameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 2, 0, 2));
                        //pipeline.addLast("frameEncoder", new LengthFieldPrepender(2));//此对象为 netty默认支持protocolbuf的编解码器
                        //pipeline.addLast("frameDecoder",new DelimiterBasedFrameDecoder(81920, Delimiters.lineDelimiter()));
                        //pipeline.addLast("frameEncoder",new DelimiterBasedFrameDecoder(81920, Delimiters.lineDelimiter()));
                        pipeline.addLast(new IdleStateHandler(ConfigEntity.getInstance().getTimeServerIdle()/1000, 0, 0));
                        pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(81920));
                        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                        pipeline.addLast("frameEncoder", new LineEncoder(LineSeparator.WINDOWS,CharsetUtil.UTF_8));
                        //pipeline.addLast("timeout", new IdleStateHandler(timer, 10, 10, 0));//此两项为添加心跳机制 10秒查看一次在线的客户端channel是否空闲，IdleStateHandler为netty jar包中提供的类
                        //pipeline.addLast("hearbeat", new Heartbeat());//此类 实现了IdleStateAwareChannelHandler接口
                        pipeline.addLast(SyRunSeverHandler.getInstance());
                    }
                });
                /**
                 * option() 是提供给NioServerSocketChannel 用来接收进来的连接。
                 * childOption() 是提供给由父管道 ServerChannel 接收到的连接
                 */
                b.option(ChannelOption.SO_BACKLOG, 128);//指定的 Channel 实现的配置参数
                b.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)
                b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//使用PooledByteBufAllocator内存池
                b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//关键是这句

                ChannelFuture channelFuture = b.bind(serverIp, serverPort).sync();

                // 等待服务器 socket 关闭 。
                // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
                channelFuture.channel().closeFuture().sync();
                logger.info("TCP服务器已启动");
                isInited = true;
            } finally {
                shutdown();
            }
        }
    }

	public static void shutdown() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}
}
