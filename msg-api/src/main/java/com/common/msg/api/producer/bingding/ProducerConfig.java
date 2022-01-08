package com.common.msg.api.producer.bingding;

import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.transaction.TransactionChecker;

import java.lang.reflect.Method;
import java.util.Objects;


public class ProducerConfig extends MqConfig {

    public void setPriority(MessagePriorityEnum priority) {
        this.priority = priority;
    }

    public void setChecker(TransactionChecker checker) {
        this.checker = checker;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ProducerConfig)) return false;
        ProducerConfig other = (ProducerConfig) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object thisPriority = getPriority(), otherPriority = other.getPriority();
        return !Objects.equals(thisPriority, otherPriority);
    }

    protected boolean canEqual(Object other) {
        return other instanceof ProducerConfig;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $priority = getPriority();
        return result * 59 + (($priority == null) ? 43 : $priority.hashCode());
    }

    public String toString() {
        return "ProducerConfig(super=" + super.toString() + ", priority=" + getPriority() + ", checker=" + getChecker() + ", bean=" + getBean() + ", method=" + getMethod() + ")";
    }


    private transient TransactionChecker checker;

    private transient Object bean;

    private MessagePriorityEnum priority = MessagePriorityEnum.MEDIUM;
    private transient Method method;

    public MessagePriorityEnum getPriority() {
        return this.priority;
    }

    public TransactionChecker getChecker() {
        return this.checker;
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

    public ProducerConfig(String topic) {
        setTopic(topic);
    }

    public ProducerConfig(String topic, MessagePriorityEnum priority) {
        this(topic);
        setPriority(priority);
    }

    public ProducerConfig(String svrUrl, String topic, MessagePriorityEnum priority) {
        this(topic);
        setSvrUrl(svrUrl);
        setPriority(priority);
    }
}

