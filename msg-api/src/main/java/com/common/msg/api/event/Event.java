package com.common.msg.api.event;

import java.util.Arrays;


public class Event {
    private transient Class group;
    private byte[] payload;

    public void setGroup(Class group) {
        this.group = group;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public String toString() {
        return "Event(group=" + getGroup() + ", payload=" + Arrays.toString(getPayload()) + ")";
    }

    public Class getGroup() {

        return this.group;
    }

    public byte[] getPayload() {

        return this.payload;
    }
}

