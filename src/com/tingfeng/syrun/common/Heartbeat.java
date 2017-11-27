package com.tingfeng.syrun.common;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

public class Heartbeat //extends IdleStateAwareChannelHandler
{

/*
    int i = 0;

    @Override
    public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e)
            throws Exception {
        // TODO Auto-generated method stub
        super.channelIdle(ctx, e);

        if(e.getState() == IdleState.WRITER_IDLE)
            i++;

        if(i==3){
            e.getChannel().close();

            System.out.println("掉了。");
        }
    }
*/


}
