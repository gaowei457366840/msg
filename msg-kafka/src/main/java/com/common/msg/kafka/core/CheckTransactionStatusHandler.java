package com.common.msg.kafka.core;

import com.common.msg.api.MessageAccessor;
import com.common.msg.api.MessageRecord;
import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.util.ClassUtil;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.bootstrap.KafkaClientFactory;
import com.common.msg.kafka.core.model.CheckTransactionStatusRequest;
import com.common.msg.api.bootstrap.MqClient;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.serialization.Serializer;
import com.common.msg.api.transaction.TransactionStatus;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CheckTransactionStatusHandler {
    private static final Logger log = LoggerFactory.getLogger(CheckTransactionStatusHandler.class);


    private final ExecutorService executor = ThreadUtil.newPool("transaction-msg-check", 3, 5, 3000);
    private final boolean auditSwitch;
    private Map<String, ConsumerConfig> checkConfig = new ConcurrentHashMap<>();

    public CheckTransactionStatusHandler() {
        String auditSwitch = ConfigManager.getString("mq.special.message.audit.switch", "on");
        this.auditSwitch = ("on".equalsIgnoreCase(auditSwitch) || "true".equalsIgnoreCase(auditSwitch));
    }


    private volatile SpecialMessageProducer producer;


    public void handle(final CheckTransactionStatusRequest request, final InetSocketAddress node) {
        final ProducerConfig config;
        KafkaClientFactory kafkaClientFactory = (KafkaClientFactory) MqClient.getMqFactory(MqTypeEnum.KAFKA.getMemo());

        if (kafkaClientFactory != null) {
            config = kafkaClientFactory.getProducerConfig(request.getTopic(), request.getGroup());
        } else {
            log.warn("Check transaction status failed. not found ClientFactory for type KAFKA.");
            return;
        }
        if (config == null) {
            log.warn("Check transaction status failed. not found config for topic:" + request.getTopic());
            return;
        }
        if (this.producer == null) {
            synchronized (this) {
                if (this.producer == null) {
                    this.producer = (SpecialMessageProducer) BeanHolder.getBean(SpecialMessageProducer.class.getSimpleName());
                }
            }
        }
        for (MessageRecord<byte[]> record : (Iterable<MessageRecord<byte[]>>) request.getMessage()) {
            this.executor.submit(new Runnable() {
                public void run() {
                    try {
                        ReceiveRecord receiveRecord = CheckTransactionStatusHandler.this.deserializerMessage(CheckTransactionStatusHandler.this.getConfig(config), record);
                        TransactionStatus status = config.getChecker().check(receiveRecord);
                        CheckTransactionStatusHandler.this.producer.sendEndTransactionMessage(config, receiveRecord.getMsgId(), node, status, null);
                        if (CheckTransactionStatusHandler.this.auditSwitch) {
                            CheckTransactionStatusHandler.log.info("Check transaction status[{}] msgId[{}] key[{}] node[{}]", new Object[]{status, receiveRecord.getMsgId(), StringUtil.isNullOrEmpty(receiveRecord.getKey()) ? "" : receiveRecord.getKey(), node});
                        }

                        CheckTransactionStatusHandler.log.debug("Check transaction status[{}] msgId[{}] key[{}] node[{}]", new Object[]{status, receiveRecord.getMsgId(), StringUtil.isNullOrEmpty(receiveRecord.getKey()) ? "" : receiveRecord.getKey(), node});
                    } catch (Throwable t) {
                        Properties sysProperties = MessageAccessor.getSystemProperties(record);
                        if (sysProperties != null && !sysProperties.isEmpty()) {
                            String msgId = MessageAccessor.getSystemProperties(record).getProperty("__SID");
                            CheckTransactionStatusHandler.log.warn("Check transaction status failed. topic:{}, msgId:{}", new Object[]{request.getTopic(), msgId, t});
                            CheckTransactionStatusHandler.this.producer.sendEndTransactionMessage(config, msgId, node, null, t);
                        } else {
                            CheckTransactionStatusHandler.log.warn("Check transaction status failed with illegal record:{}, topic:{}", new Object[]{request.getTopic(), record, t});
                        }
                    }
                }
            });
        }
    }

    public void close() {
        if (this.executor != null) {
            this.executor.shutdownNow();
        }
    }


    private ReceiveRecord deserializerMessage(ConsumerConfig config, MessageRecord<byte[]> record) {
        Object data;
        try {
            data = deserializerMessage(config, (byte[]) record.getMessage());
        } catch (Throwable t) {
            throw new MqClientException("Deserializer message failed, topic:" + config.getTopic() + ", msgId:" +
                    MessageAccessor.getSystemProperties(record).getProperty("__SID") + ", config:" + config, t);
        }


        if (data == null) {
            throw new MqClientException("Deserializer message failed, topic:" + config.getTopic() + ", msgId:" +
                    MessageAccessor.getSystemProperties(record).getProperty("__SID") + ", config:" + config);
        }

        ReceiveRecord message = new ReceiveRecord();
        message.setUserProperties(record.getUserProperties());
        message.setTopic(config.getTopic());
        message.setMsgId(MessageAccessor.getSystemProperties(record).getProperty("__SID"));
        message.setKey(record.getKey());
        message.setMessage(data);
        MessageAccessor.setSystemProperties((MessageRecord) message, MessageAccessor.getSystemProperties(record));
        MessageAccessor.setSystemProperties((MessageRecord) message, MessageAccessor.getSystemProperties(record));
        return message;
    }

    private Object deserializerMessage(ConsumerConfig config, byte[] value) {
        Serializer serializer = (Serializer) ClassUtil.newInstance(config.getSerializer().getPath(), Serializer.class);
        return serializer.deserializer((MqConfig) config, value);
    }

    private ConsumerConfig getConfig(ProducerConfig config) {
        String key = StringUtil.join(":", new String[]{config.getGroupId(), config.getTopic()});
        if (this.checkConfig.containsKey(key)) {
            return this.checkConfig.get(key);
        }
        synchronized (CheckTransactionStatusHandler.class) {
            if (this.checkConfig.containsKey(key)) {
                return this.checkConfig.get(key);
            }
            ConsumerConfig consumerConfig = new ConsumerConfig(config.getTopic());
            consumerConfig.setGroupId(config.getGroupId());
            ConsumerConfigAccessor.setBean(consumerConfig, config.getChecker());
            ConsumerConfigAccessor.setMethod(consumerConfig, getMethod(config.getChecker()));
            consumerConfig.setSerializer(config.getSerializer());
            this.checkConfig.put(key, consumerConfig);
            return consumerConfig;
        }
    }


    private Method getMethod(Object bean) {
        Method method = null;
        for (Method m : bean.getClass().getMethods()) {
            if ("check".equalsIgnoreCase(m.getName())) {
                method = m;
                break;
            }
        }
        return method;
    }
}

