package com.common.msg.kafka.core.model;

import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.producer.bingding.ProducerConfig;

import java.util.Objects;


@Deprecated
public class TopicGroupInfo {
    private String topic;
    private String groupId;
    @Deprecated
    private String producerGroup;
    @Deprecated
    private String consumerGroup;
    private ProducerConfig producerConfig;
    private ConsumerConfig consumerConfig;
    private transient boolean isConnected;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Deprecated
    public void setProducerGroup(String producerGroup) {
        this.producerGroup = producerGroup;
    }

    @Deprecated
    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public void setProducerConfig(ProducerConfig producerConfig) {
        this.producerConfig = producerConfig;
    }

    public void setConsumerConfig(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TopicGroupInfo)) return false;
        TopicGroupInfo other = (TopicGroupInfo) o;
        if (!other.canEqual(this)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        Object this$groupId = getGroupId(), other$groupId = other.getGroupId();
        if (!Objects.equals(this$groupId, other$groupId)) return false;
        Object this$producerGroup = getProducerGroup(), other$producerGroup = other.getProducerGroup();
        if (!Objects.equals(this$producerGroup, other$producerGroup))
            return false;
        Object this$consumerGroup = getConsumerGroup(), other$consumerGroup = other.getConsumerGroup();
        if (!Objects.equals(this$consumerGroup, other$consumerGroup))
            return false;
        Object this$producerConfig = getProducerConfig(), other$producerConfig = other.getProducerConfig();
        if (!Objects.equals(this$producerConfig, other$producerConfig))
            return false;
        Object this$consumerConfig = getConsumerConfig(), other$consumerConfig = other.getConsumerConfig();
        return Objects.equals(this$consumerConfig, other$consumerConfig);
    }

    protected boolean canEqual(Object other) {
        return other instanceof TopicGroupInfo;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        Object $groupId = getGroupId();
        result = result * 59 + (($groupId == null) ? 43 : $groupId.hashCode());
        Object $producerGroup = getProducerGroup();
        result = result * 59 + (($producerGroup == null) ? 43 : $producerGroup.hashCode());
        Object $consumerGroup = getConsumerGroup();
        result = result * 59 + (($consumerGroup == null) ? 43 : $consumerGroup.hashCode());
        Object $producerConfig = getProducerConfig();
        result = result * 59 + (($producerConfig == null) ? 43 : $producerConfig.hashCode());
        Object $consumerConfig = getConsumerConfig();
        return result * 59 + (($consumerConfig == null) ? 43 : $consumerConfig.hashCode());
    }

    public String toString() {
        return "TopicGroupInfo(topic=" + getTopic() + ", groupId=" + getGroupId() + ", producerGroup=" + getProducerGroup() + ", consumerGroup=" + getConsumerGroup() + ", producerConfig=" + getProducerConfig() + ", consumerConfig=" + getConsumerConfig() + ", isConnected=" + isConnected() + ")";
    }


    public String getTopic() {

        return this.topic;
    }

    public String getGroupId() {

        return this.groupId;
    }

    @Deprecated
    public String getProducerGroup() {

        return this.producerGroup;
    }

    @Deprecated
    public String getConsumerGroup() {

        return this.consumerGroup;
    }

    public ProducerConfig getProducerConfig() {

        return this.producerConfig;
    }

    public ConsumerConfig getConsumerConfig() {

        return this.consumerConfig;
    }

    public boolean isConnected() {

        return this.isConnected;
    }
}


