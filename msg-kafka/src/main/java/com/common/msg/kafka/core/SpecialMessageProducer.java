package com.common.msg.kafka.core;

import com.common.msg.api.MessageAccessor;
import com.common.msg.api.common.SendTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.ThreadUtil;
import com.common.msg.kafka.event.KafkaEndTransactionEvent;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.kafka.network.NetworkService;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.SendResult;
import com.common.msg.api.producer.SpecialSendResult;
import com.common.msg.api.transaction.TransactionStatus;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SpecialMessageProducer {
    private static final Logger log = LoggerFactory.getLogger(SpecialMessageProducer.class);

    private final NetworkService networkService;

    private final MessageAccumulate messageAccumulate;

    private final long timeout;

    private final boolean auditSwitch;

    private final int retries;

    private final int maxLength;

    private volatile boolean isConnected;


    public SpecialMessageProducer(NetworkService networkService, MessageAccumulate messageAccumulate) {
        this.networkService = networkService;
        this.messageAccumulate = messageAccumulate;
        this.timeout = ConfigManager.getLong("mq.special.message.timeout.ms", 20000L);
        String auditSwitch = ConfigManager.getString("mq.special.message.audit.switch", "on");
        this.auditSwitch = ("on".equalsIgnoreCase(auditSwitch) || "true".equalsIgnoreCase(auditSwitch));
        this.retries = ConfigManager.getInt("mq.special.message.retry.times", 3);
        this.maxLength = 63488;
    }


    public void sendTransactionMessage(KafkaProduceEvent event) {
        if (!event.getSendType().equals(SendTypeEnum.SYCN)) {
            throw new MqConfigException("Transaction message must be send by sycn with [send] method .");
        }
        if (event.getMessageRecord().getStartDeliverTime() > 0L) {
            throw new MqConfigException("Not support delay deliver with in transaction send yet.");
        }
        if (event.getExecuter() == null) {
            throw new MqConfigException("Local transactionExecuter is null.");
        }
        if (event.getConfig().getChecker() == null) {
            throw new MqConfigException("Local transactionChecker is null.");
        }
        if ((event.getPayload()).length > this.maxLength) {
            throw new MqConfigException("Transaction message length over 62K bytes.");
        }
        SpecialSendResult result = null;
        int i = 0;
        while (i <= this.retries && !Thread.currentThread().isInterrupted()) {
            try {
                result = (SpecialSendResult) this.messageAccumulate.appendTransactionMessage(event).get(this.timeout, TimeUnit.MILLISECONDS);
                break;
            } catch (Throwable e) {
                if (i == 0) {
                    log.warn("Send transaction message failed, waiting for retry." + messageInfo(event), e);
                    ThreadUtil.sleep(1000L, log);
                } else {
                    if (i == this.retries) {
                        log.error("Resend transaction message failed " + i + "/" + this.retries + " times." + messageInfo(event));
                        throw new MqClientException("Send transaction message failed. ", e);
                    }

                    log.warn("Resend transaction message failed " + i + "/" + this.retries + " times, waiting for retry." + messageInfo(event), e);
                    ThreadUtil.sleep((1000 * (i + 1)), log);
                }


                i++;
            }
        }
        if (null == result) {
            throw new MqClientException("Send transaction message failed in timeout [" + this.timeout + "]ms.");
        }

        if (!StringUtil.isNullOrEmpty(result.getMsgId())) {
            TransactionStatus status = TransactionStatus.Unknown;
            Throwable localException = null;
            try {
                MessageAccessor.putSystemProperties(event.getMessageRecord(), "__SID", result.getMsgId());
                status = event.getExecuter().execute(event.getMessageRecord(), event.getArg());
                if (this.auditSwitch) {
                    log.info("Send transaction half success. status[{}] msgId[{}] key[{}] node[{}]", new Object[]{status, result

                            .getMsgId(),
                            StringUtil.isNullOrEmpty(result.getKey()) ? "" : result.getKey(), result
                            .getNode()});
                }
                log.debug("Send transaction half success. status[{}] msgId[{}] key[{}] node[{}]", new Object[]{status, result

                        .getMsgId(),
                        StringUtil.isNullOrEmpty(result.getKey()) ? "" : result.getKey(), result
                        .getNode()});
            } catch (Throwable e) {
                localException = e;
            }
            endTransaction(event.getConfig(), result.getMsgId(), result.getNode(), status, localException);
            if (localException != null) {
                throw new MqClientException("Local transactionExecuter failed. ", localException);
            }
        }
        event.setResult((SendResult) result);
    }

    public void sendDelayMessage(KafkaProduceEvent event) {
        if (!this.isConnected) {

            this.networkService.connect();
            this.isConnected = true;
        }
        if ((event.getPayload()).length > this.maxLength) {
            throw new MqConfigException("Delay message length over 62K bytes.");
        }
        int i = 0;
        while (i <= this.retries && !Thread.currentThread().isInterrupted()) {
            try {
                if (SendTypeEnum.SYCN.equals(event.getSendType())) {

                    SendResult result = (SendResult) this.messageAccumulate.appendDelayMessage(event).get(this.timeout, TimeUnit.MILLISECONDS);
                    event.setResult(result);
                    break;
                }
                this.messageAccumulate.appendDelayMessage(event);

                break;
            } catch (Throwable e) {
                if (i == 0) {
                    log.warn("Send delay message failed, waiting for retry." + messageInfo(event), e);
                    ThreadUtil.sleep(1000L, log);
                } else {
                    if (i == this.retries) {
                        log.error("Resend delay message failed " + i + "/" + this.retries + " times." + messageInfo(event));
                        throw new MqClientException("Send delay message failed. ", e);
                    }

                    log.warn("Resend delay message failed " + i + "/" + this.retries + " times, waiting for retry." + messageInfo(event), e);
                    ThreadUtil.sleep((1000 * (i + 1)), log);
                }


                i++;
            }
        }
    }

    void sendEndTransactionMessage(ProducerConfig config, String msgId, InetSocketAddress node, TransactionStatus status, Throwable e) {
        endTransaction(config, msgId, node, status, e);
    }

    private void endTransaction(ProducerConfig config, String msgId, InetSocketAddress node, TransactionStatus status, Throwable e) {
        KafkaEndTransactionEvent event = new KafkaEndTransactionEvent();
        event.setConfig((MqConfig) config);
        event.setMsgId(msgId);
        event.setNode(node);
        event.setSendType(SendTypeEnum.ONEWAY);
        event.setStatus(status);
        event.setLocalException(e);
        this.messageAccumulate.appendEndTransactionMessage(event);
    }

    private String messageInfo(KafkaProduceEvent event) {
        StringBuilder builder = new StringBuilder();
        builder.append(" message:topic[").append(event.getConfig().getTopic()).append("]");
        builder.append(" key[").append(StringUtil.isNullOrEmpty(event.getMessageRecord().getKey()) ? "" : event.getMessageRecord().getKey()).append("]");
        return builder.toString();
    }
}


