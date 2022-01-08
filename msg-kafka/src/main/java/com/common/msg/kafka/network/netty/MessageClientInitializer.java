package com.common.msg.kafka.network.netty;

import com.google.protobuf.MessageLite;
import com.common.msg.kafka.network.ClientNetworkService;
import com.common.msg.kafka.network.MessageProtobuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;


public class MessageClientInitializer
        extends ChannelInitializer<SocketChannel> {
    private final MessageClientHandler clientHandler;
    private final IdleStateTriggerHandler idleStateTriggerHandler;

    public MessageClientInitializer(ClientNetworkService networkService) {
        this.clientHandler = new MessageClientHandler(networkService);
        this.idleStateTriggerHandler = new IdleStateTriggerHandler();
    }


    public void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new ChannelHandler[]{(ChannelHandler) new ProtobufVarint32FrameDecoder()});
        p.addLast(new ChannelHandler[]{(ChannelHandler) new ProtobufDecoder((MessageLite) MessageProtobuf.Message.getDefaultInstance())});
        p.addLast(new ChannelHandler[]{(ChannelHandler) new ProtobufVarint32LengthFieldPrepender()});
        p.addLast(new ChannelHandler[]{(ChannelHandler) new ProtobufEncoder()});
        p.addLast(new ChannelHandler[]{(ChannelHandler) new IdleStateHandler(30L, 10L, 0L, TimeUnit.SECONDS)});
        p.addLast(new ChannelHandler[]{(ChannelHandler) this.idleStateTriggerHandler});
        p.addLast(new ChannelHandler[]{(ChannelHandler) this.clientHandler});
    }
}


