package com.common.msg.kafka.consumer.strategy;

import com.common.msg.api.consumer.binding.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface KafkaConsumeStrategy {
    void onMessage(ConsumerConfig paramConsumerConfig, ConsumerRecord<String, byte[]> paramConsumerRecord);
}
