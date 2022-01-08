/*     */ package com.common.msg.ons.producer.binding;
/*     */ 
/*     */ import com.common.msg.api.MessageRecord;
/*     */ import com.common.msg.api.bootstrap.MqConfig;
/*     */ import com.common.msg.api.common.SendTypeEnum;
/*     */ import com.common.msg.api.config.ConfigManager;
/*     */ import com.common.msg.api.event.Event;
/*     */ import com.common.msg.api.event.EventBusFactory;
/*     */ import com.common.msg.api.exception.MqClientException;
/*     */ import com.common.msg.api.producer.MqProducer;
/*     */ import com.common.msg.api.producer.SendCallback;
/*     */ import com.common.msg.api.producer.SendResult;
/*     */ import com.common.msg.api.producer.bingding.ProducerConfig;
/*     */ import com.common.msg.api.serialization.Serializer;
/*     */ import com.common.msg.api.spring.BeanHolder;
/*     */ import com.common.msg.api.transaction.TransactionExecuter;
/*     */ import com.common.msg.api.util.ClassUtil;
/*     */ import com.common.msg.ons.event.OnsProduceEvent;
/*     */ import com.common.msg.ons.producer.OnsMessageProducerFactory;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class OnsProducerBinding
/*     */   implements MqProducer
/*     */ {
/*     */   private final ProducerConfig config;
/*     */   private final Serializer serializer;
/*     */   
/*     */   public OnsProducerBinding(ProducerConfig config) {
/*  46 */     this.config = config;
/*     */     
/*  48 */     this.serializer = (Serializer)ClassUtil.newInstance(config.getSerializer().getPath(), Serializer.class);
/*     */     
/*  50 */     this.serializer.setMaxSize(ConfigManager.getInt("mq.message.maxsize", 1048576));
/*  51 */     if (null != config.getChecker())
/*     */     {
/*  53 */       ((OnsMessageProducerFactory)BeanHolder.getBean(OnsMessageProducerFactory.class.getSimpleName()))
/*  54 */         .getProducer(config);
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   public <V> SendResult send(MessageRecord<V> messageRecord) {
/*  60 */     return innerSend(messageRecord, null, null, null, SendTypeEnum.SYCN);
/*     */   }
/*     */ 
/*     */   
/*     */   public <V> void sendAsync(MessageRecord<V> messageRecord, SendCallback callback) {
/*  65 */     innerSend(messageRecord, null, null, callback, SendTypeEnum.ASYCN);
/*     */   }
/*     */ 
/*     */   
/*     */   public <V> void sendOneway(MessageRecord<V> messageRecord) {
/*  70 */     innerSend(messageRecord, null, null, null, SendTypeEnum.ONEWAY);
/*     */   }
/*     */ 
/*     */   
/*     */   public <V> SendResult send(MessageRecord<V> messageRecord, TransactionExecuter executer, Object arg) {
/*  75 */     return innerSend(messageRecord, executer, arg, null, SendTypeEnum.SYCN);
/*     */   }
/*     */   
/*     */   private <V> SendResult innerSend(MessageRecord<V> record, TransactionExecuter executer, Object arg, SendCallback callback, SendTypeEnum sendType) {
/*     */     OnsProduceEvent event;
/*  80 */     if (record == null || record.getMessage() == null) {
/*  81 */       throw new MqClientException("Message can't be null or blank");
/*     */     }
/*     */ 
/*     */     
/*     */     try {
/*  86 */       event = buildEvent(record, executer, arg, callback, sendType);
/*     */       
/*  88 */       EventBusFactory.getInstance().post((Event)event);
/*  89 */     } catch (Throwable t) {
/*  90 */       throw new MqClientException("Send message failed. message:" + record, t);
/*     */     } 
/*  92 */     return event.getResult();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private <V> OnsProduceEvent buildEvent(MessageRecord<V> record, TransactionExecuter executer, Object arg, SendCallback callback, SendTypeEnum sendType) throws Exception {
/*  98 */     OnsProduceEvent<V> event = new OnsProduceEvent();
/*     */     
/* 100 */     event.setMessageRecord(record);
/*     */     
/* 102 */     event.setConfig((MqConfig)this.config);
/*     */     
/* 104 */     event.setKey(record.getKey());
/*     */     
/* 106 */     event.setPayload(this.serializer.serializer(record.getMessage()));
/*     */     
/* 108 */     event.setExecuter(executer);
/*     */     
/* 110 */     event.setArg(arg);
/*     */     
/* 112 */     event.setCallback(callback);
/*     */     
/* 114 */     event.setSendType(sendType);
/* 115 */     return event;
/*     */   }
/*     */   
/*     */   public void startup() {}
/*     */   
/*     */   public void shutdown() {}
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\producer\binding\OnsProducerBinding.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */