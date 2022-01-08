package com.common.msg.kafka.consumer;

import com.common.msg.api.common.ConsumePatternEnum;
import com.common.msg.api.common.MessageModelEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.consumer.binding.KafkaConsumerBinding;
import com.common.msg.api.bootstrap.Destroyable;
import com.common.msg.api.bootstrap.MqClient;
import com.common.msg.api.consumer.MqMessageListener;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.exception.MqConfigException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndTimestamp;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaMessageConsumer
        implements Destroyable {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageConsumer.class);


    private final KafkaMessageConsumerFactory factory;


    private final Map<String, Map<String, KafkaMessagePollThread>> perUrlPoolMap = new HashMap<>(3);


    private final Map<String, KafkaMessagePollThread> perTopicPoolMap = new HashMap<>(3);


    private final AtomicBoolean closeFlag = new AtomicBoolean(false);


    private volatile ExecutorService executor;


    private boolean mqRouterSwitch;

    private boolean kafkaCommandTopicListenerSwitch;


    public KafkaMessageConsumer(KafkaMessageConsumerFactory factory) {
        this.factory = factory;

        String routerSwitch = ConfigManager.getString("mq.router.switch", "off");
        this.mqRouterSwitch = ("on".equalsIgnoreCase(routerSwitch) || "true".equalsIgnoreCase(routerSwitch));
        String listenerSwitch = ConfigManager.getString("kafka.command.topic.listener.switch", "off");
        this.kafkaCommandTopicListenerSwitch = ("on".equalsIgnoreCase(listenerSwitch) || "true".equalsIgnoreCase(listenerSwitch));
    }


    public synchronized void subscribe(KafkaConsumerBinding consumerBinding) {
        KafkaMessagePollThread pollThread;
        ConsumerConfig config = consumerBinding.getConfig();

        if (this.kafkaCommandTopicListenerSwitch && !this.mqRouterSwitch && !this.perUrlPoolMap.containsKey(config.getSvrUrl())) {
            startCommandConsumer(config.getSvrUrl());
        }


        String clientId = config.getProps().getProperty("client.id");
        if (null == this.perUrlPoolMap.get(config.getSvrUrl())) {
            this.perUrlPoolMap.put(config.getSvrUrl(), new HashMap<>());
        }
        if (!((Map) this.perUrlPoolMap.get(config.getSvrUrl())).containsKey(clientId)) {


            pollThread = new KafkaMessagePollThread(this.factory.getConsumer(config), (config.getNumThreads() > 1) ? ConsumePatternEnum.CONCURRENCY : ConsumePatternEnum.NORMAL);
            int poolSize = ConfigManager.getInt("kafka.consumer.pool.max.size", 32);
            if (null == this.executor) {
                this.executor = ThreadUtil.newCachedPool("KMPoll", poolSize, poolSize, 120L, new RejectedExecutionHandler() {
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        throw new MqConfigException("Consumer thread poll size over the [kafka.consumer.pool.max.size].");
                    }
                });
            }
            this.executor.execute(pollThread);
            ((Map<String, KafkaMessagePollThread>) this.perUrlPoolMap.get(config.getSvrUrl())).put(clientId, pollThread);
        } else {
            pollThread = (KafkaMessagePollThread) ((Map) this.perUrlPoolMap.get(config.getSvrUrl())).get(clientId);
        }
        if (config.getProps() == null || !config.getProps().containsKey("__TEMPORARY")) {
            String key = StringUtil.join(":", new String[]{config.getGroupId(), config.getTopic()});
            this.perTopicPoolMap.put(key, pollThread);
        }

        pollThread.subscribe(consumerBinding);
    }


    public synchronized void unSubscribe(KafkaConsumerBinding consumerBinding) {
        ConsumerConfig config = consumerBinding.getConfig();
        String clientId = config.getProps().getProperty("client.id");
        KafkaMessagePollThread pollThread = (KafkaMessagePollThread) ((Map) this.perUrlPoolMap.get(config.getSvrUrl())).get(clientId);
        pollThread.unSubscribe(consumerBinding);
        ((Map) this.perUrlPoolMap.get(config.getSvrUrl())).remove(clientId);
    }


    public synchronized void rewind(ConsumerConfig config, String command) {
        String clientId = config.getProps().getProperty("client.id");
        KafkaMessagePollThread pollThread = (KafkaMessagePollThread) ((Map) this.perUrlPoolMap.get(config.getSvrUrl())).get(clientId);
        pollThread.rewind(config.getGroupId(), config.getTopic(), command);
    }


    public void destroy() {
        this.closeFlag.set(true);
        this.perTopicPoolMap.clear();
        for (Map.Entry<String, Map<String, KafkaMessagePollThread>> entry : this.perUrlPoolMap.entrySet()) {
            for (Map.Entry<String, KafkaMessagePollThread> innerEntry : (Iterable<Map.Entry<String, KafkaMessagePollThread>>) ((Map) entry.getValue()).entrySet()) {
                ((KafkaMessagePollThread) innerEntry.getValue()).destroy();
            }
        }
        if (null != this.executor) {
            this.executor.shutdown();
        }
        this.perUrlPoolMap.clear();
        log.info("Destroy kafka consumer successful.");
    }


    private class KafkaMessagePollThread
            implements Runnable, Destroyable {
        private final KafkaConsumerWrapper<String, byte[]> consumerWrapper;


        private final BlockingQueue<OffsetMetadata> commitQueue = new ArrayBlockingQueue<>(
                ConfigManager.getInt("kafka.commit.queue.size", 65535));


        private final Collection<String> topics = new ArrayList<>();


        private final Map<String, KafkaConsumerBinding> bindingMap = new HashMap<>();


        private final Map<String, BlockingQueue<List<ConsumerRecord<String, byte[]>>>> topicRecordMap = new ConcurrentHashMap<>();


        private final Object lock = new Object();

        private final AtomicBoolean stopFlag = new AtomicBoolean(false);


        KafkaMessagePollThread(KafkaConsumer<String, byte[]> consumer, ConsumePatternEnum consumePattern) {
            this.consumerWrapper = (KafkaConsumerWrapper) new KafkaConsumerWrapper<>((KafkaConsumer) consumer, consumePattern);
        }


        synchronized void subscribe(KafkaConsumerBinding consumerBinding) {
            BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue = new ArrayBlockingQueue<>(1024);

            this.topicRecordMap.put(consumerBinding.getConfig().getTopic(), recordQueue);
            this.bindingMap.put(consumerBinding.getConfig().getTopic(), consumerBinding);
            consumerBinding.init(recordQueue, this.commitQueue);

            this.topics.add(consumerBinding.getConfig().getTopic());
            this.consumerWrapper.subscribe(this.topics, new HandleRebalanceListener());
        }


        synchronized void unSubscribe(KafkaConsumerBinding consumerBinding) {
            this.topicRecordMap.remove(consumerBinding.getConfig().getTopic());
            this.bindingMap.remove(consumerBinding.getConfig().getTopic());
            this.topics.remove(consumerBinding.getConfig().getTopic());
            this.consumerWrapper.subscribe(this.topics);
            if (this.topics.size() == 0) {
                destroy();
            }
        }

        synchronized void rewind(String groupId, String topic, String command) {

            KafkaConsumerBinding binding = this.bindingMap.get(topic);
            if (null != binding && ((KafkaConsumerBinding) this.bindingMap.get(topic)).getConfig().getGroupId().equalsIgnoreCase(groupId)) {
                KafkaMessageConsumer.log.info("Received rewind command: groupId[" + groupId + "] topic[" + topic + "] command[" + command + "].");

                synchronized (this.lock) {

                    Set<TopicPartition> partitionSet = this.consumerWrapper.assignment();
                    Set<TopicPartition> windPartitionSet = new HashSet<>();
                    for (TopicPartition partition : partitionSet) {
                        if (partition.topic().equalsIgnoreCase(topic)) {
                            windPartitionSet.add(partition);
                        }
                    }

                    this.consumerWrapper.pause(windPartitionSet);
                    binding.rewind(windPartitionSet);

                    try {
                        this.consumerWrapper.batchCommitOffset(this.commitQueue, 5000L);
                    } catch (InterruptedException interruptedException) {
                    }

                    if ("earliest".equalsIgnoreCase(command)) {
                        this.consumerWrapper.seekToBeginning(windPartitionSet);
                    } else if ("latest".equalsIgnoreCase(command)) {
                        this.consumerWrapper.seekToEnd(windPartitionSet);
                    } else {
                        HashMap<TopicPartition, Long> seekPartitionTimes = new HashMap<>();
                        Long windTime = Long.valueOf(Long.parseLong(command));
                        for (TopicPartition partition : windPartitionSet) {
                            seekPartitionTimes.put(partition, windTime);
                        }

                        Map<TopicPartition, OffsetAndTimestamp> seekPartition = this.consumerWrapper.offsetsForTimes(seekPartitionTimes);
                        for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : seekPartition.entrySet()) {
                            this.consumerWrapper.seek(entry.getKey(), ((OffsetAndTimestamp) entry.getValue()).offset());
                        }
                    }


                    this.consumerWrapper.resume(windPartitionSet);

                    KafkaMessageConsumer.log.info("Rewind command: groupId[" + groupId + "] topic[" + topic + "] command[" + command + "] successful.");
                }
            }
        }


        public void run() {
            ThreadUtil.sleep(1000L, KafkaMessageConsumer.log);
            while (!KafkaMessageConsumer.this.closeFlag.get() && !this.stopFlag.get()) {
                synchronized (this.lock) {
                    try {
                        this.consumerWrapper.batchCommitOffset(this.commitQueue, 50L);
                        List<List<ConsumerRecord<String, byte[]>>> list = (List) this.consumerWrapper.poll(50L);
                        if (list.size() > 0) {
                            for (List<ConsumerRecord<String, byte[]>> records : list) {

                                if (records.size() > 0) {
                                    BlockingQueue<List<ConsumerRecord<String, byte[]>>> recordQueue = this.topicRecordMap.get(((ConsumerRecord) records.get(0)).topic());
                                    if (null != recordQueue) {
                                        recordQueue.put(records);
                                    }
                                }
                            }
                        }
                    } catch (Throwable e) {
                        KafkaMessageConsumer.log.warn("Kafka poll message failed. topics=" + Arrays.toString(this.topics.toArray()), e);
                    }
                }
            }
            this.consumerWrapper.close();
        }


        public synchronized void destroy() {
            this.stopFlag.set(true);
            ThreadUtil.sleep(60L);
            this.topicRecordMap.clear();
            this.commitQueue.clear();
            this.topicRecordMap.clear();
            this.bindingMap.clear();
        }

        private class HandleRebalanceListener implements ConsumerRebalanceListener {
            private HandleRebalanceListener() {
            }

            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                if (null == partitions || partitions.size() == 0 || KafkaMessageConsumer.this.closeFlag.get())
                    return;
                synchronized (KafkaMessagePollThread.this.lock) {
                    for (Map.Entry<String, BlockingQueue<List<ConsumerRecord<String, byte[]>>>> entry : (Iterable<Map.Entry<String, BlockingQueue<List<ConsumerRecord<String, byte[]>>>>>) KafkaMessagePollThread.this.topicRecordMap.entrySet()) {
                        ((BlockingQueue) entry.getValue()).clear();
                    }
                    for (Map.Entry<String, Set<TopicPartition>> entry : getTopicPartition(partitions).entrySet()) {
                        ((KafkaConsumerBinding) KafkaMessagePollThread.this.bindingMap.get(entry.getKey())).onPartitionsRevoked();
                    }
                    try {
                        KafkaMessagePollThread.this.consumerWrapper.batchCommitOffset(KafkaMessagePollThread.this.commitQueue, 5000L);
                    } catch (InterruptedException interruptedException) {
                    }
                }
                KafkaMessageConsumer.log.info("Before rebalance, commit offset once. topic:" + partitions);
            }

            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                if (null == partitions || partitions.size() == 0 || KafkaMessageConsumer.this.closeFlag.get())
                    return;
                synchronized (KafkaMessagePollThread.this.lock) {
                    for (Map.Entry<String, Set<TopicPartition>> entry : getTopicPartition(partitions).entrySet()) {
                        ((KafkaConsumerBinding) KafkaMessagePollThread.this.bindingMap.get(entry.getKey())).onPartitionsAssigned(entry.getValue());
                    }
                }
            }

            private Map<String, Set<TopicPartition>> getTopicPartition(Collection<TopicPartition> partitions) {
                Map<String, Set<TopicPartition>> topicPartitionMap = new HashMap<>();
                for (TopicPartition partition : partitions) {
                    if (!topicPartitionMap.containsKey(partition.topic())) {
                        Set<TopicPartition> partitionSet = new HashSet<>();
                        partitionSet.add(partition);
                        topicPartitionMap.put(partition.topic(), partitionSet);
                        continue;
                    }
                    ((Set<TopicPartition>) topicPartitionMap.get(partition.topic())).add(partition);
                }

                return topicPartitionMap;
            }
        }
    }


    private synchronized void startCommandConsumer(String svrUrl) {

        if (this.perUrlPoolMap.containsKey(svrUrl)) {
            return;
        }
        this.perUrlPoolMap.put(svrUrl, new HashMap<>());

        ConsumerConfig consumerConfig = new ConsumerConfig(ConfigManager.getString("kafka.command.request.topic", "__consumer_command_request"));
        try {
            consumerConfig.setSvrUrl("kafka|" + svrUrl);
            consumerConfig.setPattern(MessageModelEnum.BROADCASTING);
            consumerConfig.setBatchSize(1);
            consumerConfig.setListener(new KafkaRewindCommandListener());
            consumerConfig.setSerializer(SerializerTypeEnum.STRING);
            MqClient.buildConsumer(consumerConfig);
            log.info("Kafka command consumer started with svrUrl:" + svrUrl);
        } catch (Throwable e) {
            log.error("Kafka command consumer init failed. {}", consumerConfig, e);
        }
    }

    private class KafkaRewindCommandListener
            implements MqMessageListener<String> {
        private KafkaRewindCommandListener() {
        }

        public AckAction onMessage(ReceiveRecord<String> record) {
            try {
                String[] route = record.getKey().split("~");
                String groupId = route[1];
                String topic = route[0];
                String key = StringUtil.join(":", new String[]{groupId, topic});
                if (route.length == 2 && KafkaMessageConsumer.this.perTopicPoolMap.size() > 0 && KafkaMessageConsumer.this.perTopicPoolMap.containsKey(key)) {
                    String command = ((String) record.getMessage()).split("~")[1];
                    KafkaMessagePollThread pollThread = (KafkaMessagePollThread) KafkaMessageConsumer.this.perTopicPoolMap.get(key);
                    pollThread.rewind(groupId, topic, command);
                }
            } catch (Throwable t) {
                KafkaMessageConsumer.log.error("Consume command failed. " + record, t);
            }
            return AckAction.Commit;
        }


        public boolean isRedeliver(ReceiveRecord record) {
            return false;
        }
    }
}


