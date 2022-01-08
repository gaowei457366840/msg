/*     */ package com.common.msg.ons.consumer.strategy;
/*     */ 
/*     */ import com.aliyun.openservices.ons.api.Message;
/*     */ import com.aliyun.openservices.ons.api.MessageAccessor;
/*     */ import com.common.msg.api.MessageAccessor;
/*     */ import com.common.msg.api.MessageRecord;
/*     */ import com.common.msg.api.bootstrap.MqConfig;
/*     */ import com.common.msg.api.config.ConfigManager;
/*     */ import com.common.msg.api.consumer.AckAction;
/*     */ import com.common.msg.api.consumer.ReceiveRecord;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*     */ import com.common.msg.api.consumer.strategy.AbstractConsumeStrategy;
/*     */ import com.common.msg.api.exception.MqClientException;
/*     */ import com.common.msg.api.serialization.Serializer;
/*     */ import com.common.msg.api.util.ClassUtil;
/*     */ import com.common.msg.api.util.StringUtil;
/*     */ import com.common.msg.ons.consumer.OnsAction;
/*     */ import java.util.Properties;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AbstractOnsConsumeStrategy
/*     */   extends AbstractConsumeStrategy
/*     */ {
/*  38 */   private static final Logger log = LoggerFactory.getLogger(AbstractOnsConsumeStrategy.class);
/*     */ 
/*     */   
/*  41 */   private static final String S_AUDITS_WITCH = ConfigManager.getString("kafka.message.audit.switch", "on");
/*     */   
/*  43 */   static final boolean AUDIT_SWITCH = ("on".equalsIgnoreCase(S_AUDITS_WITCH) || "true".equalsIgnoreCase(S_AUDITS_WITCH));
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   protected ReceiveRecord deserializerMessage(ConsumerConfig config, Message message, OnsConsumeContext context) {
/*     */     Object data;
/*     */     try {
/*  57 */       data = deserializerMessage(config, message.getBody());
/*  58 */     } catch (Throwable t) {
/*  59 */       throw new MqClientException("Deserializer message failed, topic:" + message.getTopic() + ", tag:" + message
/*  60 */           .getTag() + ", msgId:" + message
/*  61 */           .getMsgID() + ", config:" + config, t);
/*     */     } 
/*     */ 
/*     */     
/*  65 */     if (data == null) {
/*  66 */       throw new MqClientException("Deserializer message failed, topic:" + message.getTopic() + ", tag:" + message
/*  67 */           .getTag() + ", msgId:" + message
/*  68 */           .getMsgID() + ", config:" + config);
/*     */     }
/*     */     
/*  71 */     ReceiveRecord record = new ReceiveRecord();
/*  72 */     record.setTopic(message.getTopic());
/*  73 */     record.setKey(message.getKey());
/*  74 */     record.setMessage(data);
/*  75 */     record.setMsgId(message.getMsgID());
/*  76 */     record.setUserProperties(message.getUserProperties());
/*     */     
/*  78 */     Properties sysProperties = MessageAccessor.getSystemProperties(message);
/*     */     
/*  80 */     String deliverTimeValue = sysProperties.getProperty("__STARTDELIVERTIME");
/*  81 */     if (!StringUtil.isNullOrEmpty(deliverTimeValue)) {
/*  82 */       sysProperties.setProperty("__DELIVERTIME", deliverTimeValue);
/*  83 */       sysProperties.remove("__STARTDELIVERTIME");
/*     */     } 
/*  85 */     MessageAccessor.setSystemProperties((MessageRecord)record, MessageAccessor.getSystemProperties(message));
/*     */     
/*  87 */     return record;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   AckAction innerOnMessage(ConsumerConfig config, ReceiveRecord message) {
/*     */     AckAction action;
/*  99 */     if (!isRedeliver(config, message)) {
/*     */       
/* 101 */       action = onMessage(config, message);
/*     */     } else {
/* 103 */       action = AckAction.Commit;
/* 104 */       log.info("Discard redelivered" + messageInfo(message));
/*     */     } 
/* 106 */     return action;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private Object deserializerMessage(ConsumerConfig config, byte[] value) {
/* 117 */     Serializer serializer = (Serializer)ClassUtil.newInstance(config.getSerializer().getPath(), Serializer.class);
/* 118 */     return serializer.deserializer((MqConfig)config, value);
/*     */   }
/*     */   
/*     */   String messageInfo(Message message) {
/* 122 */     StringBuilder builder = new StringBuilder();
/* 123 */     builder.append(" record:topic[").append(message.getTopic()).append("]");
/* 124 */     builder.append(" msgId[").append(message.getMsgID()).append("]");
/* 125 */     builder.append(" key[").append(StringUtil.isNullOrEmpty(message.getKey()) ? "" : message.getKey()).append("]");
/* 126 */     return builder.toString();
/*     */   }
/*     */   
/*     */   OnsAction convert2OnsAction(ConsumerConfig config, AckAction ackAction) {
/* 130 */     OnsAction action = OnsAction.CommitMessage;
/* 131 */     if (AckAction.Commit.equals(ackAction)) {
/* 132 */       action = OnsAction.CommitMessage;
/* 133 */     } else if (AckAction.Reconsume.equals(ackAction)) {
/* 134 */       action = distinguishAction(config);
/*     */     } 
/* 136 */     return action;
/*     */   }
/*     */   
/*     */   OnsAction distinguishAction(ConsumerConfig config) {
/* 140 */     if (config.getNumThreads() > 1) {
/* 141 */       return OnsAction.Suspend;
/*     */     }
/* 143 */     return OnsAction.ReconsumeLater;
/*     */   }
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\strategy\AbstractOnsConsumeStrategy.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */