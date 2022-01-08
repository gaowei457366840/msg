package com.common.msg.kafka.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class SendMessageResponse
        extends CommonResponse {
    public boolean equals(Object o) {

        if (o == this) return true;
        if (!(o instanceof SendMessageResponse)) return false;
        SendMessageResponse other = (SendMessageResponse) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        List<ProduceSendResult> this$batchResult = (List<ProduceSendResult>) getBatchResult(), other$batchResult = (List<ProduceSendResult>) other.getBatchResult();
        if (!Objects.equals(this$batchResult, other$batchResult))
            return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        return Objects.equals(this$topic, other$topic);
    }

    protected boolean canEqual(Object other) {
        return other instanceof SendMessageResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        List<ProduceSendResult> $batchResult = (List<ProduceSendResult>) getBatchResult();
        result = result * 59 + (($batchResult == null) ? 43 : $batchResult.hashCode());
        Object $topic = getTopic();
        return result * 59 + (($topic == null) ? 43 : $topic.hashCode());
    }


    public String toString() {
        return "SendMessageResponse(super=" + super.toString() + ", batchResult=" + getBatchResult() + ", topic=" + getTopic() + ")";
    }


    public void setBatchResult(List<ProduceSendResult> batchResult) {
        this.batchResult = batchResult;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public List<ProduceSendResult> getBatchResult() {

        return this.batchResult;
    }

    public String getTopic() {

        return this.topic;
    }

    private List<ProduceSendResult> batchResult = new ArrayList<>();
    private String topic;

    public ProduceSendResult getResult(int index) {

        return this.batchResult.get(index);
    }
}


