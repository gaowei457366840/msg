package com.common.msg.kafka.consumer;

import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.consumer.binding.ConsumerConfig;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaMessageOrderProcessor
        extends AbstractKafkaMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageOrderProcessor.class);


    public KafkaMessageOrderProcessor(ConsumerConfig config, BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue, BlockingQueue<OffsetMetadata> commitQueue) {
        super(config, recordQueue, commitQueue);
    }


    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !this.closeFlag) {
                try {
                    List<ConsumerRecord<String, byte[]>> records = this.recordQueue.poll(100L, TimeUnit.MILLISECONDS);
                    if (records != null) {

                        this.recordSize = records.size();
                        for (ConsumerRecord<String, byte[]> record : records) {
                            if (Thread.currentThread().isInterrupted() || this.closeFlag)
                                return;
                            if (this.topicPartition == null) {
                                this.topicPartition = new TopicPartition(record.topic(), record.partition());
                            }
                            if (null != record) {

                                process(record);
                                this.completeTask++;
                                this.lastUncommittedRecord = record;

                                commitOffset();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                reset();
            }
        } catch (Throwable t) {
            log.error("Consume message failed.", t);
        }
    }


    public void process(ConsumerRecord<String, byte[]> record) {
        onMessage(record);
    }


    public void commitOffset() throws InterruptedException {
        if (this.closeFlag)
            return;
        boolean isLastRecord = false;
        if (this.completeTask == this.recordSize) {
            isLastRecord = true;
        }
        boolean arrivedCommitLength = (this.completeTask % this.config.getBatchSize() == 0 || isLastRecord);
        long currentTime = MillisecondClock.now();
        boolean arrivedTime = (currentTime - this.lastCommitTime >= 30000L);
        if (arrivedCommitLength || arrivedTime) {
            this.lastCommitTime = currentTime;

            commitImmediately(this.topicPartition, isLastRecord);
        }
    }


    public void close() {

        this.closeFlag = true;
        try {
            commitImmediately(this.topicPartition, false);
        } catch (InterruptedException interruptedException) {
        }
    }


    private void commitImmediately(TopicPartition topicPartition, boolean isLastRecord) throws InterruptedException {
        if (null == topicPartition || this.closeFlag)
            return;
        OffsetMetadata offsetMetadata = new OffsetMetadata(topicPartition, new OffsetAndMetadata(this.lastUncommittedRecord.offset() + 1L), isLastRecord);

        this.commitQueue.put(offsetMetadata);
    }

    private void reset() {
        this.lastCommitTime = 0L;
        this.completeTask = 0;
        this.recordSize = 0;
        this.topicPartition = null;
    }
}


