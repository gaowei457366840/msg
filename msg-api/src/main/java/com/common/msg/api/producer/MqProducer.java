package com.common.msg.api.producer;

import com.common.msg.api.MessageRecord;
import com.common.msg.api.transaction.TransactionExecuter;
import com.common.msg.api.bootstrap.ServiceLifecycle;

/**
 * 通用的mq producer生产者
 */
public interface MqProducer extends ServiceLifecycle {
    <V> SendResult send(MessageRecord<V> paramMessageRecord);

    <V> void sendAsync(MessageRecord<V> paramMessageRecord, SendCallback paramSendCallback);

    <V> void sendOneway(MessageRecord<V> paramMessageRecord);

    <V> SendResult send(MessageRecord<V> paramMessageRecord, TransactionExecuter paramTransactionExecuter, Object paramObject);
}

