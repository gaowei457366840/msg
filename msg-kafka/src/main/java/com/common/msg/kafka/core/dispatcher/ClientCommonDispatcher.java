package com.common.msg.kafka.core.dispatcher;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.util.StringUtil;
import com.common.msg.kafka.bootstrap.KafkaClientFactory;
import com.common.msg.kafka.consumer.KafkaMessageConsumer;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.kafka.core.model.TopicMetadata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientCommonDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ClientCommonDispatcher.class);

    private final Map<String, TopicMetadata> topicMetadataMap;

    private final Map<String, Long> commandCacheMap;

    private final KafkaClientFactory factory;

    private final KafkaMessageConsumer consumer;


    public ClientCommonDispatcher(KafkaClientFactory factory, KafkaMessageConsumer consumer) {
        this.factory = factory;
        this.consumer = consumer;
        this.topicMetadataMap = new ConcurrentHashMap<>();
        this.commandCacheMap = new HashMap<>();
    }

    public void updateTopicMetadata(List<TopicMetadata> metadataList) {
        for (TopicMetadata metadata : metadataList) {
            this.topicMetadataMap.put(getKey(metadata), metadata);
        }
    }

    public TopicMetadata getMetadata(String groupId, String topic) {
        return this.topicMetadataMap.get(getKey(groupId, topic));
    }

    public void transfer(String content) {
        try {
            JSONObject jsonObject = JSON.parseObject(content);
            String topic = jsonObject.getString("topic");
            String srvUrl = jsonObject.getString("svrUrl");
            String expireTimeString = jsonObject.getString("expireTime");
            String id = jsonObject.getString("id");
            long expireTime = StringUtil.isNullOrEmpty(expireTimeString) ? 0L : Long.parseLong(expireTimeString);
            if (!isAbsent("transfer" + id)) {
                return;
            }

            ProducerConfig producerConfig = new ProducerConfig(topic);
            producerConfig.setSvrUrl(srvUrl);
            this.factory.transfer((MqConfig) producerConfig);


            ConsumerConfig consumerConfig = new ConsumerConfig(topic);
            consumerConfig.setSvrUrl(srvUrl);
            consumerConfig.getProps().put("expireTime", Long.valueOf(expireTime));
            this.factory.transfer((MqConfig) consumerConfig);
        } catch (Throwable t) {
            log.warn("Migrate failed from command content[{}]", content, t);
        }
    }

    public void rewind(String content) {
        try {
            JSONObject jsonObject = JSON.parseObject(content);
            String id = jsonObject.getString("id");
            if (!isAbsent("rewind" + id)) {
                return;
            }

            ConsumerConfig config = this.factory.getConsumerConfig(jsonObject.getString("groupId"), jsonObject.getString("topic"));
            this.consumer.rewind(config, jsonObject.getString("command"));
        } catch (Throwable t) {
            log.warn("Rewind failed from command content[{}]", content, t);
        }
    }

    private static String getKey(TopicMetadata metadata) {
        return StringUtil.join(":", new String[]{metadata.getGroupId(), metadata.getTopic()});
    }

    private static String getKey(String groupId, String topic) {
        return StringUtil.join(":", new String[]{groupId, topic});
    }


    private synchronized boolean isAbsent(String key) {
        boolean result = false;
        Long v = this.commandCacheMap.get(key);
        if (v == null) {
            v = this.commandCacheMap.put(key, Long.valueOf(MillisecondClock.now()));
            result = true;
        }
        clearKey();
        return result;
    }

    private void clearKey() {
        for (Map.Entry<String, Long> entry : this.commandCacheMap.entrySet()) {
            if (MillisecondClock.now() - ((Long) entry.getValue()).longValue() > 3600000L)
                this.commandCacheMap.remove(entry.getKey());
        }
    }
}


