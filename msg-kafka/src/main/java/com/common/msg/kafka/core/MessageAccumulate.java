package com.common.msg.kafka.core;

import com.common.msg.api.MessageAccessor;
import com.common.msg.api.MessageRecord;
import com.common.msg.api.common.SendTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.event.MqEvent;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.kafka.common.MessageTypeEnum;
import com.common.msg.kafka.event.KafkaEndTransactionEvent;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.kafka.network.NetworkService;
import com.common.msg.kafka.network.SyncFuture;
import com.common.msg.api.producer.SpecialSendResult;
import com.common.msg.kafka.core.model.CommonRequest;
import com.common.msg.kafka.core.model.EndTransactionRequest;
import com.common.msg.kafka.core.model.SendMessageRequest;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageAccumulate {
    private static final Logger log = LoggerFactory.getLogger(MessageAccumulate.class);


    private final Map<String, Deque<ProducerBatch>> transactionBatch;

    private final Map<String, Deque<ProducerBatch>> delayBatch;

    private final Map<String, Deque<ProducerBatch>> delayBatchOneway;

    private final Map<String, Deque<ProducerBatch>> endTransactionBatch;

    private final int batchSize;

    private final int lingerMs;

    private final ScheduledExecutorService scheduled;

    private final NetworkService clientNetwork;


    public MessageAccumulate(NetworkService clientNetwork) {
        this.clientNetwork = clientNetwork;
        this.transactionBatch = new ConcurrentHashMap<>();
        this.delayBatch = new ConcurrentHashMap<>();
        this.delayBatchOneway = new ConcurrentHashMap<>();
        this.endTransactionBatch = new ConcurrentHashMap<>();
        this.batchSize = ConfigManager.getInt("mq.special.message.batch.size", 100);
        this.lingerMs = ConfigManager.getInt("mq.special.message.linger.ms", 20);
        this.scheduled = Executors.newSingleThreadScheduledExecutor();
        startAutoAppend();
    }

    public SyncFuture<SpecialSendResult> appendTransactionMessage(KafkaProduceEvent event) {
        synchronized (this.transactionBatch) {
            Deque<ProducerBatch> topicBatch = this.transactionBatch.get(event.getConfig().getTopic());
            return tryAppend(this.transactionBatch, topicBatch, (MqEvent) event);
        }
    }

    public SyncFuture<SpecialSendResult> appendDelayMessage(KafkaProduceEvent event) {
        if (event.getSendType().getCode() == SendTypeEnum.ONEWAY.getCode()) {
            synchronized (this.delayBatchOneway) {
                Deque<ProducerBatch> topicBatch = this.delayBatchOneway.get(event.getConfig().getTopic());
                return tryAppend(this.delayBatchOneway, topicBatch, (MqEvent) event);
            }
        }
        synchronized (this.delayBatch) {
            Deque<ProducerBatch> topicBatch = this.delayBatch.get(event.getConfig().getTopic());
            return tryAppend(this.delayBatch, topicBatch, (MqEvent) event);
        }
    }


    public void appendEndTransactionMessage(KafkaEndTransactionEvent event) {
        synchronized (this.endTransactionBatch) {

            Deque<ProducerBatch> topicBatch = this.endTransactionBatch.get(event.getNode().toString());
            if (topicBatch == null) {
                topicBatch = new ArrayDeque<>(this.batchSize);
                this.endTransactionBatch.put(event.getNode().toString(), topicBatch);
            }
            tryAppend(this.endTransactionBatch, topicBatch, (MqEvent) event);
        }
    }

    public void close() {
        this.scheduled.shutdownNow();
    }

    private SyncFuture<SpecialSendResult> tryAppend(Map<String, Deque<ProducerBatch>> batch, Deque<ProducerBatch> deque, MqEvent event) {
        SyncFuture<SpecialSendResult> future;
        if (deque == null) {
            deque = new ArrayDeque<>(this.batchSize);
            batch.put(event.getConfig().getTopic(), deque);
        }

        ProducerBatch producerBatch = deque.peekLast();


        if (null == producerBatch || producerBatch.getThunks().size() == this.batchSize || (producerBatch
                .isEndTransaction() && !producerBatch.getNode().equals(((KafkaEndTransactionEvent) event).getNode()))) {
            if (null != producerBatch) {
                send(event.getConfig().getTopic(), deque.pollLast());
            }

            if (event instanceof KafkaEndTransactionEvent) {

                producerBatch = new ProducerBatch(event.getConfig().getTopic(), ((KafkaEndTransactionEvent) event).getNode(), true, true, this.batchSize);

            } else {


                producerBatch = new ProducerBatch(event.getConfig().getTopic(), SendTypeEnum.ONEWAY.equals(event.getSendType()), false, this.batchSize);
            }


            future = append(producerBatch, event);
            deque.addLast(producerBatch);
        } else {
            future = append(producerBatch, event);
        }
        return future;
    }

    private SyncFuture<SpecialSendResult> append(ProducerBatch producerBatch, MqEvent mqEvent) {
        SyncFuture<SpecialSendResult> future = null;
        if (producerBatch.isEndTransaction()) {
            KafkaEndTransactionEvent event = (KafkaEndTransactionEvent) mqEvent;
            producerBatch.append(event.getMsgId(), event.getStatus(), event.getLocalException());
            log.debug("Append end transaction to batch success. msgId:" + event.getMsgId());
        } else {
            KafkaProduceEvent event = (KafkaProduceEvent) mqEvent;
            future = producerBatch.append(buildMessageRecord(event), event.getMessageRecord(), event.getCallback());
            log.debug("Append special message to batch success. msgKey:" + event.getMessageRecord());
        }
        return future;
    }

    private MessageRecord<byte[]> buildMessageRecord(KafkaProduceEvent event) {
        MessageRecord<byte[]> record = new MessageRecord();
        record.setMessage(event.getPayload());
        record.setKey(event.getMessageRecord().getKey());
        record.setTag(event.getMessageRecord().getTag());
        record.setStartDeliverTime(event.getMessageRecord().getStartDeliverTime());
        record.setUserProperties(event.getMessageRecord().getUserProperties());
        MessageAccessor.setSystemProperties(record, MessageAccessor.getSystemProperties(event.getMessageRecord()));
        return record;
    }

    private void send(String topic, ProducerBatch producerBatch) {
        SendMessageRequest sendMessageRequest = null;
        if (producerBatch.isEndTransaction()) {
            EndTransactionRequest endTransactionRequest = buildEndTransactionRequest(topic, producerBatch);
        } else {
            sendMessageRequest = buildSendMessageRequest(topic, producerBatch);
        }
        this.clientNetwork.send((CommonRequest) sendMessageRequest, topic,
                producerBatch.getNode(),
                producerBatch.getCreateMs(),
                producerBatch.getCallback());
    }

    private SendMessageRequest buildSendMessageRequest(String topic, ProducerBatch batch) {
        SendMessageRequest request = new SendMessageRequest();
        request.setGroup(ConfigManager.getSarName(""));
        request.setType(MessageTypeEnum.SEND_MESSAGE_REQ.getCode());
        request.setTopic(topic);
        request.setOneway((batch.getCallback() == null));
        request.setMessages(batch.getRecordBatch());
        request.setLength(batch.getLength());
        return request;
    }

    private EndTransactionRequest buildEndTransactionRequest(String topic, ProducerBatch batch) {
        EndTransactionRequest request = new EndTransactionRequest();
        request.setGroup(ConfigManager.getSarName(""));
        request.setType(MessageTypeEnum.END_TRANSACTION_REQ.getCode());
        request.setTopic(topic);
        request.setOneway(true);
        request.setEndTransactionMessages(batch.getEndTransactionBatch());
        return request;
    }

    private void startAutoAppend() {
        this.scheduled.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    synchronized (MessageAccumulate.this.transactionBatch) {
                        for (Map.Entry<String, Deque<ProducerBatch>> entry : (Iterable<Map.Entry<String, Deque<ProducerBatch>>>) MessageAccumulate.this.transactionBatch.entrySet()) {
                            MessageAccumulate.this.autoAppend(entry.getValue());
                            MessageAccumulate.log.debug("Send transaction message batch success. topic:" + (String) entry.getKey());
                        }
                    }

                    synchronized (MessageAccumulate.this.delayBatch) {
                        for (Map.Entry<String, Deque<ProducerBatch>> entry : (Iterable<Map.Entry<String, Deque<ProducerBatch>>>) MessageAccumulate.this.delayBatch.entrySet()) {
                            MessageAccumulate.this.autoAppend(entry.getValue());
                            MessageAccumulate.log.debug("Send delay message batch success. topic:" + (String) entry.getKey());
                        }
                    }

                    synchronized (MessageAccumulate.this.endTransactionBatch) {
                        for (Map.Entry<String, Deque<ProducerBatch>> entry : (Iterable<Map.Entry<String, Deque<ProducerBatch>>>) MessageAccumulate.this.endTransactionBatch.entrySet()) {
                            MessageAccumulate.this.autoAppend(entry.getValue());
                            MessageAccumulate.log.debug("Send end transaction batch success. topic:" + (String) entry.getKey());
                        }
                    }
                } catch (Throwable t) {
                    MessageAccumulate.log.warn("Batch message failed.", t);
                }
            }
        }, this.lingerMs, this.lingerMs, TimeUnit.MILLISECONDS);
    }

    private void autoAppend(Deque<ProducerBatch> deque) {
        ProducerBatch batch;
        while (null != (batch = deque.peekLast())) {
            if (MillisecondClock.now() - batch.getCreateMs() > this.lingerMs)
                send(batch.getTopic(), deque.pollLast());
        }
    }
}

