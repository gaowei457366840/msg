package com.common.msg.kafka.network.retry;


import java.util.Objects;

public class RetryMessage<T> {
    public void setStartTime(long startTime) {

        this.startTime = startTime;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setCurrentTimes(int currentTimes) {
        this.currentTimes = currentTimes;
    }

    public void setDefaultTimes(int defaultTimes) {
        this.defaultTimes = defaultTimes;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof RetryMessage)) return false;
        RetryMessage<?> other = (RetryMessage) o;
        if (!other.canEqual(this)) return false;
        if (getStartTime() != other.getStartTime()) return false;
        if (getId() != other.getId()) return false;
        Object this$data = getData(), other$data = other.getData();
        return (Objects.equals(this$data, other$data)) && ((getCurrentTimes() != other.getCurrentTimes()) ? false : (!(getDefaultTimes() != other.getDefaultTimes())));
    }

    protected boolean canEqual(Object other) {
        return other instanceof RetryMessage;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        long $startTime = getStartTime();
        result = result * 59 + (int) ($startTime >>> 32L ^ $startTime);
        long $id = getId();
        result = result * 59 + (int) ($id >>> 32L ^ $id);
        Object $data = getData();
        result = result * 59 + (($data == null) ? 43 : $data.hashCode());
        result = result * 59 + getCurrentTimes();
        return result * 59 + getDefaultTimes();
    }

    public String toString() {
        return "RetryMessage(startTime=" + getStartTime() + ", id=" + getId() + ", data=" + getData() + ", currentTimes=" + getCurrentTimes() + ", defaultTimes=" + getDefaultTimes() + ")";
    }


    static long now() {
        return System.currentTimeMillis();
    }

    private long id;
    private long startTime = now() + 10000L;
    private T data;

    public long getStartTime() {
        return this.startTime;
    }

    public long getId() {
        return this.id;
    }

    public T getData() {
        return this.data;
    }

    private int currentTimes = 1;

    public int getCurrentTimes() {
        return this.currentTimes;
    }


    private int defaultTimes = 5;

    public int getDefaultTimes() {
        return this.defaultTimes;
    }

    public RetryMessage(long id, T data) {
        this.id = id;
        this.data = data;
    }
}

