package com.common.msg.kafka.consumer;

import com.common.msg.api.common.ConsumePatternEnum;
import com.common.msg.api.util.MillisecondClock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.clients.consumer.OffsetCommitCallback;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaConsumerWrapper<K, V> {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerWrapper.class);

    private final KafkaConsumer<K, V> consumer;

    private final ConsumePatternEnum patternEnum;


    KafkaConsumerWrapper(KafkaConsumer<K, V> consumer, ConsumePatternEnum patternEnum) {
        this.consumer = consumer;
        this.patternEnum = patternEnum;
    }

    synchronized void pause(Collection<TopicPartition> partitions) {
        this.consumer.pause(partitions);
    }

    synchronized void resume(Collection<TopicPartition> partitions) {
        try {
            this.consumer.resume(partitions);
        } catch (IllegalStateException e) {
            log.warn("No current assignment for partition " + partitions);
        }
    }

    synchronized Set<TopicPartition> paused() {
        return this.consumer.paused();
    }

    synchronized void commitSync() {
        this.consumer.commitSync();
    }

    synchronized void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets) {
        this.consumer.commitSync(offsets);
    }

    synchronized void commitAsync() {
        commitAsync(null);
    }

    synchronized void commitAsync(OffsetCommitCallback callback) {
        this.consumer.commitAsync(callback);
    }

    synchronized void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback) {
        this.consumer.commitAsync(offsets, callback);
    }


    synchronized List<List<ConsumerRecord<K, V>>> poll(long timeout) {
        return sequencePollAndPause(timeout);
    }


    synchronized List<List<ConsumerRecord<K, V>>> sequencePollAndPause(long timeout) {
        List<List<ConsumerRecord<K, V>>> list = new ArrayList<>();

        if (this.consumer.subscription().size() > 0) {
            ConsumerRecords<K, V> records = this.consumer.poll(timeout);
            if (!records.isEmpty()) {
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition partition : partitions) {
                    list.add(records.records(partition));
                }


                pause(partitions);
                log.info("Consumer poll topic:" + this.consumer.subscription() + ", message size:" + records.count() + ", and pause:" + partitions);
            }
        }
        return list;
    }

    synchronized List<List<ConsumerRecord<K, V>>> sequencePoll(long timeout) {
        List<List<ConsumerRecord<K, V>>> list = new ArrayList<>();

        if (this.consumer.subscription().size() > 0) {
            ConsumerRecords<K, V> records = this.consumer.poll(timeout);
            if (!records.isEmpty()) {
                Set<TopicPartition> partitions = records.partitions();
                for (TopicPartition partition : partitions) {
                    list.add(records.records(partition));
                }

                log.info("Consumer poll topic:" + this.consumer.subscription() + ", message size:" + records.count());
            }
        }
        return list;
    }

    synchronized Set<String> subscription() {
        return this.consumer.subscription();
    }

    synchronized void close() {
        this.consumer.close();
    }

    synchronized void subscribe(Collection<String> topics, ConsumerRebalanceListener listener) {
        this.consumer.subscribe(topics, listener);
    }

    synchronized void subscribe(Collection<String> topics) {
        this.consumer.subscribe(topics);
    }

    synchronized Set<TopicPartition> assignment() {
        return this.consumer.assignment();
    }

    synchronized void seek(TopicPartition partition, long offset) {
        this.consumer.seek(partition, offset);
    }

    synchronized void seekToBeginning(Collection<TopicPartition> partitions) {
        this.consumer.seekToBeginning(partitions);
    }

    synchronized void seekToEnd(Collection<TopicPartition> partitions) {
        this.consumer.seekToEnd(partitions);
    }

    synchronized Map<TopicPartition, OffsetAndTimestamp> offsetsForTimes(Map<TopicPartition, Long> timestampsToSearch) {
        return this.consumer.offsetsForTimes(timestampsToSearch);
    }

    void commitOffset(BlockingQueue<OffsetMetadata> commitQueue, long timeout) throws InterruptedException {
        long startTime = MillisecondClock.now();

        Set<TopicPartition> partitionSet = null;
        do {

            OffsetMetadata offsetMetadata = commitQueue.poll(timeout, TimeUnit.MILLISECONDS);

            if (null != offsetMetadata) {

                Map<TopicPartition, OffsetAndMetadata> offsetMap = Collections.singletonMap(offsetMetadata.getTopicPartition(), offsetMetadata.getOffsetAndMetadata());

                if (offsetMetadata.isLastOffset()) {

                    partitionSet = Collections.singleton(offsetMetadata.getTopicPartition());
                }

                commitAndPrintLog(offsetMap, partitionSet);
            } else {
                return;
            }

        } while (MillisecondClock.now() - startTime <= timeout);
    }

    void batchCommitOffset(BlockingQueue<OffsetMetadata> commitQueue, long timeout) throws InterruptedException {

        long startTime = MillisecondClock.now();

        Map<TopicPartition, OffsetAndMetadata> offsetMap = null;

        Set<TopicPartition> partitionSet = null;
        while (true) {

            OffsetMetadata offsetMetadata = commitQueue.poll(timeout, TimeUnit.MILLISECONDS);

            if (null != offsetMetadata) {
                if (null == offsetMap) {

                    offsetMap = new HashMap<>();
                }

                offsetMap.put(offsetMetadata.getTopicPartition(), offsetMetadata.getOffsetAndMetadata());

                if (offsetMetadata.isLastOffset()) {

                    if (null == partitionSet) {

                        partitionSet = new HashSet<>();
                    }

                    partitionSet.add(offsetMetadata.getTopicPartition());
                }


                if (MillisecondClock.now() - startTime > timeout)
                    break;
                continue;
            }
            break;

        }
        commitOffset(offsetMap, partitionSet);
    }

    private void commitOffset(Map<TopicPartition, OffsetAndMetadata> offsetMap, Set<TopicPartition> partitionSet) {

        if (offsetMap == null)
            return;
        try {
            commitAndPrintLog(offsetMap, partitionSet);
        } catch (KafkaException e) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Consumer commit offset failed:").append(offsetMap);
            if (partitionSet != null) {


                resume(partitionSet);
                buffer.append(", and resume:").append(partitionSet).append(" to avoid those partitions hung up.");
            }
            log.warn(buffer.toString(), (Throwable) e);
        }
    }

    private void commitAndPrintLog(Map<TopicPartition, OffsetAndMetadata> offsets, Set<TopicPartition> partitions) {
        if (null == offsets || offsets.size() == 0)
            return;
        StringBuffer buffer = new StringBuffer();
        buffer.append("Consumer ");
        if (offsets.size() > 0) {
            commitSync(offsets);
            buffer.append("commit offset:").append(offsets);
        }
        if (null != partitions && partitions.size() > 0) {
            resume(partitions);
            buffer.append(" resume:").append(partitions);
        }
        log.info(buffer.toString());
    }
}


