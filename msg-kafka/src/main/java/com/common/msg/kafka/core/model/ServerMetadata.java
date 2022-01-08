package com.common.msg.kafka.core.model;


import java.util.Objects;

public class ServerMetadata {
    String url;
    long expireTime;
    boolean isActive;

    public void setUrl(String url) {

        this.url = url;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ServerMetadata)) return false;
        ServerMetadata other = (ServerMetadata) o;
        if (!other.canEqual(this)) return false;
        Object this$url = getUrl(), other$url = other.getUrl();
        return (Objects.equals(this$url, other$url)) && ((getExpireTime() != other.getExpireTime()) ? false : (!(isActive() != other.isActive())));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ServerMetadata;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $url = getUrl();
        result = result * 59 + (($url == null) ? 43 : $url.hashCode());
        long $expireTime = getExpireTime();
        result = result * 59 + (int) ($expireTime >>> 32L ^ $expireTime);
        return result * 59 + (isActive() ? 79 : 97);
    }

    public String toString() {
        return "ServerMetadata(url=" + getUrl() + ", expireTime=" + getExpireTime() + ", isActive=" + isActive() + ")";
    }

    public String getUrl() {

        return this.url;
    }

    public long getExpireTime() {

        return this.expireTime;
    }

    public boolean isActive() {

        return this.isActive;
    }
}


