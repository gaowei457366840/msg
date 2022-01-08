package com.common.msg.api.producer;

import java.net.InetSocketAddress;
import java.util.Objects;


public class SpecialSendResult
        extends SendResult {
    private InetSocketAddress node;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof SpecialSendResult)) return false;
        SpecialSendResult other = (SpecialSendResult) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$node = getNode(), other$node = other.getNode();
        return !(!Objects.equals(this$node, other$node));
    }

    protected boolean canEqual(Object other) {
        return other instanceof SpecialSendResult;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $node = getNode();
        return result * 59 + (($node == null) ? 43 : $node.hashCode());
    }

    public String toString() {
        return "SpecialSendResult(super=" + super.toString() + ", node=" + getNode() + ")";
    }

    public void setNode(InetSocketAddress node) {
        this.node = node;
    }

    public InetSocketAddress getNode() {
        return this.node;
    }
}

