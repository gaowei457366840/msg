package com.common.msg.kafka.core.model;


public class HandshakeResponse
        extends BrokerMetadataResponse {
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof HandshakeResponse)) return false;
        HandshakeResponse other = (HandshakeResponse) o;
        return !other.canEqual(this) ? false : (!!super.equals(o));
    }

    protected boolean canEqual(Object other) {
        return other instanceof HandshakeResponse;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return "HandshakeResponse(super=" + super.toString() + ")";
    }
}


