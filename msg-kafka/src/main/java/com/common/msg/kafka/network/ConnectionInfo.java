package com.common.msg.kafka.network;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Set;


public class ConnectionInfo {
    private InetSocketAddress address;
    private String url;
    private volatile boolean isConnected;
    private Set<String> topics;

    public void setAddress(InetSocketAddress address) {

        this.address = address;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public void setTopics(Set<String> topics) {
        this.topics = topics;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof ConnectionInfo)) return false;
        ConnectionInfo other = (ConnectionInfo) o;
        if (!other.canEqual(this)) return false;
        Object this$address = getAddress(), other$address = other.getAddress();
        if (!Objects.equals(this$address, other$address)) return false;
        Object this$url = getUrl(), other$url = other.getUrl();
        if (!Objects.equals(this$url, other$url)) return false;
        if (isConnected() != other.isConnected()) return false;
        Set<String> this$topics = (Set<String>) getTopics(), other$topics = (Set<String>) other.getTopics();
        return !(!Objects.equals(this$topics, other$topics));
    }

    protected boolean canEqual(Object other) {
        return other instanceof ConnectionInfo;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $address = getAddress();
        result = result * 59 + (($address == null) ? 43 : $address.hashCode());
        Object $url = getUrl();
        result = result * 59 + (($url == null) ? 43 : $url.hashCode());
        result = result * 59 + (isConnected() ? 79 : 97);
        Set<String> $topics = (Set<String>) getTopics();
        return result * 59 + (($topics == null) ? 43 : $topics.hashCode());
    }

    public String toString() {
        return "ConnectionInfo(address=" + getAddress() + ", url=" + getUrl() + ", isConnected=" + isConnected() + ", topics=" + getTopics() + ")";
    }

    public InetSocketAddress getAddress() {

        return this.address;
    }

    public String getUrl() {

        return this.url;
    }

    public boolean isConnected() {

        return this.isConnected;
    }

    public Set<String> getTopics() {

        return this.topics;
    }

    public ConnectionInfo() {
    }

    public ConnectionInfo(InetSocketAddress address, boolean isConnected) {

        this.address = address;

        this.isConnected = isConnected;
    }
}

