package com.common.msg.kafka.consumer;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaConsumerOffsetManager {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerOffsetManager.class);

    private final Map<TopicPartition, ConsumerOffset> offsetMap;

    private final BlockingQueue<OffsetMetadata> commitQueue;


    public KafkaConsumerOffsetManager(BlockingQueue<OffsetMetadata> commitQueue) {
        this.commitQueue = commitQueue;
        this.offsetMap = new ConcurrentHashMap<>();
    }


    public void addPartition(TopicPartition partition, int recordSize, long offset) {
        if (!this.offsetMap.containsKey(partition)) {
            ConsumerOffset consumerOffset = new ConsumerOffset(partition, recordSize, offset);
            this.offsetMap.put(partition, consumerOffset);
        } else {
            reset(partition, offset);
        }
    }


    public void clear() {
        this.offsetMap.clear();
    }


    public void commitOffset(TopicPartition partition, boolean lastOffset) throws InterruptedException {
        if (null != partition && this.offsetMap.containsKey(partition)) {
            ((ConsumerOffset) this.offsetMap.get(partition)).commitOffset(lastOffset);
        }
    }


    public int getCommitableSize(TopicPartition partition) {
        if (this.offsetMap.containsKey(partition)) {
            return ((ConsumerOffset) this.offsetMap.get(partition)).getCommitableSize();
        }
        return 0;
    }


    public void acknowledge(TopicPartition partition, long offset) {
        if (this.offsetMap.containsKey(partition)) {
            ((ConsumerOffset) this.offsetMap.get(partition)).acknowledge(offset);
        }
    }


    public void reset(TopicPartition partition, long offset) {
        if (this.offsetMap.containsKey(partition)) {
            ((ConsumerOffset) this.offsetMap.get(partition)).reset(offset);
        }
    }


    public boolean isLastPosition(TopicPartition partition) {
        return (this.offsetMap.containsKey(partition) && ((ConsumerOffset) this.offsetMap.get(partition)).isLastPosition());
    }


    private class ConsumerOffset {
        private final TopicPartition partition;

        private final BitSet bitSet;

        private final int size;

        private int lastCommitPosition;
        private long lastCommitOffset;

        private ConsumerOffset(TopicPartition partition, int maxPollRecordSize, long lastCommitOffset) {
            this.lastCommitOffset = lastCommitOffset;
            this.partition = partition;
            this.bitSet = new BitSet(maxPollRecordSize);
            this.size = this.bitSet.size();
        }


        private synchronized void commitOffset(boolean lastOffset) throws InterruptedException {
            int commitablePosition = this.bitSet.nextClearBit(this.lastCommitPosition);
            if (commitablePosition > 0) {

                long commitableOffset = (commitablePosition - this.lastCommitPosition) + this.lastCommitOffset;


                OffsetMetadata offsetMetadata = new OffsetMetadata(this.partition, new OffsetAndMetadata(commitableOffset), lastOffset);


                KafkaConsumerOffsetManager.this.commitQueue.put(offsetMetadata);


                this.lastCommitOffset = commitableOffset;
                this.lastCommitPosition = commitablePosition;
                this.lastCommitPosition = (this.lastCommitPosition >= this.size) ? 0 : this.lastCommitPosition;

                KafkaConsumerOffsetManager.log.debug("commitToQueue:" + offsetMetadata + ", lastCommitOffset:" + this.lastCommitOffset + ", lastCommitPosition:" + this.lastCommitPosition);
            }
        }

        private synchronized int getCommitableSize() {
            return this.bitSet.nextClearBit(this.lastCommitPosition) - this.lastCommitPosition;
        }

        private synchronized void acknowledge(long offset) {
            this.bitSet.set(getPosition(offset));
        }

        private synchronized void reset(long offset) {
            this.bitSet.clear();
            this.lastCommitOffset = offset;
            this.lastCommitPosition = 0;
        }

        private boolean isLastPosition() {
            return (this.bitSet.size() == this.bitSet.nextClearBit(this.lastCommitPosition));
        }

        private int getPosition(long offset) {
            long gap = offset - this.lastCommitOffset;
            if (gap < 0L || gap > this.size) {
                throw new IllegalArgumentException("Commit offset is illegal. partition: " + this.partition + ", offset:" + offset + ", lastCommitOffset:" + this.lastCommitOffset);
            }


            int position = (int) (this.lastCommitPosition + gap);
            if (position > this.size) {
                position = position - this.size - 1;
            }
            KafkaConsumerOffsetManager.log.debug("Partition:" + this.partition + ", offset:" + offset + ", position:" + position);
            return position;
        }
    }

    public static void main(String[] args) throws Exception {
     /*   (new KafkaConsumerOffsetManager(null)).getClass();
       // ConsumerOffset bitSet = new ConsumerOffset(null, 8, 0L);

        long offset = 0L;
        for (int i = 0; i < 64; i++) {
            bitSet.acknowledge(i);
        }
        System.out.println("getCommitableOffset:" + bitSet.getCommitableSize() + "isLastPotition:" + bitSet.isLastPosition());*/
    }
}


