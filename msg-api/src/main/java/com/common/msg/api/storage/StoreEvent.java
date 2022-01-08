package com.common.msg.api.storage;

import com.common.msg.api.event.Event;


public class StoreEvent
        extends Event {
    private long nextRetryTime;
    private String type;
    private String topic;
    private String groupId;
    private String msgId;
    private int retries;
    private int currentRetriedCount;

    public void setNextRetryTime(long nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setRetries(int retries) {
        this.retries = retries;
    }

    public void setCurrentRetriedCount(int currentRetriedCount) {
        this.currentRetriedCount = currentRetriedCount;
    }

    public String toString() {
        return "StoreEvent(super=" + super.toString() + ", nextRetryTime=" + getNextRetryTime() + ", type=" + getType() + ", topic=" + getTopic() + ", groupId=" + getGroupId() + ", msgId=" + getMsgId() + ", retries=" + getRetries() + ", currentRetriedCount=" + getCurrentRetriedCount() + ")";
    }

    public long getNextRetryTime() {
        return this.nextRetryTime;
    }

    public String getType() {
        return this.type;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public int getRetries() {
        return this.retries;
    }

    public int getCurrentRetriedCount() {
        return this.currentRetriedCount;
    }

    public StoreEvent() {
        setGroup(StoreEvent.class);
    }

    public StoreEvent(long nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }
}

