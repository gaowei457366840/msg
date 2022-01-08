package com.common.msg.kafka.network.netty;

import com.common.msg.kafka.common.MessageTypeEnum;
import com.common.msg.kafka.network.MessageProtobuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Sharable
public class IdleStateTriggerHandler
        extends ChannelInboundHandlerAdapter {
    private static final Logger log = LoggerFactory.getLogger(IdleStateTriggerHandler.class);


    private static final MessageProtobuf.Message HEARTBEAT_PING = MessageProtobuf.Message.newBuilder().setType(MessageTypeEnum.HEARTBEAT_REQ.getCode()).build();


    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        if (event instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) event).state();
            if (state == IdleState.WRITER_IDLE) {

                log.debug("HEARTBEAT_PING");
                ctx.writeAndFlush(HEARTBEAT_PING);
            }
            if (state == IdleState.READER_IDLE) {
                ctx.channel().close();
            }
        } else {
            super.userEventTriggered(ctx, event);
        }
    }
}


