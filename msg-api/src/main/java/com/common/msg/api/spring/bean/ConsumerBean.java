package com.common.msg.api.spring.bean;

import com.common.msg.api.common.MessageModelEnum;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.bootstrap.MqClient;
import com.common.msg.api.consumer.MqConsumer;
import com.common.msg.api.consumer.MqMessageListener;
import com.common.msg.api.exception.MqConfigException;

import java.util.Properties;
import javax.annotation.PostConstruct;

import org.springframework.util.StringUtils;


public class ConsumerBean
        implements MqConsumer {
    private Properties properties;
    private final MqMessageListener<?> messageListener;
    private final String topic;
    private MqConsumer consumer;
    private String svrUrl;
    private String groupId;
    private String priority;
    private String serializer;
    private String tag;
    private String pattern;
    private int numThreads;
    private int batchSize;
    private int retries;

    public ConsumerBean(String topic, MqMessageListener<?> messageListener) {
        this.topic = topic;
        this.messageListener = messageListener;
    }


    @PostConstruct
    public void startup() {
        String svrUrl = StringUtils.trimWhitespace(this.svrUrl);


        String topic = StringUtils.trimWhitespace(this.topic);
        if (StringUtil.isNullOrEmpty(topic)) {
            throw new MqConfigException("ConsumerBean's [topic] is required.");
        }
        ConsumerConfig config = new ConsumerConfig(topic);
        config.setSvrUrl(svrUrl);


        if (!StringUtil.isNullOrEmpty(this.priority)) {
            MessagePriorityEnum priorityEnum = MessagePriorityEnum.getEnum(StringUtils.trimWhitespace(this.priority));
            if (priorityEnum.equals(MessagePriorityEnum.UNKNOWN)) {
                throw new MqConfigException("ConsumerBean's [priority] is invalid. topic:" + topic);
            }
            config.setPriority(priorityEnum);
        }


        if (!StringUtil.isNullOrEmpty(this.groupId)) {
            config.setGroupId(this.groupId);
        }


        if (!StringUtil.isNullOrEmpty(this.serializer)) {
            SerializerTypeEnum serializerTypeEnum = SerializerTypeEnum.getEnum(StringUtils.trimWhitespace(this.serializer));
            if (serializerTypeEnum.equals(SerializerTypeEnum.UNKNOWN)) {
                throw new MqConfigException("ConsumerBean's [serializer] is invalid. topic:" + topic);
            }
            config.setSerializer(serializerTypeEnum);
        }


        if (!StringUtil.isNullOrEmpty(this.pattern)) {
            MessageModelEnum messageModelEnum = MessageModelEnum.getEnum(StringUtils.trimWhitespace(this.pattern));
            if (null == messageModelEnum) {
                throw new MqConfigException("ConsumerBean's [pattern] is invalid. topic:" + topic);
            }
            config.setPattern(messageModelEnum);
        }


        if (this.properties != null && this.properties.size() > 0) {
            for (String key : this.properties.stringPropertyNames()) {
                config.put(key, this.properties.getProperty(key));
            }
        }


        String tag = StringUtils.trimWhitespace(this.tag);
        if (!StringUtil.isNullOrEmpty(tag)) {
            config.setTag(tag);
        }


        if (this.numThreads < 0)
            throw new MqConfigException("ConsumerBean's [numThreads] is invalid. topic:" + topic);
        if (this.numThreads > 0) {
            config.setNumThreads(this.numThreads);
        }


        if (this.batchSize < 0)
            throw new MqConfigException("ConsumerBean's [batchSize] is invalid. topic:" + topic);
        if (this.batchSize > 0) {
            config.setBatchSize(this.batchSize);
        }


        if (this.retries < 0)
            throw new MqConfigException("ConsumerBean's [retries] is invalid. topic:" + topic);
        if (this.retries > 0) {
            config.setRetries(this.retries);
        }


        config.setListener(this.messageListener);
        try {
            this.consumer = MqClient.buildConsumer(config);
        } catch (Throwable t) {
            throw new MqConfigException("Create consumer failed. config:" + config, t);
        }
    }


    public void shutdown() {
    }


    public String getTopic() {

        return this.topic;
    }

    public String getSvrUrl() {
        return this.svrUrl;
    }

    public void setSvrUrl(String svrUrl) {
        this.svrUrl = svrUrl;
    }

    public String getPriority() {
        return this.priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSerializer() {
        return this.serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public int getNumThreads() {
        return this.numThreads;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getRetries() {
        return this.retries;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public MqConsumer getConsumer() {
        return this.consumer;
    }


    public void unSubscribe() {
        this.consumer.unSubscribe();
    }
}

