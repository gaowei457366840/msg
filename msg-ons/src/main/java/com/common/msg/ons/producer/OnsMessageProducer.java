/*     */ package com.common.msg.ons.producer;
/*     */ 
/*     */ import com.aliyun.openservices.ons.api.Message;
/*     */ import com.aliyun.openservices.ons.api.MessageAccessor;
/*     */ import com.aliyun.openservices.ons.api.OnExceptionContext;
/*     */ import com.aliyun.openservices.ons.api.SendCallback;
/*     */ import com.aliyun.openservices.ons.api.SendResult;
/*     */ import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
/*     */ import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
/*     */ import com.common.msg.api.MessageAccessor;
/*     */ import com.common.msg.api.bootstrap.Destroyable;
/*     */ import com.common.msg.api.common.SendTypeEnum;
/*     */ import com.common.msg.api.exception.MqClientException;
/*     */ import com.common.msg.api.producer.OnExceptionContext;
/*     */ import com.common.msg.api.producer.SendResult;
/*     */ import com.common.msg.api.producer.bingding.ProducerConfig;
/*     */ import com.common.msg.api.transaction.TransactionStatus;
/*     */ import com.common.msg.api.util.StringUtil;
/*     */ import com.common.msg.ons.event.OnsProduceEvent;
/*     */ import java.util.Map;
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
/*     */ public class OnsMessageProducer
/*     */   implements Destroyable
/*     */ {
/*  41 */   private static final Logger log = LoggerFactory.getLogger(OnsMessageProducer.class); public OnsMessageProducer(OnsMessageProducerFactory factory) {
/*  42 */     this.factory = factory;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private final OnsMessageProducerFactory factory;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void send(OnsProduceEvent event) {
/*  57 */     ProducerConfig config = event.getConfig(); try {
/*     */       Message message;
/*  59 */       if (StringUtil.isNullOrEmpty(event.getKey())) {
/*     */         
/*  61 */         message = new Message(config.getTopic(), event.getMessageRecord().getTag(), event.getPayload());
/*     */       } else {
/*  63 */         message = new Message(config.getTopic(), event.getMessageRecord().getTag(), event.getKey(), event.getPayload());
/*     */       } 
/*     */       
/*  66 */       Properties userProperties = event.getMessageRecord().getUserProperties();
/*  67 */       Properties systemProperties = MessageAccessor.getSystemProperties(event.getMessageRecord());
/*  68 */       if (null != userProperties && userProperties.size() > 0) {
/*  69 */         message.setUserProperties(userProperties);
/*     */       }
/*  71 */       if (null != systemProperties && systemProperties.size() > 0) {
/*  72 */         String deliverTimeValue = systemProperties.getProperty("__DELIVERTIME");
/*  73 */         if (!StringUtil.isNullOrEmpty(deliverTimeValue)) {
/*  74 */           systemProperties.setProperty("__STARTDELIVERTIME", deliverTimeValue);
/*  75 */           systemProperties.remove("__DELIVERTIME");
/*     */         } 
/*     */         
/*  78 */         for (Map.Entry<Object, Object> entry : systemProperties.entrySet()) {
/*  79 */           MessageAccessor.putSystemProperties(message, (String)entry.getKey(), (String)entry.getValue());
/*     */         }
/*     */       } 
/*     */ 
/*     */       
/*  84 */       if (SendTypeEnum.SYCN.equals(event.getSendType())) {
/*     */         
/*  86 */         SendResult result = synSend(event, message);
/*  87 */         event.setResult(result);
/*     */       } else {
/*     */         
/*  90 */         asynSend(event, message);
/*     */       } 
/*  92 */     } catch (Throwable e) {
/*  93 */       throw new MqClientException("Ons send message failed: " + event, e);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void destroy() {
/*  99 */     this.factory.destroy();
/*     */   }
/*     */   
/*     */   private SendResult synSend(final OnsProduceEvent event, Message message) throws Throwable {
/*     */     SendResult sendResult;
/* 104 */     if (null != event.getExecuter()) {
/*     */       
/* 106 */       sendResult = this.factory.getProducer(event.getConfig()).send(message, new LocalTransactionExecuter()
/*     */           {
/*     */             public TransactionStatus execute(Message msg, Object arg) {
/* 109 */               TransactionStatus status = event.getExecuter().execute(event.getMessageRecord(), arg);
/* 110 */               return OnsMessageProducer.this.convertToStatus(status);
/*     */             }
/* 112 */           }event.getArg());
/*     */     } else {
/*     */       
/* 115 */       sendResult = this.factory.getProducer(event.getConfig()).send(message);
/*     */     } 
/* 117 */     log.debug("Ons synchronous send to topic:" + event.getConfig().getTopic() + ", msgId:" + sendResult
/* 118 */         .getMessageId() + ", message:" + event.getMessageRecord());
/* 119 */     return convertToResult(sendResult);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void asynSend(final OnsProduceEvent event, Message message) {
/* 125 */     if (event.getCallback() != null) {
/*     */       
/* 127 */       this.factory.getProducer(event.getConfig()).sendAsync(message, new SendCallback()
/*     */           {
/*     */             public void onSuccess(SendResult sendResult) {
/* 130 */               event.getCallback().onSuccess(OnsMessageProducer.this.convertToResult(sendResult));
/*     */             }
/*     */ 
/*     */             
/*     */             public void onException(OnExceptionContext onExceptionContext) {
/* 135 */               event.getCallback().onException(new OnExceptionContext(onExceptionContext
/*     */                     
/* 137 */                     .getTopic(), event
/* 138 */                     .getMessageRecord(), new MqClientException((Throwable)onExceptionContext
/* 139 */                       .getException())));
/*     */             }
/*     */           });
/*     */     } else {
/*     */       
/* 144 */       this.factory.getProducer(event.getConfig()).sendOneway(message);
/*     */     } 
/* 146 */     log.debug("Ons asynchronous send to topic:" + event.getConfig().getTopic() + ", message:" + event.getMessageRecord());
/*     */   }
/*     */   
/*     */   private SendResult convertToResult(SendResult sendResult) {
/* 150 */     if (sendResult == null) return null; 
/* 151 */     SendResult result = new SendResult();
/* 152 */     result.setTopic(sendResult.getTopic());
/* 153 */     result.setMsgId(sendResult.getMessageId());
/* 154 */     return result;
/*     */   }
/*     */   
/*     */   private TransactionStatus convertToStatus(TransactionStatus status) {
/* 158 */     TransactionStatus transactionStatus = TransactionStatus.Unknow;
/*     */     
/* 160 */     if (status.equals(TransactionStatus.Rollback)) {
/* 161 */       transactionStatus = TransactionStatus.RollbackTransaction;
/*     */     }
/* 163 */     if (status.equals(TransactionStatus.Commit)) {
/* 164 */       transactionStatus = TransactionStatus.CommitTransaction;
/*     */     }
/* 166 */     return transactionStatus;
/*     */   }
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\producer\OnsMessageProducer.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */