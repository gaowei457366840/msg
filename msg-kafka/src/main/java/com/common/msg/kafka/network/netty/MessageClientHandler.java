package com.common.msg.kafka.network.netty;

import com.common.msg.kafka.common.MessageTypeEnum;
import com.common.msg.kafka.network.ClientNetworkService;
import com.common.msg.kafka.network.MessageProtobuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Sharable
public class MessageClientHandler
        extends SimpleChannelInboundHandler<MessageProtobuf.Message> {
    private static final Logger log = LoggerFactory.getLogger(MessageClientHandler.class);


    private ClientNetworkService clientNetworkService;


    MessageClientHandler(ClientNetworkService clientNetworkService) {
        this.clientNetworkService = clientNetworkService;
    }


    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        log.info("Client channel active, local addr {}, remote addr {}", channel.localAddress().toString(), channel.remoteAddress().toString());
        ctx.fireChannelActive();
    }


    protected void channelRead0(ChannelHandlerContext ctx, MessageProtobuf.Message msg) throws Exception {
        if (msg.getType().equals(MessageTypeEnum.HEARTBEAT_RESP.getCode())) {
            log.debug("Received heartbeat response.");
        } else {
            this.clientNetworkService.receive((InetSocketAddress) ctx.channel().remoteAddress(), msg);
        }
    }


    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        log.info("Client channel inactive, remote addr {}", address.toString());
        this.clientNetworkService.connect(address, true);
        ctx.close();
    }


    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.warn("Client network occur a error.", cause);
    }
}

