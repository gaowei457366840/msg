package com.common.msg.kafka.core.model;

import java.util.List;


public class TopicMetadata {
    String topic;
    String groupId;
    int privilege;
    List<ServerMetadata> servers;

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setPrivilege(int privilege) {
        this.privilege = privilege;
    }

    public void setServers(List<ServerMetadata> servers) {
        this.servers = servers;
    }

    public String toString() {
        return "TopicMetadata(topic=" + getTopic() + ", groupId=" + getGroupId() + ", privilege=" + getPrivilege() + ", servers=" + getServers() + ")";
    }

    public String getTopic() {
        return this.topic;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public int getPrivilege() {
        return this.privilege;
    }

    public List<ServerMetadata> getServers() {
        return this.servers;
    }

    public TopicMetadata() {
    }

    public TopicMetadata(String topic, String groupId) {
        this.topic = topic;
        this.groupId = groupId;
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TopicMetadata that = (TopicMetadata) o;

        if (!getTopic().equals(that.getTopic())) return false;
        return getGroupId().equals(that.getGroupId());
    }


    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + getTopic().hashCode();
        result = 31 * result + getGroupId().hashCode();
        return result;
    }
}


