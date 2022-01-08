package com.common.msg.kafka.core.model;


import java.util.Objects;

public class HandshakeRequest extends CommonRequest {
    private String sarname;
    private String version;
    private String language;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof HandshakeRequest)) return false;
        HandshakeRequest other = (HandshakeRequest) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$sarname = getSarname(), other$sarname = other.getSarname();
        if (!Objects.equals(this$sarname, other$sarname)) return false;
        Object this$version = getVersion(), other$version = other.getVersion();
        if (!Objects.equals(this$version, other$version)) return false;
        Object this$language = getLanguage(), other$language = other.getLanguage();
        return !(!Objects.equals(this$language, other$language));
    }

    protected boolean canEqual(Object other) {
        return other instanceof HandshakeRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $sarname = getSarname();
        result = result * 59 + (($sarname == null) ? 43 : $sarname.hashCode());
        Object $version = getVersion();
        result = result * 59 + (($version == null) ? 43 : $version.hashCode());
        Object $language = getLanguage();
        return result * 59 + (($language == null) ? 43 : $language.hashCode());
    }

    public String toString() {
        return "HandshakeRequest(super=" + super.toString() + ", sarname=" + getSarname() + ", version=" + getVersion() + ", language=" + getLanguage() + ")";
    }

    public void setSarname(String sarname) {
        this.sarname = sarname;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getSarname() {

        return this.sarname;
    }

    public String getVersion() {

        return this.version;
    }

    public String getLanguage() {

        return this.language;
    }

    public HandshakeRequest() {
    }

    public HandshakeRequest(String sarname, String version) {
        this.sarname = sarname;
        this.version = version;
        this.language = "java";
    }

    public HandshakeRequest(String sarname, String version, String language) {
        this.sarname = sarname;
        this.version = version;
        this.language = language;
    }
}


