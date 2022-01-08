package com.common.msg.kafka.event;

import com.common.msg.api.storage.StoreEvent;


public class KafkaConsumeEvent extends StoreEvent {
    public String toString() {
        return "KafkaConsumeEvent(super=" + super.toString() + ")";
    }

    public KafkaConsumeEvent() {
        setGroup(KafkaConsumeEvent.class);
    }
}

