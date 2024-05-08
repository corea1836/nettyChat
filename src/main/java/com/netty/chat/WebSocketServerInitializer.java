package com.netty.chat;

import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import io.netty.handler.timeout.*;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        final ChannelPipeline pipeline = channel.pipeline();

        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpServerHandler())
                .addLast(new WriteTimeoutHandler(10))
                .addLast(new ChannelInboundHandlerAdapter() {
                    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                            throws Exception {
                            if (cause instanceof WriteTimeoutException)
                            ctx.fireExceptionCaught(cause);
                    }
                })
                .addLast(new DefaultEventLoopGroup() , new BroadCaster());
    }
}
