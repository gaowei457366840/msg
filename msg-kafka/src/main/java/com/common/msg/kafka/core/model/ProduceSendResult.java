package com.common.msg.kafka.core.model;


import java.util.Objects;

public class ProduceSendResult {
    private String topic;
    private String key;

    public void setTopic(String topic) {

        this.topic = topic;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public void setError(RuntimeException error) {
        this.error = error;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ProduceSendResult)) return false;
        ProduceSendResult other = (ProduceSendResult) o;
        if (!other.canEqual(this)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        Object this$key = getKey(), other$key = other.getKey();
        if (!Objects.equals(this$key, other$key)) return false;
        if (getTimestamp() != other.getTimestamp()) return false;
        Object this$sid = getSid(), other$sid = other.getSid();
        if (!Objects.equals(this$sid, other$sid)) return false;
        if (getPartition() != other.getPartition()) return false;
        Object this$error = getError(), other$error = other.getError();
        return !(!Objects.equals(this$error, other$error));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ProduceSendResult;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        Object $key = getKey();
        result = result * 59 + (($key == null) ? 43 : $key.hashCode());
        long $timestamp = getTimestamp();
        result = result * 59 + (int) ($timestamp >>> 32L ^ $timestamp);
        Object $sid = getSid();
        result = result * 59 + (($sid == null) ? 43 : $sid.hashCode());
        result = result * 59 + getPartition();
        Object $error = getError();
        return result * 59 + (($error == null) ? 43 : $error.hashCode());
    }

    public String toString() {
        return "ProduceSendResult(topic=" + getTopic() + ", key=" + getKey() + ", timestamp=" + getTimestamp() + ", sid=" + getSid() + ", partition=" + getPartition() + ", error=" + getError() + ")";
    }

    public String getTopic() {

        return this.topic;
    }

    public String getKey() {

        return this.key;
    }

    private String sid;
    private int partition;
    private long timestamp = -1L;
    private RuntimeException error;

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getSid() {

        return this.sid;
    }

    public int getPartition() {

        return this.partition;
    }

    public RuntimeException getError() {

        return this.error;
    }
}


