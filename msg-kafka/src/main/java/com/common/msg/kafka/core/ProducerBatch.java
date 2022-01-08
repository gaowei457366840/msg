package com.common.msg.kafka.core;

import com.common.msg.api.MessageRecord;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.kafka.common.ResultCodeEnum;
import com.common.msg.kafka.network.Callback;
import com.common.msg.kafka.network.SyncFuture;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.producer.OnExceptionContext;
import com.common.msg.api.producer.SendCallback;
import com.common.msg.api.producer.SendResult;
import com.common.msg.api.producer.SpecialSendResult;
import com.common.msg.api.transaction.TransactionStatus;
import com.common.msg.kafka.core.model.CommonResponse;
import com.common.msg.kafka.core.model.EndTransactionMessage;
import com.common.msg.kafka.core.model.ProduceSendResult;
import com.common.msg.kafka.core.model.SendMessageResponse;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProducerBatch {
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setCreateMs(long createMs) {
        this.createMs = createMs;
    }

    public void setThunks(List<Thunk> thunks) {
        this.thunks = thunks;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public void setCallback(Callback<CommonResponse, Boolean> callback) {
        this.callback = callback;
    }

    public void setEndTransaction(boolean isEndTransaction) {
        this.isEndTransaction = isEndTransaction;
    }

    public void setNode(InetSocketAddress node) {
        this.node = node;
    }

    private static final Logger log = LoggerFactory.getLogger(ProducerBatch.class);
    private String topic;
    private long createMs;
    private List<Thunk> thunks;
    private int batchSize;

    public String getTopic() {
        return this.topic;
    }

    private int length;
    private Callback<CommonResponse, Boolean> callback;
    private boolean isEndTransaction;
    private InetSocketAddress node;

    public long getCreateMs() {
        return this.createMs;
    }

    public List<Thunk> getThunks() {
        return this.thunks;
    }

    public int getBatchSize() {
        return this.batchSize;
    }

    public int getLength() {
        return this.length;
    }

    public Callback<CommonResponse, Boolean> getCallback() {
        return this.callback;
    }

    public boolean isEndTransaction() {
        return this.isEndTransaction;
    }

    public InetSocketAddress getNode() {
        return this.node;
    }

    public ProducerBatch(String topic, boolean isOneway, boolean isEndTransaction, int batchSize) {
        this.topic = topic;
        if (!isOneway) {
            this.callback = new CompletionCallback();
        }
        this.thunks = new ArrayList<>(batchSize);
        this.createMs = MillisecondClock.now();
        this.isEndTransaction = isEndTransaction;
    }

    public ProducerBatch(String topic, InetSocketAddress node, boolean isOneway, boolean isEndTransaction, int batchSize) {
        this.topic = topic;
        if (!isOneway) {
            this.callback = new CompletionCallback();
        }
        this.thunks = new ArrayList<>(batchSize);
        this.node = node;
        this.createMs = MillisecondClock.now();
        this.isEndTransaction = isEndTransaction;
    }

    public SyncFuture<SpecialSendResult> append(MessageRecord<byte[]> record, MessageRecord recordObject, SendCallback callback) {
        SyncFuture<SpecialSendResult> future = new SyncFuture();
        this.thunks.add(new Thunk(record, recordObject, callback, future));
        this.length += ((byte[]) record.getMessage()).length;
        return future;
    }

    public void append(String msgId, TransactionStatus status, Throwable localException) {
        this.thunks.add(new Thunk(msgId, status, localException));
    }

    public List<MessageRecord<byte[]>> getRecordBatch() {
        List<MessageRecord<byte[]>> records = new ArrayList<>(this.thunks.size());
        for (Thunk thunk : this.thunks) {
            records.add(thunk.record);
        }
        return records;
    }

    public List<EndTransactionMessage> getEndTransactionBatch() {
        List<EndTransactionMessage> endTransactionMessages = new ArrayList<>(this.thunks.size());
        for (Thunk thunk : this.thunks) {
            endTransactionMessages.add(thunk.endTransactionMessage);
        }
        return endTransactionMessages;
    }


    private static final class Thunk {
        final MessageRecord<byte[]> record;


        final MessageRecord recordObject;

        final SendCallback callback;

        final SyncFuture<SpecialSendResult> future;

        final EndTransactionMessage endTransactionMessage;


        Thunk(MessageRecord<byte[]> record, MessageRecord recordObject, SendCallback callback, SyncFuture<SpecialSendResult> future) {
            this.record = record;
            this.recordObject = recordObject;
            this.callback = callback;
            this.future = future;
            this.endTransactionMessage = null;
        }

        Thunk(String msgId, TransactionStatus status, Throwable localException) {
            this.record = null;
            this.recordObject = null;
            this.callback = null;
            this.future = null;
            this.endTransactionMessage = new EndTransactionMessage(msgId, status, localException);
        }
    }


    class CompletionCallback
            implements Callback<CommonResponse, Boolean> {
        public Boolean call(CommonResponse param) {
            if (param.getResultCode() != ResultCodeEnum.SUCCESS.getCode()) {
                onException(param.getResultMsg());
            } else if (param instanceof SendMessageResponse) {
                SendMessageResponse response = (SendMessageResponse) param;

                int i = 0;
                for (Thunk thunk : ProducerBatch.this.thunks) {
                    ProduceSendResult result = response.getResult(i);
                    if (thunk.future != null) {
                        if (result.getError() != null) {
                            thunk.future.setError(result.getError());
                        } else if (param.getLocalException() != null) {
                            thunk.future.setError(param.getLocalException());
                        } else {
                            thunk.future.setResponse(convert(result, param.getNode()));
                        }
                    }
                    if (thunk.callback != null) {
                        if (result.getError() != null) {
                            thunk.callback.onException(new OnExceptionContext(ProducerBatch.this
                                    .topic, thunk.recordObject, new MqClientException(result

                                    .getError())));
                        } else if (param.getLocalException() != null) {
                            thunk.callback.onException(new OnExceptionContext(ProducerBatch.this
                                    .topic, thunk.recordObject, new MqClientException(param

                                    .getLocalException())));
                        } else {
                            thunk.callback.onSuccess((SendResult) convert(result, param.getNode()));
                        }
                    }
                    i++;
                }
            } else {
                ProducerBatch.log.warn("Invalid response {}.", param);
                onException("Invalid response.");
            }


            return Boolean.valueOf(true);
        }


        private void onException(String exception) {
            for (Thunk thunk : ProducerBatch.this.thunks) {
                if (thunk.future != null) {
                    thunk.future.setError((RuntimeException) new MqClientException(exception));
                }
                if (thunk.callback != null) {
                    thunk.callback.onException(new OnExceptionContext(ProducerBatch.this
                            .topic, thunk.recordObject, new MqClientException(exception)));
                }
            }
        }


        private SpecialSendResult convert(ProduceSendResult result, InetSocketAddress node) {
            SpecialSendResult sendResult = new SpecialSendResult();
            sendResult.setMsgId(result.getSid());
            sendResult.setKey(result.getKey());
            sendResult.setTimestamp(result.getTimestamp());
            sendResult.setNode(node);
            return sendResult;
        }
    }
}


