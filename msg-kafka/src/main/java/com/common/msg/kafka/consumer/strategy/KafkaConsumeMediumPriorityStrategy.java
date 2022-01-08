package com.common.msg.kafka.consumer.strategy;

import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.util.ThreadUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


class KafkaConsumeMediumPriorityStrategy
        extends AbstractKafkaConsumeStrategy
        implements KafkaConsumeStrategy {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumeMediumPriorityStrategy.class);


    public void onMessage(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        tryConsume(config, record);
    }

    private void tryConsume(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        ReceiveRecord message = null;

        try {
            message = deserializerMessage(config, record);
        } catch (Throwable e) {
            log.error("Deserializer message failed, Discard it" + messageInfo(record), e);

            return;
        }

        int i = 0;
        while (i <= config.getRetries() && !Thread.currentThread().isInterrupted()) {
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
                } else if (i == config.getRetries()) {

                    log.error("Reconsume message failed " + i + "/" + config.getRetries() + " times, Discard it:" + message);
                } else {

                    log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message));
                    ThreadUtil.sleep((1000 * (i + 1)), log);
                }

            } catch (Throwable e) {
                if (i == 0) {
                    log.warn("Consume message failed, waiting for retry." + messageInfo(message), e);
                    ThreadUtil.sleep(1000L, log);
                } else if (i == config.getRetries()) {

                    log.error("Reconsume message failed " + i + "/" + config.getRetries() + " times, Discard it:" + message, e);
                } else {

                    log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message), e);
                    ThreadUtil.sleep((1000 * (i + 1)), log);
                }
            }
            i++;
        }
    }
}


