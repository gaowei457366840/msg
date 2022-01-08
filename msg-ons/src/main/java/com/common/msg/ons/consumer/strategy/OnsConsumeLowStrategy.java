/*    */ package com.common.msg.ons.consumer.strategy;
/*    */ 
/*    */ import com.aliyun.openservices.ons.api.Message;
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
/*    */ public class OnsConsumeLowStrategy
/*    */   extends AbstractOnsConsumeStrategy
/*    */   implements OnsConsumeStrategy
/*    */ {
/* 27 */   private static final Logger log = LoggerFactory.getLogger(OnsConsumeLowStrategy.class);
/*    */ 
/*    */ 
/*    */   
/*    */   public OnsAction onMessage(ConsumerConfig config, Message message, OnsConsumeContext context) {
/* 32 */     return tryConsume(config, message, context);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   private OnsAction tryConsume(ConsumerConfig config, Message message, OnsConsumeContext context) {
/*    */     try {
/* 39 */       ReceiveRecord record = deserializerMessage(config, message, context);
/* 40 */       innerOnMessage(config, record);
/* 41 */     } catch (Throwable e) {
/* 42 */       log.warn("Consume message failed." + messageInfo(message), e);
/*    */     } 
/* 44 */     return OnsAction.CommitMessage;
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\strategy\OnsConsumeLowStrategy.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */