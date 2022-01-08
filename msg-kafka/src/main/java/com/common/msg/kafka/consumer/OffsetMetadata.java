

package com.common.msg.kafka.consumer;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class OffsetMetadata {
    private final TopicPartition topicPartition;
    private final OffsetAndMetadata offsetAndMetadata;
    private final boolean lastOffset;

    public TopicPartition getTopicPartition() {
        return this.topicPartition;
    }

    public OffsetAndMetadata getOffsetAndMetadata() {
        return this.offsetAndMetadata;
    }

    public boolean isLastOffset() {
        return this.lastOffset;
    }

    public OffsetMetadata(TopicPartition topicPartition, OffsetAndMetadata offsetAndMetadata, boolean lastOffset) {
        this.topicPartition = topicPartition;
        this.offsetAndMetadata = offsetAndMetadata;
        this.lastOffset = lastOffset;
    }

    public String toString() {
        return "OffsetMetadata(topicPartition=" + this.getTopicPartition() + ", offsetAndMetadata=" + this.getOffsetAndMetadata() + ", lastOffset=" + this.isLastOffset() + ")";
    }
}
