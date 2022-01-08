package com.common.msg.kafka.core.model;

import java.util.List;
import java.util.Objects;


public class BrokerCommandRequest
        extends CommonRequest {
    private List<String> ipList;
    private int command;
    private String content;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof BrokerCommandRequest)) return false;
        BrokerCommandRequest other = (BrokerCommandRequest) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$ipList = (Object) getIpList(), other$ipList = (Object) other.getIpList();
        if (!Objects.equals(this$ipList, other$ipList)) return false;
        if (getCommand() != other.getCommand()) return false;
        Object this$content = getContent(), other$content = other.getContent();
        return !(!Objects.equals(this$content, other$content));
    }

    protected boolean canEqual(Object other) {
        return other instanceof BrokerCommandRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object ipList = (Object) getIpList();
        result = result * 59 + ((ipList == null) ? 43 : ipList.hashCode());
        result = result * 59 + getCommand();
        Object $content = getContent();
        return result * 59 + (($content == null) ? 43 : $content.hashCode());
    }

    public String toString() {
        return "BrokerCommandRequest(super=" + super.toString() + ", ipList=" + getIpList() + ", command=" + getCommand() + ", content=" + getContent() + ")";
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getIpList() {
        return this.ipList;
    }

    public int getCommand() {
        return this.command;
    }

    public String getContent() {
        return this.content;
    }
}

