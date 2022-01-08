package com.common.msg.kafka.producer.binding;

import com.common.msg.api.MessageAccessor;
import com.common.msg.api.MessageRecord;
import com.common.msg.api.common.SendTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.event.Event;
import com.common.msg.api.event.EventBusFactory;
import com.common.msg.api.util.ClassUtil;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.SendCallback;
import com.common.msg.api.producer.SendResult;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.serialization.Serializer;
import com.common.msg.api.transaction.TransactionExecuter;
import com.common.msg.kafka.bootstrap.KafkaClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProducerBinding implements MqProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerBinding.class);

    private final ProducerConfig config;

    private final Serializer serializer;

    public KafkaProducerBinding(ProducerConfig config) {
        this.config = config;

        this.serializer = (Serializer) ClassUtil.newInstance(config.getSerializer().getPath(), Serializer.class);
        //设置每个消息的最大值
        this.serializer.setMaxSize(ConfigManager.getInt("mq.message.maxsize", 1048576));
    }


    public ProducerConfig getConfig() {
        return this.config;
    }


    public <V> SendResult send(MessageRecord<V> messageRecord) {
        return innerSend(messageRecord, null, SendTypeEnum.SYCN, null, null);
    }


    public <V> void sendAsync(MessageRecord<V> messageRecord, SendCallback callback) {
        innerSend(messageRecord, callback, SendTypeEnum.ASYCN, null, null);
    }


    public <V> void sendOneway(MessageRecord<V> messageRecord) {
        innerSend(messageRecord, null, SendTypeEnum.ONEWAY, null, null);
    }


    public <V> SendResult send(MessageRecord<V> messageRecord, TransactionExecuter executer, Object arg) {
        if (executer == null || this.config.getChecker() == null) {
            throw new MqConfigException("Not support send transaction message without config [TransactionExecuter] or [TransactionChecker].");
        }
        MessageAccessor.getSystemProperties(messageRecord).put("__TXMSG", "");
        return innerSend(messageRecord, null, SendTypeEnum.SYCN, executer, arg);
    }


    public void startup() {
    }


    public void shutdown() {
    }

    /**
     * 维护通过 KafkaProduceEvent.class,来找到 对应的 SynEventBus
     * 在SynEventBus 中维护了
     * @param record
     * @param callback
     * @param sendType
     * @param executer
     * @param arg
     * @param <V>
     * @return
     */
    private <V> SendResult innerSend(MessageRecord<V> record, SendCallback callback, SendTypeEnum sendType, TransactionExecuter executer, Object arg) {
        KafkaProduceEvent event;
        if (record == null || record.getMessage() == null) {
            throw new MqClientException("Message can't be null or blank");
        }
        try {
            event = buildEvent(record, callback, sendType, executer, arg);

            KafkaClientFactory.awaitServerUrl((MqConfig) this.config);

            EventBusFactory.getInstance().post((Event) event);
        } catch (Throwable t) {
            throw new MqClientException("Send message failed. message:" + record, t);
        }
        return event.getResult();
    }


    private <V> KafkaProduceEvent buildEvent(MessageRecord<V> record, SendCallback callback, SendTypeEnum sendType, TransactionExecuter executer, Object arg) {

        KafkaProduceEvent<V> event = new KafkaProduceEvent();

        event.setMessageRecord(record);

        event.setConfig((MqConfig) this.config);

        event.setKey(record.getKey());

        event.setPayload(this.serializer.serializer(record.getMessage()));

        event.setCallback(callback);

        event.setSendType(sendType);

        event.setExecuter(executer);

        event.setArg(arg);

        return event;
    }
}


