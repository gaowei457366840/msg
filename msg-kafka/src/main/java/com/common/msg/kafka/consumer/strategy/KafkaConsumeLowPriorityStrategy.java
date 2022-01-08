package com.common.msg.kafka.consumer.strategy;

import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class KafkaConsumeLowPriorityStrategy
        extends AbstractKafkaConsumeStrategy
        implements KafkaConsumeStrategy {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumeLowPriorityStrategy.class);


    public void onMessage(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        tryConsume(config, record);
    }


    private void tryConsume(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        try {
            ReceiveRecord message = deserializerMessage(config, record);
            innerOnMessage(config, message);
            if (AUDIT_SWITCH) {
                log.info("Consume" + messageInfo(message));
            }
        } catch (Throwable e) {
            log.warn("Consume message failed." + messageInfo(record), e);
        }
    }
}


