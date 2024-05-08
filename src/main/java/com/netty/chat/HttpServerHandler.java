package com.netty.chat;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.*;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

import static com.netty.chat.nettyChat.allChannels;


@Slf4j
@ChannelHandler.Sharable
public class HttpServerHandler extends ChannelInboundHandlerAdapter {

    private WebSocketServerHandshaker handshaker;
    private static final Logger logger = LoggerFactory.getLogger(HttpServerHandler.class);

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//            ctx.pipeline().toMap().forEach((name, handler) -> {
//                if (name.equals("wsencoder")) {
//                    ctx.pipeline().remove("wsencoder");
//                }
//            });
            log.info("i am " + ctx.channel().toString());
            if (msg instanceof HttpRequest) {
                HttpRequest httpRequest = (HttpRequest) msg;
                HttpHeaders headers = httpRequest.headers();
                if (httpRequest.uri().equals("/test")) {
//                    logger.info("Total channels: {}", allChannels.size());
//                    Long startTime = System.currentTimeMillis();
//                    final String message = new String("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea ");
////                    BinaryWebSocketFrame frame = new BinaryWebSocketFrame(Unpooled.copiedBuffer(message.getBytes(StandardCharsets.UTF_8)));
////                    ByteBuf content = frame.content();
//                    TextWebSocketFrame frame = new TextWebSocketFrame(message);
//                    SimpleWebSocketFrameCodec codec = new SimpleWebSocketFrameCodec(false);
//                    List<Object> out = new ArrayList<>();
//                    codec.encode(ctx, frame, out);
//                    allChannels.forEach(c -> {
//
//                        boolean wrote = false;
//                        while(c.isWritable()) {
//                            c.write((frame.retainedDuplicate())).addListener(f -> {
//                                if (f.isSuccess()) {
//                                    log.info("success");
//                                } else {
//                                    log.error(String.valueOf(f.cause()));
//                                }
//                            });
//                            wrote = true;
//                        }
//                        if(wrote) {
//                            c.flush();
//                        }
//                    });
//                    ChannelGroupFuture future = allChannels.writeAndFlush(((ByteBuf) out.get(0)).retainedDuplicate(), ChannelMatchers.all(), true);

//                future.addListener(new ChannelGroupFutureListener() {
//                    @Override
//                    public void operationComplete(ChannelGroupFuture future) throws Exception {
//                        Long endTime = System.currentTimeMillis();
//                        logger.info("Elapsed time: {} ms", (endTime - startTime));
//                        ctx.channel().close();
//                        ReferenceCountUtil.release(msg); // 메시지 해제 추가
//                        ctx.close();
//                    }
//                });
//                ((ByteBuf) out.get(0)).release();
//                Long endTime = System.currentTimeMillis();
//                logger.info("Elapsed time: {} ms", (endTime - startTime));
//                ReferenceCountUtil.release(msg); // 메시지 해제 추가
//               // ReferenceCountUtil.release(frame); // 메시지 해제 추가
//                ctx.close();
                    ctx.fireChannelRead(msg);

            } else if (headers.get(HttpHeaderNames.CONNECTION).equalsIgnoreCase("Upgrade") &&
                    headers.get(HttpHeaderNames.UPGRADE).equalsIgnoreCase("WebSocket")) {
                //ctx.pipeline().replace(this, "websocketHandler", new WebSocketMessageHandler());
                handleHandshake(ctx, httpRequest);

                allChannels.add(ctx.channel());
            } else {
                logger.info("Incoming request is unknown");
                ReferenceCountUtil.release(msg); // 메시지 해제 추가
            }
        }
        else if (msg instanceof WebSocketFrame) {
            if (msg instanceof CloseWebSocketFrame) {
                log.info(ctx.channel().toString() + " is leave");
                ctx.close();
            }
        }
    }

    protected void handleHandshake(ChannelHandlerContext ctx, HttpRequest req) {
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(getWebSocketURL(), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    protected String getWebSocketURL() {
        String s = UUID.randomUUID().toString();
        return "ws://localhost:8002/chat" + s;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {

        logger.error("Exception caught", cause);
        log.info(String.valueOf(cause.getCause()));
        log.info(cause.getMessage());
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
    }
}
