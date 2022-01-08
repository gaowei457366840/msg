package com.common.msg.kafka.event;

import com.common.msg.api.MessageRecord;
import com.common.msg.api.event.MqEvent;
import com.common.msg.api.producer.SendCallback;
import com.common.msg.api.producer.SendResult;
import com.common.msg.api.producer.bingding.ProducerConfig;


public class KafkaProduceEvent<V> extends MqEvent {
    private String key;
    private transient MessageRecord<V> messageRecord;
    private transient SendResult result;
    private transient SendCallback callback;

    public void setKey(String key) {
        this.key = key;
    }

    public void setMessageRecord(MessageRecord<V> messageRecord) {
        this.messageRecord = messageRecord;
    }

    public void setResult(SendResult result) {
        this.result = result;
    }

    public void setCallback(SendCallback callback) {
        this.callback = callback;
    }

    public String toString() {
        return "KafkaProduceEvent(super=" + super.toString() + ", key=" + getKey() + ", messageRecord=" + getMessageRecord() + ", result=" + getResult() + ", callback=" + getCallback() + ")";
    }

    public String getKey() {
        return this.key;
    }

    public MessageRecord<V> getMessageRecord() {
        return this.messageRecord;
    }

    public SendResult getResult() {
        return this.result;
    }

    public SendCallback getCallback() {
        return this.callback;
    }

    public KafkaProduceEvent() {
        setGroup(KafkaProduceEvent.class);
    }


    public ProducerConfig getConfig() {
        return (ProducerConfig) super.getConfig();
    }
}


