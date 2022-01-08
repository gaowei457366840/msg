package com.common.msg.kafka.producer;

import com.alibaba.fastjson.JSON;
import com.common.msg.api.MessageAccessor;
import com.common.msg.api.common.SendTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.util.StringUtil;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.api.bootstrap.Destroyable;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.producer.OnExceptionContext;
import com.common.msg.api.producer.SendResult;
import com.common.msg.api.producer.bingding.ProducerConfig;

import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 对  KafkaMessageProducerFactory
 */
public class KafkaMessageProducer implements Destroyable {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageProducer.class);

    private static final int TIMEOUT = ConfigManager.getInt("kafka.producer.timeout", 20);
    private static final boolean USER_HEAD = (ConfigManager.getString("kafka.server.version", "0.11.0.2").compareTo("0.11") >= 0);
    private final KafkaMessageProducerFactory factory;

    public KafkaMessageProducer(KafkaMessageProducerFactory factory) {
        this.factory = factory;
    }

    public void send(KafkaProduceEvent event) {
        ProducerConfig config = event.getConfig();
        try {
            ProducerRecord<String, byte[]> producerRecord;
            if (StringUtil.isNullOrEmpty(event.getKey())) {
                producerRecord = new ProducerRecord(config.getTopic(), event.getPayload());
            } else {
                producerRecord = new ProducerRecord(config.getTopic(), event.getKey(), event.getPayload());
            }
            if (USER_HEAD) {
                Properties userProperties = event.getMessageRecord().getUserProperties();
                Properties systemProperties = MessageAccessor.getSystemProperties(event.getMessageRecord());
                if (null != userProperties && userProperties.size() > 0) {
                    producerRecord.headers().add("userProps", JSON.toJSONBytes(userProperties, new com.alibaba.fastjson.serializer.SerializerFeature[0]));
                }
                if (null != systemProperties && systemProperties.size() > 0) {
                    producerRecord.headers().add("sysProps", JSON.toJSONBytes(systemProperties, new com.alibaba.fastjson.serializer.SerializerFeature[0]));
                }
            }

            if (SendTypeEnum.SYCN.equals(event.getSendType())) {
                SendResult result = convertToResult(synSend(event, producerRecord), (String) producerRecord.key());
                event.setResult(result);
            } else {
                asynSend(event, producerRecord);
            }

        } catch (Throwable e) {

            throw new MqClientException("Kafka send message failed: " + event, e);
        }
    }


    public void destroy() {

        this.factory.destroy();
    }


    private RecordMetadata synSend(KafkaProduceEvent event, ProducerRecord<String, byte[]> producerRecord) throws Throwable {

        Future<RecordMetadata> future = this.factory.getProducer(event.getConfig()).send(producerRecord);

        RecordMetadata metadata = future.get(TIMEOUT, TimeUnit.SECONDS);

        log.debug("Kafka synchronous send to topic:" + event.getConfig().getTopic() + ", partition:" + metadata.partition() + ", message:" + event.getMessageRecord());

        return metadata;
    }


    /**
     * 发送消息
     * @param event
     * @param producerRecord
     */
    private void asynSend(final KafkaProduceEvent event, final ProducerRecord<String, byte[]> producerRecord) {

        if (event.getCallback() != null) {
            this.factory.getProducer(event.getConfig()).send(producerRecord, new Callback() {
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (null == exception) {
                        event.getCallback().onSuccess(KafkaMessageProducer.this.convertToResult(metadata, (String) producerRecord.key()));
                    } else {
                        event.getCallback().onException(new OnExceptionContext(event.getConfig().getTopic(), event.getMessageRecord(), new MqClientException(exception)));
                    }
                }
            });
        } else {
            this.factory.getProducer(event.getConfig()).send(producerRecord, null);
        }

        log.debug("Kafka asynchronous send to topic:" + event.getConfig().getTopic() + ", message:" + event.getMessageRecord());
    }

    private SendResult convertToResult(RecordMetadata recordMetadata, String key) {

        if (recordMetadata == null) return null;

        SendResult sendResult = new SendResult();

        sendResult.setTopic(recordMetadata.topic());

        if (recordMetadata.partition() != -1) {

            sendResult.setPartition(recordMetadata.partition());

            sendResult.setOffset(recordMetadata.offset());

            sendResult.setTimestamp(recordMetadata.timestamp());

            sendResult.setKey(key);

            sendResult.setMsgId(StringUtil.buildMessageId(recordMetadata.partition(), recordMetadata.offset()));
        }

        return sendResult;
    }
}


