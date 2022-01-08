package com.common.msg.kafka.core.model;

import java.util.Arrays;


public final class MessageContext {
    public void setHead(MessageHead head) {

        this.head = head;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MessageContext)) return false;
        MessageContext other = (MessageContext) o;
        MessageHead this$head = getHead(), other$head = other.getHead();
        return ((this$head == null) ? (other$head != null) : !this$head.equals(other$head)) ? false : (!!Arrays.equals(getBody(), other.getBody()));
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $head = getHead();
        result = result * 59 + (($head == null) ? 43 : $head.hashCode());
        return result * 59 + Arrays.hashCode(getBody());
    }

    public String toString() {
        return "MessageContext(head=" + getHead() + ", body=" + Arrays.toString(getBody()) + ")";
    }

    public MessageHead getHead() {

        return this.head;
    }

    public byte[] getBody() {

        return this.body;
    }

    private MessageHead head = new MessageHead();
    private byte[] body;

    public MessageContext(String type) {

        this();

        this.head.setType(type);
    }

    public MessageContext(String type, byte[] body) {

        this(type);

        this.body = body;
    }

    public MessageContext(String type, long requestId, byte[] body) {

        this(type, body);

        this.head.setRequestId(requestId);
    }

    public String getRemoteIp() {

        return this.head.getRemoteIp();
    }

    public void setRemoteId(String clientId) {

        this.head.setRemoteIp(clientId);
    }

    public String getSessionId() {

        return this.head.getSessionId();
    }

    public void setSessionId(String sessionId) {

        this.head.setSessionId(sessionId);
    }

    public long getRequestId() {

        return this.head.getRequestId();
    }

    public void setRequestId(long requestId) {

        this.head.setRequestId(requestId);
    }

    public String getType() {

        return this.head.getType();
    }

    public void setType(String type) {

        this.head.setType(type);
    }

    public String getVersion() {

        return this.head.getVersion();
    }

    public void setVersion(String version) {

        this.head.setVersion(version);
    }

    public void setHostname(String hostname) {

        this.head.setHostname(hostname);
    }

    public String getHostname() {

        return this.head.getHostname();
    }

    public MessageContext() {
    }
}


