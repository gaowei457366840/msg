package com.common.msg.kafka.core.model;

import java.util.List;
import java.util.Objects;


public class BrokerMetadataResponse
        extends CommonResponse {
    private List<String> routerAddresses;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BrokerMetadataResponse)) return false;
        BrokerMetadataResponse other = (BrokerMetadataResponse) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$routerAddresses = (Object) getRouterAddresses(), other$routerAddresses = (Object) other.getRouterAddresses();
        return Objects.equals(this$routerAddresses, other$routerAddresses);
    }

    protected boolean canEqual(Object other) {
        return other instanceof BrokerMetadataResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $routerAddresses = (Object) getRouterAddresses();
        return result * 59 + (($routerAddresses == null) ? 43 : $routerAddresses.hashCode());
    }

    public String toString() {
        return "BrokerMetadataResponse(super=" + super.toString() + ", routerAddresses=" + getRouterAddresses() + ")";
    }

    public void setRouterAddresses(List<String> routerAddresses) {
        this.routerAddresses = routerAddresses;
    }

    public List<String> getRouterAddresses() {
        return this.routerAddresses;
    }
}

