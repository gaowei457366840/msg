package com.common.msg.kafka.core.model;

import java.util.List;
import java.util.Objects;


public class JoinGroupResponse
        extends CommonResponse {
    private List<TopicMetadata> topicMetadata;

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof JoinGroupResponse)) return false;
        JoinGroupResponse other = (JoinGroupResponse) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        List<TopicMetadata> this$topicMetadata = (List<TopicMetadata>) getTopicMetadata(), other$topicMetadata = (List<TopicMetadata>) other.getTopicMetadata();
        return !(!Objects.equals(this$topicMetadata, other$topicMetadata));
    }

    protected boolean canEqual(Object other) {
        return other instanceof JoinGroupResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        List<TopicMetadata> topicMetadata = getTopicMetadata();
        return result * 59 + ((topicMetadata == null) ? 43 : topicMetadata.hashCode());
    }

    public String toString() {
        return "JoinGroupResponse(super=" + super.toString() + ", topicMetadata=" + getTopicMetadata() + ")";
    }

    public void setTopicMetadata(List<TopicMetadata> topicMetadata) {
        this.topicMetadata = topicMetadata;
    }

    public List<TopicMetadata> getTopicMetadata() {

        return this.topicMetadata;
    }
}


