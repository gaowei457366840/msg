package com.common.msg.kafka.consumer.strategy;

import com.common.msg.api.common.RetryTimeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.storage.StoreEvent;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.event.KafkaConsumeEvent;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.util.ProtobufUtil;
import com.common.msg.kafka.consumer.ConsumerRecordWrapper;
import com.common.msg.kafka.recorvery.PersistentEventService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class KafkaConsumeHighStrategy
        extends AbstractKafkaConsumeStrategy
        implements KafkaConsumeStrategy {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumeHighStrategy.class);


    private final PersistentEventService service = (PersistentEventService) BeanHolder.getBean(PersistentEventService.class.getSimpleName());

    KafkaConsumeHighStrategy() {
        String storeSwitch = ConfigManager.getString("mq.store.switch", "off");
        this.isPersist = ("on".equalsIgnoreCase(storeSwitch) || "true".equalsIgnoreCase(storeSwitch));
    }

    private final boolean isPersist;

    public void onMessage(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        tryConsume(config, record);
    }


    private void tryConsume(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        ReceiveRecord message;
        try {
            message = deserializerMessage(config, record);
        } catch (Throwable e) {
            log.error("Deserializer message failed, Discard it" + messageInfo(record), e);

            return;
        }

        int i = 0;
        int retries = 1;
        if (!this.isPersist) {
            retries = config.getRetries();
        }
        while (i <= retries && !Thread.currentThread().isInterrupted()) {
            try {
                if (innerOnMessage(config, message).equals(AckAction.Commit)) {
                    if (AUDIT_SWITCH) {
                        log.info("Consume" + messageInfo(message));
                    }
                    break;
                }
                if (i == 0) {
                    log.warn("Consume message failed, waiting for retry." + messageInfo(message));
                    ThreadUtil.sleep(1000L, log);
                } else if (i == retries) {
                    if (this.isPersist) {

                        log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message));
                        persist(config, record, message);
                    } else {

                        log.error("Reconsume message failed " + i + "/" + config.getRetries() + " times, Discard it:" + message);
                    }
                } else {
                    log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message));
                    ThreadUtil.sleep((1000 * (i + 1)), log);
                }

            } catch (Throwable e) {
                if (i == 0) {
                    log.warn("Consume message failed, waiting for retry." + messageInfo(message), e);
                    ThreadUtil.sleep(1000L, log);
                } else if (i == retries) {
                    if (this.isPersist) {

                        log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message), e);
                        persist(config, record, message);
                    } else {

                        log.error("Reconsume message failed " + i + "/" + config.getRetries() + " times, Discard it:" + message, e);
                    }
                } else {

                    log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message), e);
                    ThreadUtil.sleep((1000 * (i + 1)), log);
                }
            }

            i++;
        }
    }


    private void persist(ConsumerConfig config, ConsumerRecord<String, byte[]> record, ReceiveRecord message) {
        if (!this.isPersist)
            return;
        StoreEvent event = buildEvent(config, record);

        int i = 1;
        while (i <= 3 && !Thread.currentThread().isInterrupted()) {
            if (this.service == null) {
                log.error("Can't Persist message for reconsume, because KeyValueStore is null, discard this message:" + message);

                break;
            }
            try {
                event.setPayload(ProtobufUtil.serializer(new ConsumerRecordWrapper(record)));
                this.service.save(event);
                break;
            } catch (Throwable e) {
                if (i == 3) {
                    log.error("Persist message failed 3/3 times, Discard it:" + message, e);
                } else {
                    log.warn("Persist message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message), e);
                }

                ThreadUtil.sleep((1000 * i), log);
                i++;
            }
        }
    }


    private StoreEvent buildEvent(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        StoreEvent event = new StoreEvent();

        event.setType(KafkaConsumeEvent.class.getName());

        event.setTopic(record.topic());

        event.setGroupId(config.getGroupId());

        event.setRetries(config.getRetries());

        event.setNextRetryTime(MillisecondClock.now() + RetryTimeEnum.getMemo(RetryTimeEnum.TWO.getCode()));

        event.setCurrentRetriedCount(RetryTimeEnum.TWO.getCode());

        event.setMsgId(StringUtil.buildMessageId(record.partition(), record.offset()));
        return event;
    }
}

