package com.common.msg.api.consumer.binding;

import com.common.msg.api.common.MessageModelEnum;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.consumer.MqMessageListener;

import java.lang.reflect.Method;
import java.util.Objects;


public class ConsumerConfig
        extends MqConfig {
    public void setPriority(MessagePriorityEnum priority) {
        this.priority = priority;
    }

    public void setPattern(MessageModelEnum pattern) {
        this.pattern = pattern;
    }

    public void setListener(MqMessageListener listener) {
        this.listener = listener;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setNumThreads(int numThreads) {
        this.numThreads = numThreads;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ConsumerConfig)) return false;
        ConsumerConfig other = (ConsumerConfig) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$priority = getPriority(), other$priority = other.getPriority();
        if (!Objects.equals(this$priority, other$priority)) return false;
        Object this$pattern = getPattern(), other$pattern = other.getPattern();
        if (!Objects.equals(this$pattern, other$pattern)) return false;
        Object this$isRedeliver = getIsRedeliver(), other$isRedeliver = other.getIsRedeliver();
        if (!Objects.equals(this$isRedeliver, other$isRedeliver))
            return false;
        Object this$tag = getTag(), other$tag = other.getTag();
        return ((this$tag == null) ? (other$tag != null) : !this$tag.equals(other$tag)) ? false : ((getNumThreads() != other.getNumThreads()) ? false : ((getBatchSize() != other.getBatchSize()) ? false : (!(getRetries() != other.getRetries()))));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConsumerConfig;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $priority = getPriority();
        result = result * 59 + (($priority == null) ? 43 : $priority.hashCode());
        Object $pattern = getPattern();
        result = result * 59 + (($pattern == null) ? 43 : $pattern.hashCode());
        Object $isRedeliver = getIsRedeliver();
        result = result * 59 + (($isRedeliver == null) ? 43 : $isRedeliver.hashCode());
        Object $tag = getTag();
        result = result * 59 + (($tag == null) ? 43 : $tag.hashCode());
        result = result * 59 + getNumThreads();
        result = result * 59 + getBatchSize();
        return result * 59 + getRetries();
    }

    public String toString() {
        return "ConsumerConfig(super=" + super.toString() + ", priority=" + getPriority() + ", pattern=" + getPattern() + ", listener=" + getListener() + ", bean=" + getBean() + ", method=" + getMethod() + ", isRedeliver=" + getIsRedeliver() + ", tag=" + getTag() + ", numThreads=" + getNumThreads() + ", batchSize=" + getBatchSize() + ", retries=" + getRetries() + ")";
    }


    private MessagePriorityEnum priority = MessagePriorityEnum.MEDIUM;

    public MessagePriorityEnum getPriority() {
        return this.priority;
    }

    private transient MqMessageListener listener;
    private transient Object bean;
    private transient Method method;
    private MessageModelEnum pattern = MessageModelEnum.CLUSTERING;
    private String isRedeliver;
    private String tag;

    public MessageModelEnum getPattern() {
        return this.pattern;
    }

    public MqMessageListener getListener() {
        return this.listener;
    }

    void setBean(Object bean) {
        this.bean = bean;
    }

    Object getBean() {
        return this.bean;
    }

    void setMethod(Method method) {
        this.method = method;
    }

    Method getMethod() {
        return this.method;
    }

    void setIsRedeliver(String isRedeliver) {
        this.isRedeliver = isRedeliver;
    }

    String getIsRedeliver() {
        return this.isRedeliver;
    }

    public String getTag() {
        return this.tag;
    }

    private int numThreads = 1;

    public int getNumThreads() {
        return this.numThreads;
    }


    private int batchSize = 10;

    public int getBatchSize() {
        return this.batchSize;
    }


    private int retries = 3;

    public int getRetries() {
        return this.retries;
    }

    public ConsumerConfig(String topic) {
        setTopic(topic);
    }

    public ConsumerConfig(String topic, MqMessageListener listener) {
        this(topic);
        setListener(listener);
    }

    public ConsumerConfig(String topic, MqMessageListener listener, MessagePriorityEnum priority) {
        this(topic, listener);
        setPriority(priority);
    }

    public ConsumerConfig(String topic, MqMessageListener listener, MessagePriorityEnum priority, int numThreads, int batchSize) {
        this(topic, listener, priority);
        setNumThreads(numThreads);
        setBatchSize(batchSize);
    }

    public ConsumerConfig(String svrUrl, String topic, MqMessageListener listener) {
        this(topic);
        setSvrUrl(svrUrl);
        setListener(listener);
    }

    public ConsumerConfig(String svrUrl, String topic, MqMessageListener listener, MessagePriorityEnum priority) {
        this(topic, listener);
        setSvrUrl(svrUrl);
        setPriority(priority);
    }

    public ConsumerConfig(String svrUrl, String topic, MqMessageListener listener, MessagePriorityEnum priority, int numThreads, int batchSize) {
        this(topic, listener, priority);
        setSvrUrl(svrUrl);
        setNumThreads(numThreads);
        setBatchSize(batchSize);
    }

    public static class SystemPropKey {
        public static final String TEMPORARY = "__TEMPORARY";
    }
}


