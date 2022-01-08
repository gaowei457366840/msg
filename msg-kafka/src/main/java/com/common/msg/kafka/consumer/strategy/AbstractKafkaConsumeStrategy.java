package com.common.msg.kafka.consumer.strategy;

import com.alibaba.fastjson.JSON;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.util.ClassUtil;
import com.common.msg.api.MessageAccessor;
import com.common.msg.api.MessageRecord;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.strategy.AbstractConsumeStrategy;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.serialization.Serializer;
import com.common.msg.api.util.StringUtil;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AbstractKafkaConsumeStrategy
        extends AbstractConsumeStrategy {
    private static final Logger log = LoggerFactory.getLogger(AbstractKafkaConsumeStrategy.class);


    private static final boolean USER_HEAD = (ConfigManager.getString("kafka.server.version", "0.11.0.2").compareTo("0.11") >= 0);

    private static final String S_AUDITS_WITCH = ConfigManager.getString("kafka.message.audit.switch", "on");

    protected static final boolean AUDIT_SWITCH = ("on".equalsIgnoreCase(S_AUDITS_WITCH) || "true".equalsIgnoreCase(S_AUDITS_WITCH));


    protected ReceiveRecord deserializerMessage(ConsumerConfig config, ConsumerRecord<String, byte[]> record) {
        Object data;
        try {
            data = deserializerMessage(config, (byte[]) record.value());
        } catch (Throwable t) {
            throw new MqClientException("Deserializer message failed, topic:" + record.topic() + ", partition:" + record
                    .partition() + ", offset:" + record
                    .offset() + ", config:" + config, t);
        }


        if (data == null) {
            throw new MqClientException("Deserializer message failed, topic:" + record.topic() + ", partition:" + record
                    .partition() + ", offset:" + record
                    .offset() + ", config:" + config);
        }

        ReceiveRecord message = new ReceiveRecord();
        message.setTopic(record.topic());
        message.setKey((String) record.key());
        message.setTimestamp(record.timestamp());
        message.setPartition(record.partition());
        message.setOffset(record.offset());
        message.setMessage(data);
        message.setMsgId(StringUtil.buildMessageId(message.getPartition(), message.getOffset()));
        if (USER_HEAD) {
            for (Header header : record.headers()) {
                if ("userProps".equals(header.key())) {
                    Properties userProperties = (Properties) JSON.parseObject(header.value(), Properties.class, new com.alibaba.fastjson.parser.Feature[0]);
                    message.setUserProperties(userProperties);
                }

                if ("sysProps".equals(header.key())) {
                    Properties systemProperties = (Properties) JSON.parseObject(header.value(), Properties.class, new com.alibaba.fastjson.parser.Feature[0]);
                    MessageAccessor.setSystemProperties((MessageRecord) message, systemProperties);
                }
            }
        }
        return message;
    }


    protected AckAction innerOnMessage(ConsumerConfig config, ReceiveRecord message) {
        AckAction action;
        if (!isRedeliver(config, message)) {

            action = onMessage(config, message);
        } else {
            action = AckAction.Commit;
            log.info("Discard redelivered" + messageInfo(message));
        }
        return action;
    }


    private Object deserializerMessage(ConsumerConfig config, byte[] value) {
        Serializer serializer = (Serializer) ClassUtil.newInstance(config.getSerializer().getPath(), Serializer.class);
        return serializer.deserializer((MqConfig) config, value);
    }

    String messageInfo(ConsumerRecord<String, byte[]> record) {
        StringBuilder builder = new StringBuilder();
        builder.append(" record:topic[").append(record.topic()).append("]");
        builder.append(" partition[").append(record.partition()).append("]");
        builder.append(" offset[").append(record.offset()).append("]");
        builder.append(" key[").append(StringUtil.isNullOrEmpty((String) record.key()) ? "" : (String) record.key()).append("]");
        return builder.toString();
    }
}


