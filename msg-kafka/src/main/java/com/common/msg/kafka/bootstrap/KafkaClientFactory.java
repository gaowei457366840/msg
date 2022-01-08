package com.common.msg.kafka.bootstrap;

import com.common.msg.api.common.MessageModelEnum;
import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.common.TopicPrivilegeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.event.EventAdviceService;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.storage.KeyValueStore;
import com.common.msg.api.storage.RocksDBStore;
import com.common.msg.api.storage.StoreContext;
import com.common.msg.api.storage.StoreEvent;
import com.common.msg.api.util.LocalHostUtil;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.consumer.KafkaMessageConsumer;
import com.common.msg.kafka.consumer.KafkaMessageConsumerFactory;
import com.common.msg.kafka.consumer.binding.KafkaConsumerBinding;
import com.common.msg.kafka.network.ClientNetworkService;
import com.common.msg.kafka.network.NetworkService;
import com.common.msg.kafka.producer.KafkaMessageProducer;
import com.common.msg.kafka.producer.KafkaMessageProducerFactory;
import com.common.msg.kafka.producer.binding.KafkaProducerBinding;
import com.common.msg.kafka.recorvery.PersistentEventRecoveryJob;
import com.common.msg.kafka.recorvery.PersistentEventService;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.bootstrap.MqFactory;
import com.common.msg.api.consumer.MqConsumer;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.kafka.core.MessageAccumulate;
import com.common.msg.kafka.core.SpecialMessageProducer;
import com.common.msg.kafka.core.dispatcher.ClientCommonDispatcher;
import com.common.msg.kafka.core.model.ServerMetadata;
import com.common.msg.kafka.core.model.TopicMetadata;
import com.common.msg.kafka.event.listener.KafkaEventListenerManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaClientFactory implements MqFactory {
    private static final Logger log = LoggerFactory.getLogger(KafkaClientFactory.class);
    /**
     * 生成者对应的存储
     * key configKey
     * value：KafkaProducerBinding 生产者包装
     */
    private static final ConcurrentMap<String, KafkaProducerBinding> PRODUCER_MAP = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, KafkaConsumerBinding> CONSUMER_MAP = new ConcurrentHashMap<>();

    private static final BlockingQueue<MqConfig> JOIN_QUEUE = new ArrayBlockingQueue<>(256);

    private static final Object LOCK_PRODUCER = new Object();

    private static final Object LOCK_CONSUMER = new Object();

    private ExecutorService joiner;

    private KafkaMessageProducer producer;

    private KafkaMessageConsumer consumer;

    private KafkaMessageProducerFactory producerFactory;

    private KafkaMessageConsumerFactory consumerFactory;

    private SpecialMessageProducer specialProducer;

    private KeyValueStore<StoreEvent, byte[]> store;

    private PersistentEventRecoveryJob recoveryJob;

    private MessageAccumulate messageAccumulate;

    private NetworkService clientNetwork;

    private ClientCommonDispatcher dispatcher;

    private boolean mqRouterSwitch;

    private volatile boolean isClose;


    public KafkaClientFactory() {
        String routerSwitch = ConfigManager.getString("mq.router.switch", "off");
        this.mqRouterSwitch = ("on".equalsIgnoreCase(routerSwitch) || "true".equalsIgnoreCase(routerSwitch));
        startup();
    }


    //创建生产者 DoubleClink
    public MqProducer buildProducer(ProducerConfig config) {
        String configKey = getConfigKey((MqConfig) config);
        //如果有对应的生产者，则 直接范湖i
        if (PRODUCER_MAP.containsKey(configKey)) {
            return (MqProducer) PRODUCER_MAP.get(configKey);
        }

        synchronized (LOCK_PRODUCER) {
            if (PRODUCER_MAP.containsKey(configKey)) {
                return (MqProducer) PRODUCER_MAP.get(configKey);
            }


            if (this.mqRouterSwitch) {
                config.setSvrUrl(null);
            }
            this.producerFactory.buildClientId(config);
            KafkaProducerBinding producer = new KafkaProducerBinding(config);
            PRODUCER_MAP.put(configKey, producer);
            if (this.mqRouterSwitch) {
                try {
                    JOIN_QUEUE.put(config);
                } catch (InterruptedException e) {
                    throw new MqClientException("Connect to server failed.", e);
                }
            }
            if (config.getChecker() != null) {
                this.clientNetwork.connect();
            }
            log.info("Binding kafka producer on config : " + config);
            return (MqProducer) producer;
        }
    }


    public MqConsumer buildConsumer(ConsumerConfig config) {
        String configKey = getConfigKey((MqConfig) config);
        if (CONSUMER_MAP.containsKey(configKey)) {
            throw new MqConfigException("Duplicated definition: Consumer(type='kafka', topic='" + config
                    .getTopic() + ", groupId=" + config.getGroupId() + "'.");
        }
        synchronized (LOCK_CONSUMER) {
            if (CONSUMER_MAP.containsKey(configKey)) {
                throw new MqConfigException("Duplicated definition: Consumer(type='kafka', topic='" + config
                        .getTopic() + ", groupId=" + config.getGroupId() + "'.");
            }
            if (this.mqRouterSwitch) {
                config.setSvrUrl(null);
            }
            this.consumerFactory.buildClientId(config);
            KafkaConsumerBinding consumer = new KafkaConsumerBinding(config);
            CONSUMER_MAP.put(configKey, consumer);
            if (this.mqRouterSwitch) {
                try {
                    JOIN_QUEUE.put(config);
                } catch (InterruptedException e) {
                    throw new MqClientException("Connect to server failed.", e);
                }
            } else {
                consumer.startup();
            }
            log.info("Binding kafka consumer on config : " + config);
            return (MqConsumer) consumer;
        }
    }


    public void transfer(MqConfig config) {
        if (config instanceof ProducerConfig) {
            for (Map.Entry<String, KafkaProducerBinding> entry : PRODUCER_MAP.entrySet()) {
                if (config.getTopic().equalsIgnoreCase(((KafkaProducerBinding) entry.getValue()).getConfig().getTopic()) &&
                        !((KafkaProducerBinding) entry.getValue()).getConfig().getSvrUrl().equalsIgnoreCase(config.getSvrUrl())) {

                    ((KafkaProducerBinding) entry.getValue()).getConfig().setSvrUrl(config.getSvrUrl());
                    try {
                        JOIN_QUEUE.put(config);
                    } catch (InterruptedException e) {
                        throw new MqClientException("Connect to server failed.", e);
                    }
                }
            }
        }

        if (config instanceof ConsumerConfig) {
            for (Map.Entry<String, KafkaConsumerBinding> entry : CONSUMER_MAP.entrySet()) {
                if (((KafkaConsumerBinding) entry.getValue()).getConfig().getTopic().equalsIgnoreCase(config.getTopic()) &&
                        !((KafkaConsumerBinding) entry.getValue()).getConfig().getSvrUrl().equalsIgnoreCase(config.getSvrUrl()) &&
                        StringUtil.isNullOrEmpty("__TEMPORARY")) {
                    KafkaConsumerBinding consumerBinding = entry.getValue();
                    if (null != consumerBinding) {

                        ConsumerConfig newConfig = cloneConsumerConfig(consumerBinding.getConfig());
                        newConfig.setSvrUrl(config.getSvrUrl());
                        KafkaConsumerBinding newBinding = new KafkaConsumerBinding(newConfig);
                        newBinding.startup();
                        CONSUMER_MAP.put(entry.getKey(), newBinding);


                        temporaryConsumer(consumerBinding);
                        consumerBinding.getConfig().getProps().remove("expireTime");
                        try {
                            JOIN_QUEUE.put(newConfig);
                        } catch (InterruptedException e) {
                            throw new MqClientException("Connect to server failed.", e);
                        }
                    }
                }
            }
        }
    }


    public MqTypeEnum getMqType() {
        return MqTypeEnum.KAFKA;
    }


    public ProducerConfig getProducerConfig(String topic, String groupId) {
        String key = getKey(topic, groupId);
        if (PRODUCER_MAP.containsKey(key)) {
            return ((KafkaProducerBinding) PRODUCER_MAP.get(key)).getConfig();
        }
        return null;
    }


    public ConsumerConfig getConsumerConfig(String topic, String groupId) {
        String configKey = getKey(topic, groupId);
        if (CONSUMER_MAP.containsKey(configKey)) {
            return ((KafkaConsumerBinding) CONSUMER_MAP.get(configKey)).getConfig();
        }
        return null;
    }


    public void startup() {
        try {
            String storeSwitch = ConfigManager.getString("mq.store.switch", "off");
            boolean isSwitch = ("on".equalsIgnoreCase(storeSwitch) || "true".equalsIgnoreCase(storeSwitch));

            if (isSwitch) {
                this.store = (KeyValueStore<StoreEvent, byte[]>) new RocksDBStore(ConfigManager.getSarName("") + ConfigManager.getString("instance.id", ""), StoreEvent.class, byte[].class);

                this.store.init(new StoreContext() {
                    public String applicationId() {

                        return null;
                    }

                    public File stateDir() {
                        return null;
                    }

                    public Map<String, Object> appConfigs() {
                        return null;
                    }
                });
                BeanHolder.addBean(KeyValueStore.class.getSimpleName(), this.store);
            }

            PersistentEventService persistentEventService = new PersistentEventService();
            BeanHolder.addBean(PersistentEventService.class.getSimpleName(), persistentEventService);

            //创建KafkaMessageProducer 生产者
            this.producerFactory = new KafkaMessageProducerFactory();
            this.producer = new KafkaMessageProducer(this.producerFactory);


            this.consumerFactory = new KafkaMessageConsumerFactory();
            this.consumer = new KafkaMessageConsumer(this.consumerFactory);
            BeanHolder.addBean(KafkaMessageConsumer.class.getSimpleName(), this.consumer);

            this.dispatcher = new ClientCommonDispatcher(this, this.consumer);
            this.clientNetwork = (NetworkService) new ClientNetworkService(this.dispatcher);
            this.messageAccumulate = new MessageAccumulate(this.clientNetwork);
            this.specialProducer = new SpecialMessageProducer(this.clientNetwork, this.messageAccumulate);
            BeanHolder.addBean(SpecialMessageProducer.class.getSimpleName(), this.specialProducer);

            EventAdviceService eventAdviceService = (EventAdviceService) BeanHolder.getBean(EventAdviceService.class.getSimpleName());

            //创建KafkaEventListenerManager
            KafkaEventListenerManager manager = new KafkaEventListenerManager(this.producer, this.specialProducer, eventAdviceService);


            if (isSwitch) {
                this.recoveryJob = new PersistentEventRecoveryJob();
                this.recoveryJob.startup();
            }

            if (this.mqRouterSwitch) {
                this.joiner = ThreadUtil.newSinglePool("joiner");
                this.joiner.submit(new Runnable() {

                    public void run() {
                        try {
                            while (!Thread.currentThread().isInterrupted() && !KafkaClientFactory.this.isClose) {
                                List<MqConfig> configList = new ArrayList<>();
                                try {
                                    MqConfig config = KafkaClientFactory.JOIN_QUEUE.poll(60L, TimeUnit.SECONDS);
                                    if (config != null) {
                                        configList.add(config);
                                        while (true) {
                                            config = KafkaClientFactory.JOIN_QUEUE.poll();
                                            if (config != null) {

                                                configList.add(config);
                                                continue;
                                            }
                                            break;
                                        }
                                    }
                                    if (!configList.isEmpty()) {
                                        KafkaClientFactory.this.clientNetwork.join(configList);
                                        KafkaClientFactory.this.completeJoin(configList);
                                        configList.clear();
                                    }
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                } catch (MqClientException e1) {
                                    KafkaClientFactory.log.warn("Join request to server failed.", (Throwable) e1);

                                    ThreadUtil.sleep(10000L, KafkaClientFactory.log);
                                    for (MqConfig configErr : configList) {
                                        KafkaClientFactory.JOIN_QUEUE.put(configErr);
                                    }
                                }
                            }
                        } catch (Throwable t) {
                            KafkaClientFactory.log.warn("Uncaught error in join request to server.", t);
                        }
                    }
                });
            }
        } catch (Throwable t) {
            log.error("Can't startup kafkaClientFactory.", t);
            throw new MqClientException(t);
        }
    }


    public void shutdown() {
        this.isClose = true;
        if (null != this.recoveryJob) {
            this.recoveryJob.shutdown();
        }
        for (Map.Entry<String, KafkaProducerBinding> entry : PRODUCER_MAP.entrySet()) {
            ((KafkaProducerBinding) entry.getValue()).shutdown();
        }
        PRODUCER_MAP.clear();

        for (Map.Entry<String, KafkaConsumerBinding> entry : CONSUMER_MAP.entrySet()) {
            ((KafkaConsumerBinding) entry.getValue()).shutdown();
        }
        CONSUMER_MAP.clear();
        if (null != this.producer) {
            this.producer.destroy();
        }
        if (null != this.clientNetwork) {
            this.clientNetwork.close();
        }
        if (null != this.messageAccumulate) {
            this.messageAccumulate.close();
        }
        if (null != this.consumer) {
            this.consumer.destroy();
        }
        if (null != this.store) {
            this.store.close();
        }
        JOIN_QUEUE.clear();
        if (this.joiner != null && !this.joiner.isShutdown()) {
            this.joiner.shutdownNow();
        }
    }

    public void setClientNetwork(NetworkService clientNetwork) {
        this.clientNetwork = clientNetwork;
    }

    public void setSpecialProducer(SpecialMessageProducer specialProducer) {
        this.specialProducer = specialProducer;
    }

    public static void awaitServerUrl(MqConfig config) {
        if (StringUtil.isNullOrEmpty(config.getSvrUrl())) {
            for (int n = 0; n < 3; n++) {
                for (int i = 0; i < 30; i++) {
                    ThreadUtil.sleep(1000L, log);
                    if (!StringUtil.isNullOrEmpty(config.getSvrUrl())) {
                        return;
                    }
                }
                log.warn("Get 'server url' from router server timeout over 30s for group[" + config.getGroupId() + " ] topic[" + config.getTopic() + "].");
            }
            throw new MqClientException("Get 'server url' from router server timeout over 90s for group[" + config.getGroupId() + " ] topic[" + config.getTopic() + "].");
        }
    }


    private void completeJoin(List<MqConfig> configList) {
        for (MqConfig config : configList) {
            TopicMetadata metadata = this.dispatcher.getMetadata(config.getGroupId(), config.getTopic());
            if (metadata == null) {
                try {
                    JOIN_QUEUE.put(config);
                } catch (InterruptedException interruptedException) {
                }

                continue;
            }
            if (metadata.getServers() == null || metadata.getServers().isEmpty()) {
                log.error("[Join server failed] not found topic information by groupId[{}] topic[{}] from router server.", config.getGroupId(), config.getTopic());
                continue;
            }
            if (metadata.getPrivilege() == TopicPrivilegeEnum.DENY.getCode()) {
                log.error("[Join server failed] groupId[{}] not authorized to access topic[{}].", config.getGroupId(), config.getTopic());
                continue;
            }
            if (StringUtil.isNullOrEmpty(((ServerMetadata) metadata.getServers().get(0)).getUrl())) {
                log.error("[Join server failed] maybe inner error happened. server url is null. groupId[{}] topic[{}]", config.getGroupId(), config.getTopic());
                continue;
            }
            if (config instanceof ProducerConfig) {
                if (metadata.getPrivilege() == TopicPrivilegeEnum.SUB.getCode()) {
                    log.error("[Join server failed] groupId[{}] not authorized to access topic[{}].", config.getGroupId(), config.getTopic());
                    continue;
                }
                config.setSvrUrl(((ServerMetadata) metadata.getServers().get(0)).getUrl());
            }
            if (config instanceof ConsumerConfig) {
                if (metadata.getPrivilege() == TopicPrivilegeEnum.PUB.getCode()) {
                    log.error("[Join server failed] groupId[{}] not authorized to access topic[{}].", config.getGroupId(), config.getTopic());
                    continue;
                }
                int num = 0;
                for (ServerMetadata serverMetadata : metadata.getServers()) {
                    if (num == 0) {
                        config.setSvrUrl(serverMetadata.getUrl());
                        ((KafkaConsumerBinding) CONSUMER_MAP.get(getKey(config.getTopic(), config.getGroupId()))).startup();
                    } else if (!StringUtil.isNullOrEmpty("__TEMPORARY")) {
                        ConsumerConfig newConfig = cloneConsumerConfig((ConsumerConfig) config);
                        newConfig.setSvrUrl(serverMetadata.getUrl());
                        KafkaConsumerBinding binding = new KafkaConsumerBinding(newConfig);

                        temporaryConsumer(binding);
                        binding.startup();
                    }

                    num++;
                }
            }
        }
    }


    private String getConfigKey(MqConfig config) {
        String configKey = getKey(config.getTopic(), getGroupId(config));
        if (config.getTopic().equals("__consumer_command_request")) {
            return configKey + config.getSvrUrl();
        }
        return configKey;
    }


    private String getGroupId(MqConfig config) {
        String str = config.getGroupId();
        if (StringUtil.isNullOrEmpty(str)) {
            str = ConfigManager.getSarName("");
        }

        if (config instanceof ConsumerConfig && ((ConsumerConfig) config).getPattern().equals(MessageModelEnum.BROADCASTING)) {
            String instanceId = ConfigManager.getString("instance.id", "");
            if (!StringUtil.isNullOrEmpty(instanceId)) {
                str = str + "-" + instanceId;
            } else {
                str = str + "-" + LocalHostUtil.getHostName();
            }
        }

        config.setGroupId(str);
        return str;
    }

    private String getKey(String groupId, String topic) {
        return StringUtil.join(":", new String[]{groupId, topic});
    }

    private ConsumerConfig cloneConsumerConfig(ConsumerConfig config) {
        ConsumerConfig newConfig = new ConsumerConfig(config.getTopic());
        newConfig.setGroupId(config.getGroupId());
        newConfig.setSerializer(config.getSerializer());
        if (null != config.getProps()) {
            for (Map.Entry<Object, Object> element : config.getProps().entrySet()) {
                newConfig.getProps().put(element.getKey(), element.getValue());
            }
        }
        newConfig.setSvrUrl(config.getSvrUrl());
        newConfig.setPriority(config.getPriority());
        newConfig.setPattern(config.getPattern());

        newConfig.setListener(config.getListener());

        ConsumerConfigAccessor.setBean(newConfig, ConsumerConfigAccessor.getBean(config));

        ConsumerConfigAccessor.setMethod(newConfig, ConsumerConfigAccessor.getMethod(config));

        ConsumerConfigAccessor.setIsRedeliver(newConfig, ConsumerConfigAccessor.getIsRedeliver(config));

        newConfig.setTag(config.getTag());

        newConfig.setNumThreads(config.getNumThreads());

        newConfig.setBatchSize(config.getBatchSize());

        newConfig.setRetries(config.getRetries());

        return newConfig;
    }

    private void temporaryConsumer(KafkaConsumerBinding binding) {

        ConsumerConfig config = binding.getConfig();

        config.put("__TEMPORARY", "true");

        config.put("__topic", config.getTopic());

        config.put("__groupId", config.getGroupId());


        CONSUMER_MAP.put(StringUtil.join(":", new String[]{config.getGroupId(), config.getTopic(), config.getSvrUrl()}), binding);
    }
}

