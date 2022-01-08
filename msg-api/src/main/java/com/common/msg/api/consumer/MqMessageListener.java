package com.common.msg.api.consumer;

public interface MqMessageListener<T> {
    AckAction onMessage(ReceiveRecord<T> paramReceiveRecord);

    boolean isRedeliver(ReceiveRecord<T> paramReceiveRecord);
}

