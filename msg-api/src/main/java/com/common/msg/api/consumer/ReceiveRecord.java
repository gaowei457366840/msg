package com.common.msg.api.consumer;

import com.common.msg.api.MessageRecord;

import java.util.Objects;


public class ReceiveRecord<T>
        extends MessageRecord<T> {
    private transient String topic;
    private int partition;
    private long offset;
    private long timestamp;
    private String msgId;

    public void setTopic(String topic) {
        this.topic = topic;
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
        if (!(o instanceof ReceiveRecord)) return false;
        ReceiveRecord<?> other = (ReceiveRecord) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        if (getPartition() != other.getPartition()) return false;
        if (getOffset() != other.getOffset()) return false;
        if (getTimestamp() != other.getTimestamp()) return false;
        Object this$msgId = getMsgId(), other$msgId = other.getMsgId();
        return Objects.equals(this$msgId, other$msgId);
    }

    protected boolean canEqual(Object other) {
        return other instanceof ReceiveRecord;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        result = result * 59 + getPartition();
        long $offset = getOffset();
        result = result * 59 + (int) ($offset >>> 32L ^ $offset);
        long $timestamp = getTimestamp();
        result = result * 59 + (int) ($timestamp >>> 32L ^ $timestamp);
        Object $msgId = getMsgId();
        return result * 59 + (($msgId == null) ? 43 : $msgId.hashCode());
    }

    public String toString() {
        return "ReceiveRecord(super=" + super.toString() + ", topic=" + getTopic() + ", partition=" + getPartition() + ", offset=" + getOffset() + ", timestamp=" + getTimestamp() + ", msgId=" + getMsgId() + ")";
    }

    public String getTopic() {
        return this.topic;
    }

    public int getPartition() {
        return this.partition;
    }

    public long getOffset() {
        return this.offset;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getMsgId() {
        return this.msgId;
    }
}


