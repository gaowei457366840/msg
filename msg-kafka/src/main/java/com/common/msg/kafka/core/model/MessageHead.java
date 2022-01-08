package com.common.msg.kafka.core.model;


import java.util.Objects;

public final class MessageHead {
    private String remoteIp;
    private String hostname;
    private String sessionId;
    private long requestId;
    private String type;
    private String version;

    public void setRemoteIp(String remoteIp) {

        this.remoteIp = remoteIp;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MessageHead)) return false;
        MessageHead other = (MessageHead) o;
        Object this$remoteIp = getRemoteIp(), other$remoteIp = other.getRemoteIp();
        if (!Objects.equals(this$remoteIp, other$remoteIp)) return false;
        Object this$hostname = getHostname(), other$hostname = other.getHostname();
        if (!Objects.equals(this$hostname, other$hostname)) return false;
        Object this$sessionId = getSessionId(), other$sessionId = other.getSessionId();
        if (!Objects.equals(this$sessionId, other$sessionId))
            return false;
        if (getRequestId() != other.getRequestId()) return false;
        Object this$type = getType(), other$type = other.getType();
        if (!Objects.equals(this$type, other$type)) return false;
        Object this$version = getVersion(), other$version = other.getVersion();
        return Objects.equals(this$version, other$version);
    }

    public int hashCode() {
        int PRIME = 59, result = 1;
        Object $remoteIp = getRemoteIp();
        result = result * PRIME + (($remoteIp == null) ? 43 : $remoteIp.hashCode());
        Object $hostname = getHostname();
        result = result * PRIME + (($hostname == null) ? 43 : $hostname.hashCode());
        Object $sessionId = getSessionId();
        result = result * PRIME + (($sessionId == null) ? 43 : $sessionId.hashCode());
        long $requestId = getRequestId();
        result = result * PRIME + (int) ($requestId >>> 32L ^ $requestId);
        Object $type = getType();
        result = result * PRIME + (($type == null) ? 43 : $type.hashCode());
        Object $version = getVersion();
        return result * PRIME + (($version == null) ? 43 : $version.hashCode());
    }

    public String toString() {
        return "MessageHead(remoteIp=" + getRemoteIp() + ", hostname=" + getHostname() + ", sessionId=" + getSessionId() + ", requestId=" + getRequestId() + ", type=" + getType() + ", version=" + getVersion() + ")";
    }


    public String getRemoteIp() {

        return this.remoteIp;
    }

    public String getHostname() {

        return this.hostname;
    }

    public String getSessionId() {

        return this.sessionId;
    }

    public long getRequestId() {

        return this.requestId;
    }

    public String getType() {

        return this.type;
    }

    public String getVersion() {

        return this.version;
    }
}

