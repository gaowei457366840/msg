package com.common.msg.kafka.network.netty;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.kafka.network.ClientNetworkService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.internal.SystemPropertyUtil;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MessageClient
        implements NettyClient {
    private static final Logger log = LoggerFactory.getLogger(MessageClient.class);

    private Bootstrap bootstrap;

    private EventLoopGroup workerGroup;

    private MessageClientInitializer clientInitializer;


    public MessageClient(ClientNetworkService networkService) {
        this.clientInitializer = new MessageClientInitializer(networkService);
        init();
    }


    public void init() {
        int threads = ConfigManager.getInt("client.num.network.threads", Math.max(1, SystemPropertyUtil.getInt("io.netty.eventLoopThreads",
                NettyRuntime.availableProcessors() + 1) * 2));
        this.workerGroup = createEventLoopGroup(threads);
        this.bootstrap = new Bootstrap();
        try {
            if (this.workerGroup instanceof EpollEventLoopGroup) {
                groupsEpoll(this.bootstrap, this.workerGroup, false);
            } else {
                groupsNio(this.bootstrap, this.workerGroup, false);
            }
        } catch (Throwable e) {
            log.warn("Maybe used netty4.0.x lib.", e);
            this.bootstrap = new Bootstrap();
            if (this.workerGroup instanceof EpollEventLoopGroup) {
                groupsEpoll(this.bootstrap, this.workerGroup, true);
            } else {
                groupsNio(this.bootstrap, this.workerGroup, true);
            }
        }
    }


    public synchronized ChannelFuture connect(InetSocketAddress socketAddress) {
        return this.bootstrap.connect(socketAddress.getAddress().getHostAddress(), socketAddress
                .getPort()).syncUninterruptibly();
    }


    public <T> void send(Channel channel, T message) {
        if (null == channel || !channel.isActive()) {
            throw new MqClientException("Connection is inactive or not connected to the message server yet.");
        }
        channel.writeAndFlush(message);
    }


    public synchronized void close() {
        if (null != this.workerGroup) {
            this.workerGroup.shutdownGracefully();
        }
        this.workerGroup = null;
        this.bootstrap = null;
        log.info("Message client shutdown completed.");
    }

    private EventLoopGroup createEventLoopGroup(int threads) {
        try {
            return (EventLoopGroup) new EpollEventLoopGroup(threads);
        } catch (UnsatisfiedLinkError | ExceptionInInitializerError | NoClassDefFoundError ex) {
            return (EventLoopGroup) new NioEventLoopGroup(threads);
        }
    }

    private void groupsEpoll(Bootstrap bootstrap, EventLoopGroup workerGroup, boolean preVersion) {
        if (!preVersion) {
            ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) bootstrap.group(workerGroup))
                    .channel(EpollSocketChannel.class))
                    .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT))
                    .option(EpollChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(65536, 4194304)))
                    .option(EpollChannelOption.SO_KEEPALIVE, Boolean.valueOf(true)))
                    .option(EpollChannelOption.TCP_CORK, Boolean.valueOf(true)))
                    .handler((ChannelHandler) this.clientInitializer);
        } else {
            ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) bootstrap.group(workerGroup))
                    .channel(EpollSocketChannel.class))
                    .option(EpollChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT))
                    .option(EpollChannelOption.WRITE_BUFFER_LOW_WATER_MARK, Integer.valueOf(65536)))
                    .option(EpollChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, Integer.valueOf(4194304)))
                    .option(EpollChannelOption.SO_KEEPALIVE, Boolean.valueOf(true)))
                    .option(EpollChannelOption.TCP_CORK, Boolean.valueOf(true)))
                    .handler((ChannelHandler) this.clientInitializer);
        }
    }

    private void groupsNio(Bootstrap bootstrap, EventLoopGroup workerGroup, boolean preVersion) {
        if (!preVersion) {
            ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) bootstrap.group(workerGroup))
                    .channel(NioSocketChannel.class))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT))
                    .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(65536, 4194304)))
                    .option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true)))
                    .option(ChannelOption.TCP_NODELAY, Boolean.valueOf(true)))
                    .handler((ChannelHandler) this.clientInitializer);
        } else {
            ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) ((Bootstrap) bootstrap.group(workerGroup))
                    .channel(NioSocketChannel.class))
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT))
                    .option(ChannelOption.WRITE_BUFFER_LOW_WATER_MARK, Integer.valueOf(32768)))
                    .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, Integer.valueOf(65536)))
                    .option(ChannelOption.SO_KEEPALIVE, Boolean.valueOf(true)))
                    .option(ChannelOption.TCP_NODELAY, Boolean.valueOf(true)))
                    .handler((ChannelHandler) this.clientInitializer);
        }
    }
}


