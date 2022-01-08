package com.common.msg.kafka.event;

import com.common.msg.api.event.MqEvent;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.transaction.TransactionStatus;

import java.net.InetSocketAddress;


public class KafkaEndTransactionEvent
        extends MqEvent {
    private String msgId;
    private TransactionStatus status;
    private InetSocketAddress node;
    private Throwable localException;

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setNode(InetSocketAddress node) {
        this.node = node;
    }

    public void setLocalException(Throwable localException) {
        this.localException = localException;
    }

    public String toString() {
        return "KafkaEndTransactionEvent(super=" + super.toString() + ", msgId=" + getMsgId() + ", status=" + getStatus() + ", node=" + getNode() + ", localException=" + getLocalException() + ")";
    }

    public String getMsgId() {
        return this.msgId;
    }

    public TransactionStatus getStatus() {
        return this.status;
    }

    public InetSocketAddress getNode() {
        return this.node;
    }

    public Throwable getLocalException() {
        return this.localException;
    }

    public KafkaEndTransactionEvent() {
        setGroup(KafkaEndTransactionEvent.class);
    }


    public ProducerConfig getConfig() {
        return (ProducerConfig) super.getConfig();
    }
}

