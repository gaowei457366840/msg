package com.common.msg.kafka.consumer;

import com.common.msg.api.util.StringUtil;
import com.common.msg.api.consumer.binding.ConsumerConfig;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaMessageConsumerFactory {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageConsumerFactory.class);


    public void buildClientId(ConsumerConfig config) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(config.getType().getCode()).append(config.getSvrUrl()).append(config.getPattern().getCode()).append(config.getGroupId());

        if (config.getNumThreads() > 1) {
            buffer.append(config.getTopic());
        }
        if (null != config.getProps()) {
            TreeMap<Object, Object> sortMap = new TreeMap<>();


            for (Map.Entry<Object, Object> element : config.getProps().entrySet()) {
                sortMap.put(element.getKey(), element.getValue());
            }
            for (Map.Entry<Object, Object> element : sortMap.entrySet()) {
                buffer.append(element.getKey()).append(element.getValue());
            }
        }


        String clientId = config.getGroupId() + "_" + StringUtil.getMd5_16(buffer.toString());
        config.put("client.id", clientId);
        log.debug("Build clientId:" + buffer.toString() + " " + config);
    }


    KafkaConsumer<String, byte[]> getConsumer(ConsumerConfig config) {
        return createConsumer(config);
    }

    private synchronized KafkaConsumer<String, byte[]> createConsumer(ConsumerConfig config) {
        Properties props = new Properties();
        for (Map.Entry<Object, Object> element : config.getProps().entrySet()) {
            props.put(element.getKey(), element.getValue());
        }
        props.put("bootstrap.servers", config
                .getSvrUrl());
        props.put("group.id", config.getGroupId());


        props.put("enable.auto.commit",
                Boolean.valueOf(config.getBoolean("enable.auto.commit", false)));
        props.put("auto.commit.interval.ms",
                Integer.valueOf(config.getInt("auto.commit.interval.ms", 5000)));
        props.put("fetch.max.bytes",
                Integer.valueOf(config.getInt("fetch.max.bytes", 1048576)));
        props.put("session.timeout.ms",
                Integer.valueOf(config.getInt("session.timeout.ms", 30000)));
        props.put("heartbeat.interval.ms",
                Integer.valueOf(config.getInt("heartbeat.interval.ms", 10000)));
        props.put("max.poll.records",
                Integer.valueOf(config.getInt("max.poll.records", 512)));
        props.put("auto.offset.reset", config
                .getString("auto.offset.reset", "latest"));
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        props.put("value.deserializer", "org.apache.kafka.common.serialization.ByteArrayDeserializer");


        KafkaConsumer<String, byte[]> consumer = new KafkaConsumer(props);
        log.info("Create kafka consumer successful. config:" + props);
        return consumer;
    }
}


