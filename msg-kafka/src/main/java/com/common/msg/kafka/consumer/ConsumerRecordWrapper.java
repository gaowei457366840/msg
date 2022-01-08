package com.common.msg.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;


public class ConsumerRecordWrapper {
    private ConsumerRecord<String, byte[]> record;

    public ConsumerRecordWrapper() {
    }

    public ConsumerRecordWrapper(ConsumerRecord<String, byte[]> record) {
        this.record = record;
    }

    public ConsumerRecord<String, byte[]> getRecord() {
        return this.record;
    }

    public void setRecord(ConsumerRecord<String, byte[]> record) {
        this.record = record;
    }
}


