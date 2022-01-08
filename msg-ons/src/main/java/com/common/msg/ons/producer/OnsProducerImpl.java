package com.zhongan.msg.ons.producer;

import com.alibaba.ons.open.trace.core.common.OnsTraceDispatcherType;
import com.alibaba.ons.open.trace.core.dispatch.AsyncDispatcher;
import com.alibaba.ons.open.trace.core.dispatch.NameServerAddressSetter;
import com.alibaba.ons.open.trace.core.dispatch.impl.AsyncArrayDispatcher;
//import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.OnExceptionContext;
import com.aliyun.openservices.ons.api.Producer;
//import com.aliyun.openservices.ons.api.SendCallback;
//import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.exception.ONSClientException;
import com.aliyun.openservices.ons.api.impl.rocketmq.FAQ;
import com.aliyun.openservices.ons.api.impl.rocketmq.ONSClientAbstract;
import com.aliyun.openservices.ons.api.impl.rocketmq.ONSUtil;
import com.aliyun.openservices.ons.api.impl.rocketmq.OnsClientRPCHook;
import com.aliyun.openservices.ons.api.impl.tracehook.OnsClientSendMessageHookImpl;
import com.aliyun.openservices.ons.api.impl.util.ClientLoggerUtil;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.hook.SendMessageHook;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendCallback;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.UtilAll;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageClientIDSetter;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageQueue;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.logging.InternalLogger;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.remoting.RPCHook;
import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
//import com.zhongan.msg.api.exception.MqClientException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;










public class OnsProducerImpl extends ONSClientAbstract implements Producer, OnsProducer
{
    private static final InternalLogger log = ClientLoggerUtil.getClientLogger();
    private final DefaultMQProducer defaultMQProducer;

    public OnsProducerImpl(Properties properties) {
        super(properties);

        String producerGroup = properties.getProperty("GROUP_ID", properties.getProperty("ProducerId"));
        if (StringUtils.isEmpty(producerGroup)) {
            producerGroup = "__ONS_PRODUCER_DEFAULT_GROUP";
        }

        this
                .defaultMQProducer = new DefaultMQProducer(getNamespace(), producerGroup, (RPCHook)new OnsClientRPCHook(this.sessionCredentials));


        this.defaultMQProducer.setProducerGroup(producerGroup);

        boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty("isVipChannelEnabled", "false"));
        this.defaultMQProducer.setVipChannelEnabled(isVipChannelEnabled);

        if (properties.containsKey("SendMsgTimeoutMillis")) {
            this.defaultMQProducer.setSendMsgTimeout(Integer.valueOf(properties.get("SendMsgTimeoutMillis").toString()).intValue());
        } else {
            this.defaultMQProducer.setSendMsgTimeout(5000);
        }

        if (properties.containsKey("exactlyOnceDelivery")) {
            this.defaultMQProducer.setAddExtendUniqInfo(Boolean.valueOf(properties.get("exactlyOnceDelivery").toString()).booleanValue());
        }

        String instanceName = properties.getProperty("InstanceName", buildIntanceName());
        this.defaultMQProducer.setInstanceName(instanceName);
        this.defaultMQProducer.setNamesrvAddr(getNameServerAddr());

        this.defaultMQProducer.setMaxMessageSize(4194304);

        String msgTraceSwitch = properties.getProperty("MsgTraceSwitch");
        if (!UtilAll.isBlank(msgTraceSwitch) && !Boolean.parseBoolean(msgTraceSwitch)) {
            log.info("MQ Client Disable the Trace Hook!");
        } else {
            try {
                Properties tempProperties = new Properties();
                tempProperties.put("AccessKey", this.sessionCredentials.getAccessKey());
                tempProperties.put("SecretKey", this.sessionCredentials.getSecretKey());
                tempProperties.put("MaxMsgSize", "128000");
                tempProperties.put("AsyncBufferSize", "2048");
                tempProperties.put("MaxBatchNum", "100");
                tempProperties.put("InstanceName", "PID_CLIENT_INNER_TRACE_PRODUCER");
                tempProperties.put("DispatcherType", OnsTraceDispatcherType.PRODUCER.name());
                AsyncArrayDispatcher dispatcher = new AsyncArrayDispatcher(tempProperties, this.sessionCredentials, new NameServerAddressSetter()
                {
                    public String getNewNameServerAddress() {
                        return OnsProducerImpl.this.getNameServerAddr();
                    }
                });
                dispatcher.setHostProducer(this.defaultMQProducer.getDefaultMQProducerImpl());
                this.traceDispatcher = (AsyncDispatcher)dispatcher;
                this.defaultMQProducer.getDefaultMQProducerImpl().registerSendMessageHook((SendMessageHook)new OnsClientSendMessageHookImpl(this.traceDispatcher));
            }
            catch (Throwable e) {
                log.error("system mqtrace hook init failed ,maybe can't send msg trace data.", e);
            }
        }
    }


    protected void updateNameServerAddr(String newAddrs) {
        this.defaultMQProducer.setNamesrvAddr(newAddrs);
        this.defaultMQProducer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().updateNameServerAddressList(newAddrs);
    }


    public void start() {
        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQProducer.start();
                super.start();
            }
        } catch (Exception e) {
            throw new ONSClientException(e.getMessage());
        }
    }


    public void startup() {
        start();
    }


    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQProducer.shutdown();
        }
        super.shutdown();
    }


    public SendResult send(Message message) {
        checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());
        Message msgRMQ = ONSUtil.msgConvert(message);
        try {
            SendResult sendResultRMQ;
            if (!UtilAll.isBlank(message.getShardingKey())) {

                sendResultRMQ = this.defaultMQProducer.send(msgRMQ, new MessageQueueSelector()
                {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object shardingKey)
                    {
                        int select = Math.abs(shardingKey.hashCode());
                        if (select < 0) {
                            select = 0;
                        }
                        return mqs.get(select % mqs.size());
                    }
                },  message.getShardingKey());
            } else {
                sendResultRMQ = this.defaultMQProducer.send(msgRMQ);
            }

            message.setMsgID(sendResultRMQ.getMsgId());
            SendResult sendResult = new SendResult();
            sendResult.setTopic(sendResultRMQ.getMessageQueue().getTopic());
            sendResult.setMessageId(sendResultRMQ.getMsgId());
            return sendResult;
        } catch (Exception e) {
            log.error(String.format("Send message Exception, %s", new Object[] { message }), e);
            throw checkProducerException(message.getTopic(), message.getMsgID(), e);
        }
    }


    public void sendOneway(Message message) {
        checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());
        Message msgRMQ = ONSUtil.msgConvert(message);
        try {
            if (!UtilAll.isBlank(message.getShardingKey())) {
                this.defaultMQProducer.sendOneway(msgRMQ, new MessageQueueSelector()
                {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object shardingKey)
                    {
                        int select = Math.abs(shardingKey.hashCode());
                        if (select < 0) {
                            select = 0;
                        }
                        return mqs.get(select % mqs.size());
                    }
                },  message.getShardingKey());
            } else {
                this.defaultMQProducer.sendOneway(msgRMQ);
            }
            message.setMsgID(MessageClientIDSetter.getUniqID(msgRMQ));
        } catch (Exception e) {
            log.error(String.format("Send message oneway Exception, %s", new Object[] { message }), e);
            throw checkProducerException(message.getTopic(), message.getMsgID(), e);
        }
    }


    public void sendAsync(Message message, SendCallback sendCallback) {
        checkONSProducerServiceState(this.defaultMQProducer.getDefaultMQProducerImpl());
        Message msgRMQ = ONSUtil.msgConvert(message);
        try {
            if (!UtilAll.isBlank(message.getShardingKey())) {
                this.defaultMQProducer.send(msgRMQ, new MessageQueueSelector()
                {
                    public MessageQueue select(List<MessageQueue> mqs, Message msg, Object shardingKey)
                    {
                        int select = Math.abs(shardingKey.hashCode());
                        if (select < 0) {
                            select = 0;
                        }
                        return mqs.get(select % mqs.size());
                    }
                },  message.getShardingKey(), sendCallbackConvert(message, sendCallback));
            } else {
                this.defaultMQProducer.send(msgRMQ, sendCallbackConvert(message, sendCallback));
            }
            message.setMsgID(MessageClientIDSetter.getUniqID(msgRMQ));
        } catch (Exception e) {
            log.error(String.format("Send message async Exception, %s", new Object[] { message }), e);
            throw checkProducerException(message.getTopic(), message.getMsgID(), e);
        }
    }


    public SendResult send(Message message, LocalTransactionExecuter executer, Object arg) throws MQClientException {
        throw new MqClientException("Can't send transaction message without config [TransactionExecuter].");
    }


    public void setCallbackExecutor(ExecutorService callbackExecutor) {
        this.defaultMQProducer.setCallbackExecutor(callbackExecutor);
    }

    public DefaultMQProducer getDefaultMQProducer() {
        return this.defaultMQProducer;
    }


    private SendCallback sendCallbackConvert(final Message message, final SendCallback sendCallback) {
        return new SendCallback()
        {
            public void onSuccess(SendResult sendResult) {
                sendCallback.onSuccess(OnsProducerImpl.this.sendResultConvert(sendResult));
            }


            public void onException(Throwable e) {
                String topic = message.getTopic();
                String msgId = message.getMsgID();
                ONSClientException onsEx = OnsProducerImpl.this.checkProducerException(topic, msgId, e);
                OnExceptionContext context = new OnExceptionContext();
                context.setTopic(topic);
                context.setMessageId(msgId);
                context.setException(onsEx);
                sendCallback.onException(context);
            }
        };
    }


    private SendResult sendResultConvert(SendResult rmqSendResult) {
        SendResult sendResult = new SendResult();
        sendResult.setTopic(rmqSendResult.getMessageQueue().getTopic());
        sendResult.setMessageId(rmqSendResult.getMsgId());
        return sendResult;
    }

    private ONSClientException checkProducerException(String topic, String msgId, Throwable e) {
        if (e instanceof MQClientException)
        {
            if (e.getCause() != null) {

                if (e.getCause() instanceof com.aliyun.openservices.shade.com.alibaba.rocketmq.remoting.exception.RemotingConnectException) {
                    return new ONSClientException(
                            FAQ.errorMessage(String.format("Connect broker failed, Topic=%s, msgId=%s", new Object[] { topic, msgId }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&connect_broker_failed"));
                }

                if (e.getCause() instanceof com.aliyun.openservices.shade.com.alibaba.rocketmq.remoting.exception.RemotingTimeoutException) {
                    return new ONSClientException(FAQ.errorMessage(String.format("Send message to broker timeout, %dms, Topic=%s, msgId=%s", new Object[] {
                            Integer.valueOf(this.defaultMQProducer.getSendMsgTimeout()), topic, msgId
                    }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&send_msg_failed"));
                }
                if (e.getCause() instanceof MQBrokerException) {
                    MQBrokerException excep = (MQBrokerException)e.getCause();
                    return new ONSClientException(FAQ.errorMessage(
                            String.format("Receive a broker exception, Topi=%s, msgId=%s, %s", new Object[] { topic, msgId, excep.getErrorMessage() }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&broker_response_exception"));
                }

            }
            else {

                MQClientException excep = (MQClientException)e;
                if (-1 == excep.getResponseCode())
                    return new ONSClientException(
                            FAQ.errorMessage(String.format("Topic does not exist, Topic=%s, msgId=%s", new Object[] { topic, msgId }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&topic_not_exist"));
                if (13 == excep.getResponseCode()) {
                    return new ONSClientException(
                            FAQ.errorMessage(String.format("ONS Client check message exception, Topic=%s, msgId=%s", new Object[] { topic, msgId }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&msg_check_failed"));
                }
            }
        }


        return new ONSClientException("defaultMQProducer send exception", e);
    }
}


