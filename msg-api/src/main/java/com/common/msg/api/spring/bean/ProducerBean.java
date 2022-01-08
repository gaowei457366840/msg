package com.common.msg.api.spring.bean;

import com.common.msg.api.MessageRecord;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.bootstrap.MqClient;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.SendCallback;
import com.common.msg.api.producer.SendResult;
import com.common.msg.api.transaction.TransactionChecker;
import com.common.msg.api.transaction.TransactionExecuter;

import java.util.Properties;
import javax.annotation.PostConstruct;

import org.springframework.util.StringUtils;


public class ProducerBean
        implements MqProducer {
    private Properties properties;
    private MqProducer producer;
    private String svrUrl;
    private String topic;
    private String groupId;
    private String priority;
    private String serializer;
    private TransactionChecker<?> checker;

    public ProducerBean(String topic) {
        this.topic = topic;
    }


    @PostConstruct
    public void startup() {
        String svrUrl = StringUtils.trimWhitespace(this.svrUrl);


        String topic = StringUtils.trimWhitespace(this.topic);
        if (StringUtil.isNullOrEmpty(topic)) {
            throw new MqConfigException("ProducerBean's [topic] is required.");
        }

        ProducerConfig config = new ProducerConfig(topic);
        config.setSvrUrl(svrUrl);


        if (!StringUtil.isNullOrEmpty(this.priority)) {
            MessagePriorityEnum priorityEnum = MessagePriorityEnum.getEnum(this.priority);
            if (priorityEnum.equals(MessagePriorityEnum.UNKNOWN)) {
                throw new MqConfigException("ProducerBean's [priority] is invalid. topic:" + topic);
            }
            config.setPriority(priorityEnum);
        }


        if (!StringUtil.isNullOrEmpty(this.groupId)) {
            config.setGroupId(this.groupId);
        }


        if (!StringUtil.isNullOrEmpty(this.serializer)) {
            SerializerTypeEnum serializerTypeEnum = SerializerTypeEnum.getEnum(StringUtils.trimWhitespace(this.serializer));
            if (serializerTypeEnum.equals(SerializerTypeEnum.UNKNOWN)) {
                throw new MqConfigException("ProducerBean's [serializer] is invalid. topic:" + topic);
            }
            config.setSerializer(serializerTypeEnum);
        }


        if (this.properties != null && this.properties.size() > 0) {
            for (String key : this.properties.stringPropertyNames()) {
                config.put(key, this.properties.getProperty(key));
            }
        }


        config.setChecker(this.checker);


        try {
            this.producer = MqClient.buildProducer(config);
        } catch (Throwable t) {
            throw new MqConfigException("Create producer failed. config:" + config, t);
        }
    }


    public void shutdown() {
    }


    public <V> SendResult send(MessageRecord<V> messageRecord) {
        return this.producer.send(messageRecord);
    }


    public <V> void sendAsync(MessageRecord<V> messageRecord, SendCallback callback) {
        this.producer.sendAsync(messageRecord, callback);
    }


    public <V> void sendOneway(MessageRecord<V> messageRecord) {
        this.producer.sendOneway(messageRecord);
    }


    public <V> SendResult send(MessageRecord<V> messageRecord, TransactionExecuter executer, Object arg) {
        return this.producer.send(messageRecord, executer, arg);
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSvrUrl() {
        return this.svrUrl;
    }

    public void setSvrUrl(String svrUrl) {
        this.svrUrl = svrUrl;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public TransactionChecker<?> getChecker() {
        return this.checker;
    }

    public void setChecker(TransactionChecker<?> checker) {
        this.checker = checker;
    }
}

