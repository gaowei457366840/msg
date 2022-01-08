package com.common.msg.kafka.core.model;


import java.util.Objects;

public class BrokerMetadataRequest
        extends CommonRequest {
    private String sarname;
    private String version;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BrokerMetadataRequest)) return false;
        BrokerMetadataRequest other = (BrokerMetadataRequest) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$sarname = getSarname(), other$sarname = other.getSarname();
        if (!Objects.equals(this$sarname, other$sarname)) return false;
        Object this$version = getVersion(), other$version = other.getVersion();
        return !(!Objects.equals(this$version, other$version));
    }

    protected boolean canEqual(Object other) {
        return other instanceof BrokerMetadataRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $sarname = getSarname();
        result = result * 59 + (($sarname == null) ? 43 : $sarname.hashCode());
        Object $version = getVersion();
        return result * 59 + (($version == null) ? 43 : $version.hashCode());
    }


    public String toString() {
        return "BrokerMetadataRequest(super=" + super.toString() + ", sarname=" + getSarname() + ", version=" + getVersion() + ")";
    }


    public void setSarname(String sarname) {
        this.sarname = sarname;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSarname() {

        return this.sarname;
    }

    public String getVersion() {

        return this.version;
    }

    public BrokerMetadataRequest() {
    }

    public BrokerMetadataRequest(String sarname, String version) {

        this.sarname = sarname;

        this.version = version;
    }
}


