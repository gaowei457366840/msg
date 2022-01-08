package com.common.msg.kafka.consumer;

import com.common.msg.kafka.consumer.strategy.KafkaConsumeStrategyFactory;
import com.common.msg.api.consumer.binding.ConsumerConfig;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class AbstractKafkaMessageProcessor
        implements KafkaMessageProcessor, Runnable {
    private static final Logger log = LoggerFactory.getLogger(AbstractKafkaMessageProcessor.class);


    final ConsumerConfig config;


    final BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue;


    final BlockingQueue<OffsetMetadata> commitQueue;


    volatile boolean closeFlag;


    volatile TopicPartition topicPartition;


    volatile int recordSize;


    volatile long lastCommitTime;


    volatile int completeTask;


    volatile ConsumerRecord<String, byte[]> lastUncommittedRecord;


    AbstractKafkaMessageProcessor(ConsumerConfig config, BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue, BlockingQueue<OffsetMetadata> commitQueue) {
        this.config = config;
        this.recordQueue = recordQueue;
        this.commitQueue = commitQueue;
    }

    protected void onMessage(ConsumerRecord<String, byte[]> record) {
        KafkaConsumeStrategyFactory.getInstance().getStrategy(this.config.getPriority()).onMessage(this.config, record);
        log.debug("Consume message:" + record);
    }
}

