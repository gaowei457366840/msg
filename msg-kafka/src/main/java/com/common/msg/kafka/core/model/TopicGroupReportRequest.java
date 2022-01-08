package com.common.msg.kafka.core.model;

import java.util.Objects;
import java.util.Set;


public class TopicGroupReportRequest {
    private Set<TopicGroupInfo> topicGroupInfos;

    public String toString() {
        return "TopicGroupReportRequest(topicGroupInfos=" + getTopicGroupInfos() + ")";
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Set<TopicGroupInfo> $topicGroupInfos = getTopicGroupInfos();
        return result * 59 + (($topicGroupInfos == null) ? 43 : $topicGroupInfos.hashCode());
    }

    protected boolean canEqual(Object other) {
        return other instanceof TopicGroupReportRequest;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TopicGroupReportRequest)) return false;
        TopicGroupReportRequest other = (TopicGroupReportRequest) o;
        if (!other.canEqual(this)) return false;
        Set<TopicGroupInfo> this$topicGroupInfos = getTopicGroupInfos(), other$topicGroupInfos = other.getTopicGroupInfos();
        return Objects.equals(this$topicGroupInfos, other$topicGroupInfos);
    }

    public void setTopicGroupInfos(Set<TopicGroupInfo> topicGroupInfos) {
        this.topicGroupInfos = topicGroupInfos;
    }

    public Set<TopicGroupInfo> getTopicGroupInfos() {

        return this.topicGroupInfos;
    }
}


