package com.common.msg.kafka.core.model;

import com.common.msg.api.MessageRecord;

import java.util.List;
import java.util.Objects;


public class SendMessageRequest
        extends CommonRequest {
    private String group;
    private String topic;
    private int length;
    private List<MessageRecord<byte[]>> messages;

    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof SendMessageRequest)) return false;
        SendMessageRequest other = (SendMessageRequest) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$group = getGroup(), other$group = other.getGroup();
        if (!Objects.equals(this$group, other$group)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        if (getLength() != other.getLength()) return false;
        Object this$messages = (Object) getMessages(), other$messages = (Object) other.getMessages();
        return !(!Objects.equals(this$messages, other$messages));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SendMessageRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $group = getGroup();
        result = result * 59 + (($group == null) ? 43 : $group.hashCode());
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        result = result * 59 + getLength();
        Object $messages = (Object) getMessages();
        return result * 59 + (($messages == null) ? 43 : $messages.hashCode());
    }


    public String toString() {
        return "SendMessageRequest(super=" + super.toString() + ", group=" + getGroup() + ", topic=" + getTopic() + ", length=" + getLength() + ", messages=" + getMessages() + ")";
    }


    public void setGroup(String group) {
        this.group = group;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setMessages(List<MessageRecord<byte[]>> messages) {
        this.messages = messages;
    }

    public String getGroup() {

        return this.group;
    }

    public String getTopic() {

        return this.topic;
    }

    public int getLength() {

        return this.length;
    }

    public List<MessageRecord<byte[]>> getMessages() {

        return this.messages;
    }
}

