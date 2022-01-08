package com.common.msg.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaMessageProcessor extends Runnable {
    void process(ConsumerRecord<String, byte[]> paramConsumerRecord) throws InterruptedException;

    void commitOffset() throws InterruptedException;

    void close();
}

