package com.common.msg.api.producer;


import java.util.Objects;

public class SendResult {
    private String topic;
    private String key;
    private int partition;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SendResult)) return false;
        SendResult other = (SendResult) o;
        if (!other.canEqual(this)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        Object this$key = getKey(), other$key = other.getKey();
        if (!Objects.equals(this$key, other$key)) return false;
        if (getPartition() != other.getPartition()) return false;
        if (getOffset() != other.getOffset()) return false;
        if (getTimestamp() != other.getTimestamp()) return false;
        Object this$msgId = getMsgId(), other$msgId = other.getMsgId();
        return !(!Objects.equals(this$msgId, other$msgId));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SendResult;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        Object $key = getKey();
        result = result * 59 + (($key == null) ? 43 : $key.hashCode());
        result = result * 59 + getPartition();
        long $offset = getOffset();
        result = result * 59 + (int) ($offset >>> 32L ^ $offset);
        long $timestamp = getTimestamp();
        result = result * 59 + (int) ($timestamp >>> 32L ^ $timestamp);
        Object $msgId = getMsgId();
        return result * 59 + (($msgId == null) ? 43 : $msgId.hashCode());
    }

    public String toString() {
        return "SendResult(topic=" + getTopic() + ", key=" + getKey() + ", partition=" + getPartition() + ", offset=" + getOffset() + ", timestamp=" + getTimestamp() + ", msgId=" + getMsgId() + ")";
    }


    public String getTopic() {
        return this.topic;
    }

    public String getKey() {
        return this.key;
    }

    public int getPartition() {
        return this.partition;
    }

    private long offset = -1L;

    public long getOffset() {
        return this.offset;
    }


    private long timestamp = -1L;
    private String msgId;

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getMsgId() {
        return this.msgId;
    }
}

