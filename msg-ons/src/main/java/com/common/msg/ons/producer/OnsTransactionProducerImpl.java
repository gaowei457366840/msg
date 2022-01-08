/*     */ package com.common.msg.ons.producer;
/*     */ import com.alibaba.ons.open.trace.core.dispatch.NameServerAddressSetter;
/*     */ import com.alibaba.ons.open.trace.core.dispatch.impl.AsyncArrayDispatcher;
/*     */ import com.aliyun.openservices.ons.api.Message;
/*     */ import com.aliyun.openservices.ons.api.OnExceptionContext;
/*     */ import com.aliyun.openservices.ons.api.SendCallback;
/*     */ import com.aliyun.openservices.ons.api.SendResult;
/*     */ import com.aliyun.openservices.ons.api.exception.ONSClientException;
/*     */ import com.aliyun.openservices.ons.api.impl.rocketmq.FAQ;
/*     */ import com.aliyun.openservices.ons.api.impl.rocketmq.ONSUtil;
/*     */ import com.aliyun.openservices.ons.api.impl.util.ClientLoggerUtil;
/*     */ import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
/*     */ import com.aliyun.openservices.ons.api.transaction.TransactionProducer;
/*     */ import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQBrokerException;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.LocalTransactionExecuter;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.LocalTransactionState;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.MessageQueueSelector;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendCallback;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.TransactionCheckListener;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.TransactionMQProducer;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.TransactionSendResult;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.UtilAll;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageAccessor;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageClientIDSetter;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageQueue;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.logging.InternalLogger;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.remoting.RPCHook;
/*     */ import com.aliyun.openservices.shade.org.apache.commons.lang3.StringUtils;
/*     */ import java.util.List;
/*     */ import java.util.Properties;
/*     */ import java.util.concurrent.ExecutorService;
/*     */ 
/*     */ public class OnsTransactionProducerImpl extends ONSClientAbstract implements TransactionProducer, OnsProducer {
/*  38 */   private static final InternalLogger log = ClientLoggerUtil.getClientLogger();
/*  39 */   TransactionMQProducer transactionMQProducer = null;
/*     */   private Properties properties;
/*     */   
/*     */   public OnsTransactionProducerImpl(Properties properties, TransactionCheckListener transactionCheckListener) {
/*  43 */     super(properties);
/*  44 */     this.properties = properties;
/*  45 */     String producerGroup = properties.getProperty("GROUP_ID", properties.getProperty("ProducerId"));
/*  46 */     if (StringUtils.isEmpty(producerGroup)) {
/*  47 */       producerGroup = "__ONS_PRODUCER_DEFAULT_GROUP";
/*     */     }
/*  49 */     this
/*  50 */       .transactionMQProducer = new TransactionMQProducer(getNamespace(), producerGroup, (RPCHook)new OnsClientRPCHook(this.sessionCredentials));
/*     */     
/*  52 */     boolean isVipChannelEnabled = Boolean.parseBoolean(properties.getProperty("isVipChannelEnabled", "false"));
/*  53 */     this.transactionMQProducer.setVipChannelEnabled(isVipChannelEnabled);
/*     */     
/*  55 */     String instanceName = properties.getProperty("InstanceName", buildIntanceName());
/*  56 */     this.transactionMQProducer.setInstanceName(instanceName);
/*     */     
/*  58 */     boolean addExtendUniqInfo = Boolean.parseBoolean(properties.getProperty("exactlyOnceDelivery", "false"));
/*  59 */     this.transactionMQProducer.setAddExtendUniqInfo(addExtendUniqInfo);
/*     */     
/*  61 */     this.transactionMQProducer.setTransactionCheckListener(transactionCheckListener);
/*     */     
/*  63 */     String msgTraceSwitch = properties.getProperty("MsgTraceSwitch");
/*  64 */     if (!UtilAll.isBlank(msgTraceSwitch) && !Boolean.parseBoolean(msgTraceSwitch)) {
/*  65 */       log.info("MQ Client Disable the Trace Hook!");
/*     */     } else {
/*     */       try {
/*  68 */         Properties tempProperties = new Properties();
/*  69 */         tempProperties.put("AccessKey", this.sessionCredentials.getAccessKey());
/*  70 */         tempProperties.put("SecretKey", this.sessionCredentials.getSecretKey());
/*  71 */         tempProperties.put("MaxMsgSize", "128000");
/*  72 */         tempProperties.put("AsyncBufferSize", "2048");
/*  73 */         tempProperties.put("MaxBatchNum", "100");
/*  74 */         tempProperties.put("InstanceName", "PID_CLIENT_INNER_TRACE_PRODUCER");
/*  75 */         tempProperties.put("DispatcherType", OnsTraceDispatcherType.PRODUCER.name());
/*  76 */         AsyncArrayDispatcher dispatcher = new AsyncArrayDispatcher(tempProperties, this.sessionCredentials, new NameServerAddressSetter()
/*     */             {
/*     */               public String getNewNameServerAddress() {
/*  79 */                 return OnsTransactionProducerImpl.this.getNameServerAddr();
/*     */               }
/*     */             });
/*  82 */         dispatcher.setHostProducer(this.transactionMQProducer.getDefaultMQProducerImpl());
/*  83 */         this.traceDispatcher = (AsyncDispatcher)dispatcher;
/*  84 */         this.transactionMQProducer.getDefaultMQProducerImpl().registerSendMessageHook((SendMessageHook)new OnsClientSendMessageHookImpl(this.traceDispatcher));
/*     */       }
/*  86 */       catch (Throwable e) {
/*  87 */         log.error("system mqtrace hook init failed ,maybe can't send msg trace data", e);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void start() {
/*  94 */     if (this.started.compareAndSet(false, true)) {
/*  95 */       if (this.transactionMQProducer.getTransactionCheckListener() == null) {
/*  96 */         throw new IllegalArgumentException("TransactionCheckListener is null");
/*     */       }
/*     */       
/*  99 */       this.transactionMQProducer.setNamesrvAddr(this.nameServerAddr);
/*     */       try {
/* 101 */         this.transactionMQProducer.start();
/* 102 */         super.start();
/* 103 */       } catch (MQClientException e) {
/* 104 */         throw new RuntimeException(e);
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   protected void updateNameServerAddr(String newAddrs) {
/* 111 */     this.transactionMQProducer.setNamesrvAddr(newAddrs);
/* 112 */     this.transactionMQProducer.getDefaultMQProducerImpl().getmQClientFactory().getMQClientAPIImpl().updateNameServerAddressList(newAddrs);
/*     */   }
/*     */ 
/*     */   
/*     */   public void startup() {
/* 117 */     start();
/*     */   }
/*     */ 
/*     */   
/*     */   public void shutdown() {
/* 122 */     if (this.started.compareAndSet(true, false)) {
/* 123 */       this.transactionMQProducer.shutdown();
/*     */     }
/* 125 */     super.shutdown();
/*     */   }
/*     */   
/*     */   public SendResult send(Message message) {
/* 129 */     checkONSProducerServiceState(this.transactionMQProducer.getDefaultMQProducerImpl());
/* 130 */     Message msgRMQ = ONSUtil.msgConvert(message);
/*     */     try {
/*     */       SendResult sendResultRMQ;
/* 133 */       if (!UtilAll.isBlank(message.getShardingKey())) {
/*     */         
/* 135 */         sendResultRMQ = this.transactionMQProducer.send(msgRMQ, new MessageQueueSelector()
/*     */             {
/*     */               public MessageQueue select(List<MessageQueue> mqs, Message msg, Object shardingKey)
/*     */               {
/* 139 */                 int select = Math.abs(shardingKey.hashCode());
/* 140 */                 if (select < 0) {
/* 141 */                   select = 0;
/*     */                 }
/* 143 */                 return mqs.get(select % mqs.size());
/*     */               }
/* 145 */             },  message.getShardingKey());
/*     */       } else {
/* 147 */         sendResultRMQ = this.transactionMQProducer.send(msgRMQ);
/*     */       } 
/*     */       
/* 150 */       message.setMsgID(sendResultRMQ.getMsgId());
/* 151 */       SendResult sendResult = new SendResult();
/* 152 */       sendResult.setTopic(sendResultRMQ.getMessageQueue().getTopic());
/* 153 */       sendResult.setMessageId(sendResultRMQ.getMsgId());
/* 154 */       return sendResult;
/* 155 */     } catch (Exception e) {
/* 156 */       log.error(String.format("Send message Exception, %s", new Object[] { message }), e);
/* 157 */       throw checkProducerException(message.getTopic(), message.getMsgID(), e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void sendOneway(Message message) {
/* 163 */     checkONSProducerServiceState(this.transactionMQProducer.getDefaultMQProducerImpl());
/* 164 */     Message msgRMQ = ONSUtil.msgConvert(message);
/*     */     try {
/* 166 */       if (!UtilAll.isBlank(message.getShardingKey())) {
/* 167 */         this.transactionMQProducer.sendOneway(msgRMQ, new MessageQueueSelector()
/*     */             {
/*     */               public MessageQueue select(List<MessageQueue> mqs, Message msg, Object shardingKey)
/*     */               {
/* 171 */                 int select = Math.abs(shardingKey.hashCode());
/* 172 */                 if (select < 0) {
/* 173 */                   select = 0;
/*     */                 }
/* 175 */                 return mqs.get(select % mqs.size());
/*     */               }
/* 177 */             },  message.getShardingKey());
/*     */       } else {
/* 179 */         this.transactionMQProducer.sendOneway(msgRMQ);
/*     */       } 
/* 181 */       message.setMsgID(MessageClientIDSetter.getUniqID(msgRMQ));
/* 182 */     } catch (Exception e) {
/* 183 */       log.error(String.format("Send message oneway Exception, %s", new Object[] { message }), e);
/* 184 */       throw checkProducerException(message.getTopic(), message.getMsgID(), e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void sendAsync(Message message, SendCallback sendCallback) {
/* 190 */     checkONSProducerServiceState(this.transactionMQProducer.getDefaultMQProducerImpl());
/* 191 */     Message msgRMQ = ONSUtil.msgConvert(message);
/*     */     try {
/* 193 */       if (!UtilAll.isBlank(message.getShardingKey())) {
/* 194 */         this.transactionMQProducer.send(msgRMQ, new MessageQueueSelector()
/*     */             {
/*     */               public MessageQueue select(List<MessageQueue> mqs, Message msg, Object shardingKey)
/*     */               {
/* 198 */                 int select = Math.abs(shardingKey.hashCode());
/* 199 */                 if (select < 0) {
/* 200 */                   select = 0;
/*     */                 }
/* 202 */                 return mqs.get(select % mqs.size());
/*     */               }
/* 204 */             },  message.getShardingKey(), sendCallbackConvert(message, sendCallback));
/*     */       } else {
/* 206 */         this.transactionMQProducer.send(msgRMQ, sendCallbackConvert(message, sendCallback));
/*     */       } 
/* 208 */       message.setMsgID(MessageClientIDSetter.getUniqID(msgRMQ));
/* 209 */     } catch (Exception e) {
/* 210 */       log.error(String.format("Send message async Exception, %s", new Object[] { message }), e);
/* 211 */       throw checkProducerException(message.getTopic(), message.getMsgID(), e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public SendResult send(final Message message, final LocalTransactionExecuter executer, Object arg) {
/* 217 */     checkONSProducerServiceState(this.transactionMQProducer.getDefaultMQProducerImpl());
/* 218 */     Message msgRMQ = ONSUtil.msgConvert(message);
/* 219 */     MessageAccessor.putProperty(msgRMQ, "ProducerId", (String)this.properties.get("ProducerId"));
/* 220 */     TransactionSendResult sendResultRMQ = null;
/*     */     try {
/* 222 */       sendResultRMQ = this.transactionMQProducer.sendMessageInTransaction(msgRMQ, new LocalTransactionExecuter()
/*     */           {
/*     */ 
/*     */             
/*     */             public LocalTransactionState executeLocalTransactionBranch(Message msg, Object arg)
/*     */             {
/* 228 */               String msgId = msg.getProperty("__transactionId__");
/* 229 */               message.setMsgID(msgId);
/* 230 */               TransactionStatus transactionStatus = executer.execute(message, arg);
/* 231 */               if (TransactionStatus.CommitTransaction == transactionStatus)
/* 232 */                 return LocalTransactionState.COMMIT_MESSAGE; 
/* 233 */               if (TransactionStatus.RollbackTransaction == transactionStatus) {
/* 234 */                 return LocalTransactionState.ROLLBACK_MESSAGE;
/*     */               }
/* 236 */               return LocalTransactionState.UNKNOW;
/*     */             }
/*     */           }arg);
/* 239 */     } catch (Exception e) {
/* 240 */       throw new RuntimeException(e);
/*     */     } 
/* 242 */     if (sendResultRMQ.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE)
/*     */     {
/* 244 */       throw new RuntimeException("local transaction branch failed ,so transaction rollback");
/*     */     }
/* 246 */     SendResult sendResult = new SendResult();
/* 247 */     sendResult.setMessageId(sendResultRMQ.getMsgId());
/* 248 */     sendResult.setTopic(sendResultRMQ.getMessageQueue().getTopic());
/* 249 */     return sendResult;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setCallbackExecutor(ExecutorService callbackExecutor) {
/* 254 */     this.transactionMQProducer.setCallbackExecutor(callbackExecutor);
/*     */   }
/*     */ 
/*     */   
/*     */   private SendCallback sendCallbackConvert(final Message message, final SendCallback sendCallback) {
/* 259 */     return new SendCallback()
/*     */       {
/*     */         public void onSuccess(SendResult sendResult) {
/* 262 */           sendCallback.onSuccess(OnsTransactionProducerImpl.this.sendResultConvert(sendResult));
/*     */         }
/*     */ 
/*     */         
/*     */         public void onException(Throwable e) {
/* 267 */           String topic = message.getTopic();
/* 268 */           String msgId = message.getMsgID();
/* 269 */           ONSClientException onsEx = OnsTransactionProducerImpl.this.checkProducerException(topic, msgId, e);
/* 270 */           OnExceptionContext context = new OnExceptionContext();
/* 271 */           context.setTopic(topic);
/* 272 */           context.setMessageId(msgId);
/* 273 */           context.setException(onsEx);
/* 274 */           sendCallback.onException(context);
/*     */         }
/*     */       };
/*     */   }
/*     */ 
/*     */   
/*     */   private SendResult sendResultConvert(SendResult rmqSendResult) {
/* 281 */     SendResult sendResult = new SendResult();
/* 282 */     sendResult.setTopic(rmqSendResult.getMessageQueue().getTopic());
/* 283 */     sendResult.setMessageId(rmqSendResult.getMsgId());
/* 284 */     return sendResult;
/*     */   }
/*     */   
/*     */   private ONSClientException checkProducerException(String topic, String msgId, Throwable e) {
/* 288 */     if (e instanceof MQClientException)
/*     */     {
/* 290 */       if (e.getCause() != null) {
/*     */         
/* 292 */         if (e.getCause() instanceof com.aliyun.openservices.shade.com.alibaba.rocketmq.remoting.exception.RemotingConnectException) {
/* 293 */           return new ONSClientException(
/* 294 */               FAQ.errorMessage(String.format("Connect broker failed, Topic=%s, msgId=%s", new Object[] { topic, msgId }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&connect_broker_failed"));
/*     */         }
/*     */         
/* 297 */         if (e.getCause() instanceof com.aliyun.openservices.shade.com.alibaba.rocketmq.remoting.exception.RemotingTimeoutException) {
/* 298 */           return new ONSClientException(FAQ.errorMessage(String.format("Send message to broker timeout, %dms, Topic=%s, msgId=%s", new Object[] {
/* 299 */                     Integer.valueOf(this.transactionMQProducer.getSendMsgTimeout()), topic, msgId
/*     */                   }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&send_msg_failed"));
/*     */         }
/* 302 */         if (e.getCause() instanceof MQBrokerException) {
/* 303 */           MQBrokerException excep = (MQBrokerException)e.getCause();
/* 304 */           return new ONSClientException(FAQ.errorMessage(
/* 305 */                 String.format("Receive a broker exception, Topi=%s, msgId=%s, %s", new Object[] { topic, msgId, excep.getErrorMessage() }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&broker_response_exception"));
/*     */         }
/*     */       
/*     */       }
/*     */       else {
/*     */         
/* 311 */         MQClientException excep = (MQClientException)e;
/* 312 */         if (-1 == excep.getResponseCode())
/* 313 */           return new ONSClientException(
/* 314 */               FAQ.errorMessage(String.format("Topic does not exist, Topic=%s, msgId=%s", new Object[] { topic, msgId }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&topic_not_exist")); 
/* 315 */         if (13 == excep.getResponseCode()) {
/* 316 */           return new ONSClientException(
/* 317 */               FAQ.errorMessage(String.format("ONS Client check message exception, Topic=%s, msgId=%s", new Object[] { topic, msgId }), "http://docs.aliyun.com/cn#/pub/ons/faq/exceptions&msg_check_failed"));
/*     */         }
/*     */       } 
/*     */     }
/*     */ 
/*     */     
/* 323 */     return new ONSClientException("defaultMQProducer send exception", e);
/*     */   }
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\producer\OnsTransactionProducerImpl.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */