package com.common.msg.kafka.core.model;

import com.common.msg.api.MessageRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class CheckTransactionStatusRequest {
    private String group;
    private String topic;
    private List<MessageRecord<byte[]>> message;

    public void setGroup(String group) {

        this.group = group;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setMessage(List<MessageRecord<byte[]>> message) {
        this.message = message;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CheckTransactionStatusRequest)) return false;
        CheckTransactionStatusRequest other = (CheckTransactionStatusRequest) o;
        if (!other.canEqual(this)) return false;
        Object this$group = getGroup(), other$group = other.getGroup();
        if (!Objects.equals(this$group, other$group)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        List<MessageRecord<byte[]>> this$message = (List<MessageRecord<byte[]>>) getMessage(), other$message = (List<MessageRecord<byte[]>>) other.getMessage();
        return !(!Objects.equals(this$message, other$message));
    }

    protected boolean canEqual(Object other) {
        return other instanceof CheckTransactionStatusRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $group = getGroup();
        result = result * 59 + (($group == null) ? 43 : $group.hashCode());
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        List<MessageRecord<byte[]>> $message = (List<MessageRecord<byte[]>>) getMessage();
        return result * 59 + (($message == null) ? 43 : $message.hashCode());
    }

    public String toString() {
        return "CheckTransactionStatusRequest(group=" + getGroup() + ", topic=" + getTopic() + ", message=" + getMessage() + ")";
    }

    public String getGroup() {
        return this.group;
    }

    public String getTopic() {
        return this.topic;
    }

    public List<MessageRecord<byte[]>> getMessage() {
        return this.message;
    }

    public CheckTransactionStatusRequest() {
    }

    public CheckTransactionStatusRequest(String group, String topic, int size) {

        this.group = group;

        this.topic = topic;

        this.message = new ArrayList<>(size);
    }
}


