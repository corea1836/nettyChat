package com.netty.chat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.ChannelGroupFutureListener;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import static com.netty.chat.nettyChat.allChannels;

@Slf4j
public class BroadCaster extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("Total channels: {}", allChannels.size());
        Long startTime = System.currentTimeMillis();
        final String message = new String("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea ");
        TextWebSocketFrame frame = new TextWebSocketFrame(message);
        ChannelGroupFuture future = allChannels.writeAndFlush(frame);
        future.addListener(new ChannelGroupFutureListener() {
                    @Override
                    public void operationComplete(ChannelGroupFuture future) throws Exception {
                        Long endTime = System.currentTimeMillis();
                        log.info("Elapsed time: {} ms", (endTime - startTime));
                        ctx.channel().close();
                        ReferenceCountUtil.release(msg); // 메시지 해제 추가
                        ctx.close();
                    }
                });
    }
}
