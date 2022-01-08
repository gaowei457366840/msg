package com.common.msg.kafka.event.listener;

import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.event.EventListener;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.storage.StoreEvent;
import com.common.msg.api.util.ProtobufUtil;
import com.common.msg.kafka.bootstrap.KafkaClientFactory;
import com.common.msg.kafka.consumer.ConsumerRecordWrapper;
import com.common.msg.kafka.consumer.strategy.AbstractKafkaConsumeStrategy;
import com.common.msg.kafka.event.KafkaConsumeEvent;
import com.common.msg.kafka.recorvery.PersistentEventService;
import com.common.msg.api.bootstrap.MqClient;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.exception.MqClientException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaConsumeEventListener
        extends AbstractKafkaConsumeStrategy
        implements EventListener<KafkaConsumeEvent> {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumeEventListener.class);


    private final PersistentEventService service = (PersistentEventService) BeanHolder.getBean(PersistentEventService.class.getSimpleName());


    public void listen(KafkaConsumeEvent event) {
        onMessage(event);
    }


    public String getIdentity() {
        return KafkaConsumeEvent.class.getSimpleName();
    }


    private void onMessage(KafkaConsumeEvent event) {

        ConsumerConfig config = getConsumerConfig(event);

        ReceiveRecord message = deserializer(config, event);

        try {
            if (!innerOnMessage(config, message).equals(AckAction.Commit)) {
                retry(event, message, (Throwable) null);
            } else if (AUDIT_SWITCH) {
                log.info("Consume" + messageInfo(message));
            }

        } catch (Throwable t) {
            retry(event, message, t);
        }
    }

    private ConsumerConfig getConsumerConfig(KafkaConsumeEvent event) {
        KafkaClientFactory clientFactory = (KafkaClientFactory) MqClient.getMqFactory(MqTypeEnum.KAFKA.getMemo());
        try {
            ConsumerConfig config;
            if (clientFactory != null) {
                config = clientFactory.getConsumerConfig(event.getTopic(), event.getGroupId());
            } else {
                throw new MqClientException("Kafka client not initialzation yet.");
            }
            if (config == null) {
                throw new MqClientException("Can not get consumer config. topic:" + event.getTopic() + ", groupId:" + event.getGroupId());
            }
            return config;
        } catch (Throwable t) {
            throw new MqClientException("Can not get consumer config. topic:" + event.getTopic() + ", groupId:" + event.getGroupId(), t);
        }
    }


    private ReceiveRecord deserializer(ConsumerConfig config, KafkaConsumeEvent event) {
        ConsumerRecordWrapper wrapper = (ConsumerRecordWrapper) ProtobufUtil.deserializer(event.getPayload(), ConsumerRecordWrapper.class);
        ConsumerRecord<String, byte[]> record = wrapper.getRecord();
        return deserializerMessage(config, record);
    }

    private void retry(KafkaConsumeEvent event, ReceiveRecord message, Throwable t) {
        if (event.getRetries() != event.getCurrentRetriedCount()) {
            if (null == t) {
                log.warn("Reconsume message failed " + event.getCurrentRetriedCount() + "/" + event.getRetries() + " times, waiting for retry." + messageInfo(message));
            } else {
                log.warn("Reconsume message failed " + event.getCurrentRetriedCount() + "/" + event.getRetries() + " times, waiting for retry." + messageInfo(message), t);
            }
        } else {
            log.error("Reconsume message failed " + event.getCurrentRetriedCount() + "/" + event.getRetries() + " times, Discard it:" + event, t);
        }
        this.service.retry((StoreEvent) event);
    }
}


