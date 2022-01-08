/*    */ package com.common.msg.ons.consumer.strategy;
/*    */ 
/*    */ import com.aliyun.openservices.ons.api.Message;
/*    */ import com.common.msg.api.consumer.AckAction;
/*    */ import com.common.msg.api.consumer.ReceiveRecord;
/*    */ import com.common.msg.api.consumer.binding.ConsumerConfig;
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
/*    */ public class OnsConsumeHighStrategy
/*    */   extends AbstractOnsConsumeStrategy
/*    */   implements OnsConsumeStrategy
/*    */ {
/* 28 */   private static final Logger log = LoggerFactory.getLogger(OnsConsumeHighStrategy.class);
/*    */ 
/*    */ 
/*    */   
/*    */   public OnsAction onMessage(ConsumerConfig config, Message message, OnsConsumeContext context) {
/* 33 */     return tryConsume(config, message, context);
/*    */   }
/*    */ 
/*    */   
/*    */   private OnsAction tryConsume(ConsumerConfig config, Message message, OnsConsumeContext context) {
/*    */     OnsAction action;
/* 39 */     int reconsumeTimes = message.getReconsumeTimes();
/*    */     
/*    */     try {
/* 42 */       ReceiveRecord record = deserializerMessage(config, message, context);
/* 43 */       AckAction ackAction = innerOnMessage(config, record);
/* 44 */       action = convert2OnsAction(config, ackAction);
/* 45 */       if (ackAction.equals(AckAction.Commit)) {
/* 46 */         if (AUDIT_SWITCH) {
/* 47 */           log.info("Consume" + messageInfo(message));
/*    */         }
/* 49 */       } else if (reconsumeTimes == 0) {
/* 50 */         log.warn("Consume message failed, waiting for retry." + messageInfo(message));
/* 51 */       } else if (reconsumeTimes == config.getRetries()) {
/*    */         
/* 53 */         action = OnsAction.CommitMessage;
/* 54 */         log.error("Reconsume message failed " + reconsumeTimes + "/" + config.getRetries() + " times, Discard it:" + message);
/*    */       } else {
/*    */         
/* 57 */         log.warn("Reconsume message failed " + reconsumeTimes + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message));
/*    */       } 
/* 59 */     } catch (Throwable e) {
/* 60 */       if (reconsumeTimes == 0) {
/* 61 */         action = distinguishAction(config);
/* 62 */         log.warn("Consume message failed, waiting for retry." + messageInfo(message), e);
/* 63 */       } else if (reconsumeTimes == config.getRetries()) {
/*    */         
/* 65 */         action = OnsAction.CommitMessage;
/* 66 */         log.error("Reconsume message failed " + reconsumeTimes + "/" + config.getRetries() + " times, Discard it:" + message, e);
/*    */       } else {
/*    */         
/* 69 */         action = distinguishAction(config);
/* 70 */         log.warn("Reconsume message failed " + reconsumeTimes + "/" + config.getRetries() + " times, waiting for retry." + messageInfo(message), e);
/*    */       } 
/*    */     } 
/* 73 */     return action;
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\strategy\OnsConsumeHighStrategy.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */