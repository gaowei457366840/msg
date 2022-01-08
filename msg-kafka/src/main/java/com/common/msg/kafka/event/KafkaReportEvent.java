package com.common.msg.kafka.event;

import com.common.msg.api.storage.StoreEvent;


public class KafkaReportEvent
        extends StoreEvent {
    public String toString() {
        return "KafkaReportEvent(super=" + super.toString() + ")";
    }

    public KafkaReportEvent() {
        setGroup(KafkaReportEvent.class);
    }
}


