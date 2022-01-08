package com.common.msg.kafka.network.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.io.Closeable;
import java.net.InetSocketAddress;

public interface NettyClient extends Closeable {
    void init();

    ChannelFuture connect(InetSocketAddress paramInetSocketAddress);

    <T> void send(Channel paramChannel, T paramT);
}


