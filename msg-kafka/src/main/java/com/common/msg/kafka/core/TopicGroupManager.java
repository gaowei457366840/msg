package com.common.msg.kafka.core;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.util.StringUtil;
import com.common.msg.kafka.core.model.TopicGroupInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class TopicGroupManager {
    private static final Map<String, TopicGroupInfo> PRODUCER_GROUP = new ConcurrentHashMap<>();

    private static final Map<String, TopicGroupInfo> CONSUMER_GROUP = new ConcurrentHashMap<>();

    public static void addProducer(ProducerConfig config) {
        TopicGroupInfo info = new TopicGroupInfo();
        info.setTopic(config.getTopic());

        info.setGroupId(ConfigManager.getSarName(""));
        info.setProducerConfig(config);
        PRODUCER_GROUP.put(getKey(config.getGroupId(), config.getTopic()), info);
    }

    public static void addConsumer(ConsumerConfig config) {
        TopicGroupInfo info = new TopicGroupInfo();
        info.setTopic(config.getTopic());
        info.setConsumerConfig(config);
        info.setGroupId(config.getGroupId());
        CONSUMER_GROUP.put(getKey(config.getGroupId(), config.getTopic()), info);
    }

    public static TopicGroupInfo getProducerInfo(String groupId, String topic) {
        return PRODUCER_GROUP.get(getKey(groupId, topic));
    }

    public static TopicGroupInfo getConsumerInfo(String groupId, String topic) {
        return CONSUMER_GROUP.get(getKey(groupId, topic));
    }

    public static List<TopicGroupInfo> getUnconnected() {
        List<TopicGroupInfo> infos = new ArrayList<>();
        for (Map.Entry<String, TopicGroupInfo> entry : PRODUCER_GROUP.entrySet()) {
            if (!((TopicGroupInfo) entry.getValue()).isConnected()) {
                infos.add(entry.getValue());
            }
        }
        for (Map.Entry<String, TopicGroupInfo> entry : CONSUMER_GROUP.entrySet()) {
            if (!((TopicGroupInfo) entry.getValue()).isConnected()) {
                infos.add(entry.getValue());
            }
        }
        return infos;
    }

    public static void isConnected(String groupId, String topic) {
        String key = getKey(groupId, topic);
        TopicGroupInfo info = PRODUCER_GROUP.get(key);
        setConnected(info);
        info = CONSUMER_GROUP.get(key);
        setConnected(info);
    }

    private static void setConnected(TopicGroupInfo info) {
        if (info != null) {
            info.setConnected(true);
        }
    }

    private static String getKey(String groupId, String topic) {
        return StringUtil.join(":", new String[]{groupId, topic});
    }
}


