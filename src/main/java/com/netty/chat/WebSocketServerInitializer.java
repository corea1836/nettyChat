package com.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;

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
                        if (cause instanceof ReadTimeoutException)
                            ctx.fireExceptionCaught(cause);
                    }
                });
    }
}
