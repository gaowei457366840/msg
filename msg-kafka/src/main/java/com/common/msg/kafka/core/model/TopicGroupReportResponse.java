package com.common.msg.kafka.core.model;


public class TopicGroupReportResponse
        extends CommonResponse {
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof TopicGroupReportResponse)) return false;
        TopicGroupReportResponse other = (TopicGroupReportResponse) o;
        return !other.canEqual(this) ? false : (!!super.equals(o));
    }

    protected boolean canEqual(Object other) {
        return other instanceof TopicGroupReportResponse;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return "TopicGroupReportResponse(super=" + super.toString() + ")";
    }
}


