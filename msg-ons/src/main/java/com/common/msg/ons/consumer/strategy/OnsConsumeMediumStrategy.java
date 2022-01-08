/*    */ package com.common.msg.ons.consumer.strategy;
/*    */ 
/*    */ import com.aliyun.openservices.ons.api.Message;
/*    */ import com.common.msg.api.consumer.AckAction;
/*    */ import com.common.msg.api.consumer.ReceiveRecord;
/*    */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*    */ import com.common.msg.api.util.ThreadUtil;
/*    */ import com.common.msg.ons.consumer.OnsAction;
/*    */ import org.slf4j.Logger;
/*    */ import org.slf4j.LoggerFactory;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OnsConsumeMediumStrategy
/*    */   extends AbstractOnsConsumeStrategy
/*    */   implements OnsConsumeStrategy
/*    */ {
/* 29 */   private static final Logger log = LoggerFactory.getLogger(OnsConsumeMediumStrategy.class);
/*    */ 
/*    */ 
/*    */   
/*    */   public OnsAction onMessage(ConsumerConfig config, Message message, OnsConsumeContext context) {
/* 34 */     return tryConsume(config, message, context);
/*    */   }
/*    */ 
/*    */   
/*    */   private OnsAction tryConsume(ConsumerConfig config, Message message, OnsConsumeContext context) {
/* 39 */     ReceiveRecord record = null;
/*    */     
/*    */     try {
/* 42 */       record = deserializerMessage(config, message, context);
/* 43 */     } catch (Throwable e) {
/* 44 */       log.error("Deserializer message failed, Discard it" + messageInfo(message), e);
/*    */     } 
/*    */ 
/*    */     
/* 48 */     int i = 0;
/* 49 */     while (i <= config.getRetries() && !Thread.currentThread().isInterrupted()) {
/*    */       try {
/* 51 */         if (innerOnMessage(config, record).equals(AckAction.Commit)) {
/* 52 */           if (AUDIT_SWITCH) {
/* 53 */             log.info("Consume" + messageInfo(message));
/*    */           }
/*    */           break;
/*    */         } 
/* 57 */         if (i == 0) {
/* 58 */           log.warn("Consume message failed, waiting for retry." + messageInfo(message));
/* 59 */           ThreadUtil.sleep(1000L, log);
/* 60 */         } else if (i == config.getRetries()) {
/*    */           
/* 62 */           log.error("Reconsume message failed " + i + "/" + config.getRetries() + " times, Discard it:" + message);
/*    */         } else {
/*    */           
/* 65 */           log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message));
/* 66 */           ThreadUtil.sleep((1000 * (i + 1)), log);
/*    */         }
/*    */       
/* 69 */       } catch (Throwable e) {
/* 70 */         if (i == 0) {
/* 71 */           log.warn("Consume message failed, waiting for retry." + messageInfo(message), e);
/* 72 */           ThreadUtil.sleep(1000L, log);
/* 73 */         } else if (i == config.getRetries()) {
/*    */           
/* 75 */           log.error("Reconsume message failed " + i + "/" + config.getRetries() + " times, Discard it:" + message, e);
/*    */         } else {
/*    */           
/* 78 */           log.warn("Reconsume message failed " + i + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message), e);
/* 79 */           ThreadUtil.sleep((1000 * (i + 1)), log);
/*    */         } 
/*    */       } 
/* 82 */       i++;
/*    */     } 
/* 84 */     return OnsAction.CommitMessage;
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\strategy\OnsConsumeMediumStrategy.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */