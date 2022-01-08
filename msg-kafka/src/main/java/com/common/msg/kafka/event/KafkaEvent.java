package com.common.msg.kafka.event;

import com.common.msg.api.event.Event;
import com.common.msg.api.producer.SendCallback;


public class KafkaEvent extends Event {

    private transient int priority = 1;
    private transient SendCallback callback;
    private transient String type;
    private String topic;

    public void setType(String type) {
        this.type = type;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setCallback(SendCallback callback) {
        this.callback = callback;
    }

    public String toString() {
        return "KafkaEvent(super=" + super.toString() + ", type=" + getType() + ", topic=" + getTopic() + ", priority=" + getPriority() + ", callback=" + getCallback() + ")";
    }

    public String getType() {
        return this.type;
    }

    public String getTopic() {
        return this.topic;
    }

    public int getPriority() {
        return this.priority;
    }

    public SendCallback getCallback() {
        return this.callback;
    }
}


