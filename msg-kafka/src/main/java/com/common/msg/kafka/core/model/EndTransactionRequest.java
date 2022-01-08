package com.common.msg.kafka.core.model;

import java.util.List;
import java.util.Objects;


public class EndTransactionRequest
        extends CommonRequest {
    private String topic;
    private String group;
    private List<EndTransactionMessage> endTransactionMessages;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof EndTransactionRequest)) return false;
        EndTransactionRequest other = (EndTransactionRequest) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        Object this$group = getGroup(), other$group = other.getGroup();
        if (!Objects.equals(this$group, other$group)) return false;
        Object this$endTransactionMessages = getEndTransactionMessages(), other$endTransactionMessages = (Object) other.getEndTransactionMessages();
        return Objects.equals(this$endTransactionMessages, other$endTransactionMessages);
    }

    protected boolean canEqual(Object other) {
        return other instanceof EndTransactionRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        Object $group = getGroup();
        result = result * 59 + (($group == null) ? 43 : $group.hashCode());
        Object $endTransactionMessages = getEndTransactionMessages();
        return result * 59 + (($endTransactionMessages == null) ? 43 : $endTransactionMessages.hashCode());
    }

    public String toString() {
        return "EndTransactionRequest(super=" + super.toString() + ", topic=" + getTopic() + ", group=" + getGroup() + ", endTransactionMessages=" + getEndTransactionMessages() + ")";
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setEndTransactionMessages(List<EndTransactionMessage> endTransactionMessages) {
        this.endTransactionMessages = endTransactionMessages;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getGroup() {
        return this.group;
    }

    public List<EndTransactionMessage> getEndTransactionMessages() {
        return this.endTransactionMessages;
    }

    public EndTransactionRequest() {
        setOneway(true);
    }
}

