package com.laofeizhu.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;

/**
 * @author 老肥猪
 * @since 2019/8/21
 */
@Slf4j(topic = "netty-client")
public class NettyClient {

    @Setter
    private MessageCallback messageCallback;

    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private ChannelHandlerContext ctx;

    public NettyClient(String ip, Integer port) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ByteBuf delimiter = Unpooled.copiedBuffer("$$".getBytes());
                            channel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024 * 1024, delimiter));
                            channel.pipeline().addLast(new StringDecoder());
                            channel.pipeline().addLast(new NettyClientHandler());
                        }
                    });
            b.connect(ip, port).sync();
        } catch (Exception e) {
            log.error("客户端连接异常：[" + e.getMessage() + "]", e);
        }
    }

    public ChannelHandlerContext getCtx() {
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error("客户端等待发生异常[" + e.getMessage() + "]", e);
        }
        return this.ctx;
    }

    public interface MessageCallback {
        void onMessage(String message);
    }

    public class NettyClientHandler extends ChannelInboundHandlerAdapter {
        /**
         * 读取信息
         * @param ctx
         * @param msg
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            try {
                String message = (String) msg;
                if (messageCallback != null) {
                    messageCallback.onMessage(message);
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            NettyClient.this.ctx = ctx;
            log.info("客户端连接成功：" + ctx);
            countDownLatch.countDown();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        /**
         * 出现异常
         * @param ctx
         * @param cause
         * @throws Exception
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
            log.error("客户端发生异常：" + cause.getMessage());
        }
    }
}
