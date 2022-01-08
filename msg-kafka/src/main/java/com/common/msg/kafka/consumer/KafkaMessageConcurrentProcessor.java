package com.common.msg.kafka.consumer;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.consumer.binding.ConsumerConfig;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaMessageConcurrentProcessor
        extends AbstractKafkaMessageProcessor {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageConcurrentProcessor.class);


    private final KafkaConsumerOffsetManager offsetManager;


    private final BlockingQueue<Long> innerCommitQueue;


    private final ExecutorService subExecutor;


    private final int maxPollRecordSize;

    private long lastIdleTime;


    public KafkaMessageConcurrentProcessor(ConsumerConfig config, BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue, BlockingQueue<OffsetMetadata> commitQueue, ExecutorService subExecutor, KafkaConsumerOffsetManager offsetManager) {
        super(config, recordQueue, commitQueue);
        this.innerCommitQueue = new ArrayBlockingQueue<>(ConfigManager.getInt("kafka.commit.sub.queue.size", 65535));
        this.subExecutor = subExecutor;
        this.offsetManager = offsetManager;
        this.maxPollRecordSize = config.getInt("max.poll.records", 512);
    }


    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && !this.closeFlag) {
                try {
                    List<ConsumerRecord<String, byte[]>> records = this.recordQueue.poll(100L, TimeUnit.MILLISECONDS);
                    if (records != null) {

                        reset(records.size());


                        int i = 0;
                        for (ConsumerRecord<String, byte[]> record : records) {
                            if (Thread.currentThread().isInterrupted() || this.closeFlag)
                                return;
                            if (this.topicPartition == null) {
                                this.topicPartition = new TopicPartition(record.topic(), record.partition());
                                this.offsetManager.addPartition(this.topicPartition, this.maxPollRecordSize, record.offset());
                            }
                            if (null != record) {

                                process(record);
                                if (i % this.config.getBatchSize() == 0 || i == records.size() - 1) {
                                    commitOffset();
                                }
                            }
                            i++;
                        }

                        waitOnComplete();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Throwable t) {
            log.error("Consume message failed.", t);
        }
    }

    private void waitOnComplete() throws InterruptedException {
        long currentTime = MillisecondClock.now();
        while (this.completeTask < this.recordSize && !this.closeFlag) {
            Thread.sleep(10L);
            if (MillisecondClock.now() - currentTime > 30000L) {
                currentTime = MillisecondClock.now();
                log.warn("The worker thread processes messages for more than 30 seconds and needs to check the performance of the business code. current partition:" + this.topicPartition);
            }


            commitOffset();
        }
    }


    public void process(ConsumerRecord<String, byte[]> record) throws InterruptedException {
        while (!isIdle()) {
            acknowledge(10L);
        }
        this.subExecutor.execute(new KafkaMessageSubProcessor(record));
    }


    public void commitOffset() throws InterruptedException {
        if (null == this.topicPartition || this.closeFlag)
            return;
        acknowledge();
        commitToQueue();
    }


    public void close() {
        this.closeFlag = true;
        try {
            this.offsetManager.commitOffset(this.topicPartition, false);
            this.innerCommitQueue.clear();
        } catch (InterruptedException interruptedException) {
        }
    }


    private void acknowledge() throws InterruptedException {
        while (true) {
            Long offset = this.innerCommitQueue.poll();
            if (null != offset) {
                this.offsetManager.acknowledge(this.topicPartition, offset.longValue());
                continue;
            }
            break;
        }
    }

    private void acknowledge(long timeout) throws InterruptedException {
        long startTime = MillisecondClock.now();
        while (true) {
            Long offset = this.innerCommitQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (null != offset) {
                this.offsetManager.acknowledge(this.topicPartition, offset.longValue());


                if (MillisecondClock.now() - startTime > timeout || this.closeFlag)
                    break;
                continue;
            }
            break;
        }
    }

    private void commitToQueue() throws InterruptedException {
        if (this.closeFlag)
            return;
        boolean isPoll = false;
        int commitableSize = this.offsetManager.getCommitableSize(this.topicPartition);

        if (commitableSize == 0)
            return;
        if (this.completeTask + commitableSize == this.recordSize) {
            isPoll = true;
        }

        boolean arrivedCommitLength = (commitableSize >= this.config.getBatchSize() || isPoll);

        long currentTime = MillisecondClock.now();
        boolean arrivedTime = (this.lastCommitTime > 0L && currentTime - this.lastCommitTime >= 30000L);
        if (arrivedCommitLength || arrivedTime) {
            this.lastCommitTime = currentTime;

            this.offsetManager.commitOffset(this.topicPartition, isPoll);
            this.completeTask += commitableSize;
        }
    }


    private void reset(int size) {
        this.lastCommitTime = 0L;
        this.recordSize = size;
        this.completeTask = 0;
        this.topicPartition = null;
    }

    private boolean isIdle() {
        ThreadPoolExecutor threadPool = (ThreadPoolExecutor) this.subExecutor;
        int activeCount = threadPool.getActiveCount();
        int waitQueueSize = threadPool.getQueue().size();

        if (activeCount <= threadPool.getMaximumPoolSize() && waitQueueSize == 0) {
            this.lastIdleTime = MillisecondClock.now();
            return true;
        }
        if (MillisecondClock.now() - this.lastIdleTime > 30000L) {
            log.warn("KMPSub pool is busy, processes messages for more than 30 seconds and needs to check the performance of the business code. current partition:" + this.topicPartition);
        }


        return false;
    }

    private class KafkaMessageSubProcessor
            implements Runnable {
        private ConsumerRecord<String, byte[]> record;

        KafkaMessageSubProcessor(ConsumerRecord<String, byte[]> record) {
            this.record = record;
        }


        public void run() {
            try {
                KafkaMessageConcurrentProcessor.this.onMessage(this.record);
                if (!KafkaMessageConcurrentProcessor.this.closeFlag) {
                    KafkaMessageConcurrentProcessor.this.innerCommitQueue.put(Long.valueOf(this.record.offset()));
                }
                this.record = null;
            } catch (Throwable t) {
                KafkaMessageConcurrentProcessor.log.error("Consume message failed.", t);
            }
        }
    }
}

