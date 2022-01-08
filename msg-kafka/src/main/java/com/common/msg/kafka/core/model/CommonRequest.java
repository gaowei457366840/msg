package com.common.msg.kafka.core.model;


import java.util.Objects;

public class CommonRequest {
    private boolean isOneway;
    private String type;

    public void setOneway(boolean isOneway) {
        this.isOneway = isOneway;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof CommonRequest)) return false;
        CommonRequest other = (CommonRequest) o;
        if (!other.canEqual(this)) return false;
        if (isOneway() != other.isOneway()) return false;
        Object this$type = getType(), other$type = other.getType();
        return Objects.equals(this$type, other$type);
    }

    protected boolean canEqual(Object other) {
        return other instanceof CommonRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        result = result * PRIME + (isOneway() ? 79 : 97);
        Object $type = getType();
        return result * 59 + (($type == null) ? 43 : $type.hashCode());
    }

    public String toString() {
        return "CommonRequest(isOneway=" + isOneway() + ", type=" + getType() + ")";
    }

    public boolean isOneway() {
        return this.isOneway;
    }

    public String getType() {
        return this.type;
    }
}

