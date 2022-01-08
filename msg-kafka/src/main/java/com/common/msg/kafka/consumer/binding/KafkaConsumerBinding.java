package com.common.msg.kafka.consumer.binding;

import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.consumer.MqConsumer;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.bootstrap.KafkaClientFactory;
import com.common.msg.kafka.consumer.KafkaConsumerOffsetManager;
import com.common.msg.kafka.consumer.KafkaMessageConcurrentProcessor;
import com.common.msg.kafka.consumer.KafkaMessageConsumer;
import com.common.msg.kafka.consumer.KafkaMessageOrderProcessor;
import com.common.msg.kafka.consumer.KafkaMessageProcessor;
import com.common.msg.kafka.consumer.OffsetMetadata;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaConsumerBinding
        implements MqConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerBinding.class);


    private final ConsumerConfig config;


    private final KafkaMessageConsumer consumer;


    private final HashMap<TopicPartition, KafkaMessageProcessor> processorMap;


    private final ExecutorService executor;


    private static volatile ScheduledExecutorService scheduled;


    private ExecutorService subExecutor;


    private BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue;


    private BlockingQueue<OffsetMetadata> commitQueue;


    private KafkaConsumerOffsetManager offsetManager;


    private AtomicBoolean startupFlag = new AtomicBoolean(false);

    public KafkaConsumerBinding(ConsumerConfig config) {
        this.config = config;
        this.executor = ThreadUtil.newCachedPool("KMP-" + config.getTopic());
        this.consumer = (KafkaMessageConsumer) BeanHolder.getBean(KafkaMessageConsumer.class.getSimpleName());
        this.processorMap = new HashMap<>();
    }


    public synchronized void init(BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue, BlockingQueue<OffsetMetadata> commitQueue) {
        this.recordQueue = recordQueue;
        this.commitQueue = commitQueue;
    }


    public synchronized void onPartitionsRevoked() {
        for (Map.Entry<TopicPartition, KafkaMessageProcessor> entry : this.processorMap.entrySet()) {
            ((KafkaMessageProcessor) entry.getValue()).close();
        }
        this.processorMap.clear();
        if (null != this.offsetManager) this.offsetManager.clear();
    }

    public synchronized void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        if (this.config.getNumThreads() > 1) {
            if (null == this.subExecutor) {
                int coreSize = partitions.size() * this.config.getNumThreads();
                this.subExecutor = ThreadUtil.newCachedPool("KMPSub-" + this.config.getTopic(), coreSize, coreSize, 120L, new ThreadPoolExecutor.CallerRunsPolicy());

                this.offsetManager = new KafkaConsumerOffsetManager(this.commitQueue);
            } else {
                int coreSize = partitions.size() * this.config.getNumThreads();
                ((ThreadPoolExecutor) this.subExecutor).setCorePoolSize(coreSize);
                ((ThreadPoolExecutor) this.subExecutor).setMaximumPoolSize(coreSize);
            }
        }


        for (TopicPartition partition : partitions) {
            launchProcessor(partition);
        }
    }


    public synchronized void rewind(Collection<TopicPartition> partitions) {
        for (TopicPartition partition : partitions) {
            KafkaMessageProcessor processor = this.processorMap.get(partition);
            if (null != processor) {
                processor.close();
            }

            launchProcessor(partition);
        }
    }


    public void startup() {
        if (!this.startupFlag.get()) {
            KafkaClientFactory.awaitServerUrl((MqConfig) this.config);
            this.consumer.subscribe(this);
            this.startupFlag.set(true);
        }
    }


    public synchronized void shutdown() {

        if (null != this.executor) {

            synchronized (this.executor) {

                this.executor.shutdownNow();
            }
        }

        if (null != scheduled) {

            scheduled.shutdownNow();
        }
    }

    public synchronized void shutdown(long delayTime) {

        scheduleShutdownConsumer(delayTime);
    }

    public ConsumerConfig getConfig() {

        return this.config;
    }

    public void unSubscribe() {

        this.consumer.unSubscribe(this);
        shutdown();
    }


    private void launchProcessor(TopicPartition partition) {
        if (partition.topic().equals(this.config.getTopic()) &&
                !this.executor.isTerminated()) {
            KafkaMessageProcessor processor = (this.config.getNumThreads() > 1) ? (KafkaMessageProcessor) new KafkaMessageConcurrentProcessor(this.config, this.recordQueue, this.commitQueue, this.subExecutor, this.offsetManager) : (KafkaMessageProcessor) new KafkaMessageOrderProcessor(this.config, this.recordQueue, this.commitQueue);


            this.executor.execute((Runnable) processor);
            this.processorMap.put(partition, processor);
        }
    }


    private void scheduleShutdownConsumer(long delayTime) {
        if (null == scheduled) {
            synchronized (this) {
                if (null == scheduled) {
                    scheduled = Executors.newSingleThreadScheduledExecutor();
                }
            }
        }
        scheduled.schedule(new Runnable() {
            public void run() {
                KafkaConsumerBinding.this.unSubscribe();
            }
        }, delayTime, TimeUnit.MILLISECONDS);
    }
}

