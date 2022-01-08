package com.common.msg.api.producer;

import com.common.msg.api.MessageRecord;
import com.common.msg.api.exception.MqClientException;

import java.util.Objects;


public class OnExceptionContext<T> {
    private MessageRecord<T> messageRecord;
    private String topic;
    private MqClientException exception;

    public void setMessageRecord(MessageRecord<T> messageRecord) {
        this.messageRecord = messageRecord;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setException(MqClientException exception) {
        this.exception = exception;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof OnExceptionContext)) return false;
        OnExceptionContext<?> other = (OnExceptionContext) o;
        if (!other.canEqual(this)) return false;
        Object this$messageRecord = (Object) getMessageRecord();
        Object other$messageRecord = (Object) other.getMessageRecord();
        if (!Objects.equals(this$messageRecord, other$messageRecord))
            return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        Object this$exception = getException(), other$exception = other.getException();
        return !(!Objects.equals(this$exception, other$exception));
    }

    protected boolean canEqual(Object other) {
        return other instanceof OnExceptionContext;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $messageRecord = (Object) getMessageRecord();
        result = result * 59 + (($messageRecord == null) ? 43 : $messageRecord.hashCode());
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        Object $exception = getException();
        return result * 59 + (($exception == null) ? 43 : $exception.hashCode());
    }

    public String toString() {
        return "OnExceptionContext(messageRecord=" + getMessageRecord() + ", topic=" + getTopic() + ", exception=" + getException() + ")";
    }


    public MessageRecord<T> getMessageRecord() {
        return this.messageRecord;
    }


    public String getTopic() {
        return this.topic;
    }


    public MqClientException getException() {
        return this.exception;
    }

    public OnExceptionContext(String topic, MessageRecord<T> messageRecord, MqClientException exception) {
        this.topic = topic;
        this.messageRecord = messageRecord;
        this.exception = exception;
    }
}
