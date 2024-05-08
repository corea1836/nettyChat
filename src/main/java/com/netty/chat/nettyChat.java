package com.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.ImmediateEventExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


public class nettyChat {


    private Logger logger = LoggerFactory.getLogger(getClass());
    private static final int PORT = 8002;
    public static final ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    Channel ch;

    EventLoopGroup bossGroup ;
    EventLoopGroup workerGroup ;


    public void postConstruct(){ //@Value("${websocket.port}")int port){
//		this.port = port;
        new Thread(){
            @Override
            public void run() {
                try {
                    startServer();
                } catch (Exception e) {
                    logger.error("== failed in start websocket", e);
                }
            }
        }.start();
    }
    public nettyChat startServer() throws Exception {
        logger.info("== WebSocketServer start");

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(1);

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 5000);

        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new WebSocketServerInitializer())
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_SNDBUF, 10485760)
                .childOption(ChannelOption.SO_RCVBUF, 10485760);

        ch = b.bind(PORT).sync().channel();
        return this;
    }

    public void preDestroy(){
        shutdown();
    }
    public void shutdown(){
        ch.close().addListener(f->{
            logger.warn("== server closing");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        });
    }


    public static void main(String[] args) throws Exception {
        nettyChat main = new nettyChat();
        main.startServer();
        main.awaitTermination(); // 채널이 닫힐 때까지 대기
    }

    private void awaitTermination() {
        try {
            ch.closeFuture().sync(); // 채널이 닫힐 때까지 대기
        } catch (InterruptedException e) {
            logger.error("Error while waiting for channel to close", e);
        } finally {
            shutdown(); // 서버 종료
        }
    }
}
