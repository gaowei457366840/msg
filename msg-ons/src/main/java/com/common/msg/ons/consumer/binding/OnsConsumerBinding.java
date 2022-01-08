/*     */ package com.common.msg.ons.consumer.binding;
/*     */ 
/*     */ import com.aliyun.openservices.ons.api.Action;
/*     */ import com.aliyun.openservices.ons.api.ConsumeContext;
/*     */ import com.aliyun.openservices.ons.api.Consumer;
/*     */ import com.aliyun.openservices.ons.api.Message;
/*     */ import com.aliyun.openservices.ons.api.MessageListener;
/*     */ import com.aliyun.openservices.ons.api.exception.ONSClientException;
/*     */ import com.aliyun.openservices.ons.api.order.ConsumeOrderContext;
/*     */ import com.aliyun.openservices.ons.api.order.MessageOrderListener;
/*     */ import com.aliyun.openservices.ons.api.order.OrderAction;
/*     */ import com.aliyun.openservices.ons.api.order.OrderConsumer;
/*     */ import com.common.msg.ons.consumer.strategy.OnsConsumeContext;
          import com.common.msg.ons.consumer.strategy.OnsConsumerStrategyFactory;
          import com.common.msg.api.consumer.MqConsumer;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*     */ import com.common.msg.ons.consumer.OnsAction;
/*     */ import com.common.msg.ons.consumer.OnsMessageConsumerFactory;
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
/*     */ public class OnsConsumerBinding
/*     */   implements MqConsumer
/*     */ {
/*     */   private final ConsumerConfig config;
/*     */   private final OrderConsumer orderConsumer;
/*     */   private final Consumer consumer;
/*     */   
/*     */   public OnsConsumerBinding(ConsumerConfig config) {
/*  44 */     this.config = config;
/*  45 */     if (config.getNumThreads() > 1) {
/*  46 */       this.consumer = OnsMessageConsumerFactory.getConsumer(config);
/*  47 */       this.orderConsumer = null;
/*  48 */       this.consumer.subscribe(config.getTopic(), config.getTag(), new OnsMessageConcurrentListener());
/*  49 */       this.consumer.start();
/*     */     } else {
/*  51 */       this.consumer = null;
/*  52 */       this.orderConsumer = OnsMessageConsumerFactory.getOrderConsumer(config);
/*  53 */       this.orderConsumer.subscribe(config.getTopic(), config.getTag(), new OnsMessageOrderListener());
/*  54 */       this.orderConsumer.start();
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void startup() {}
/*     */ 
/*     */ 
/*     */   
/*     */   public void shutdown() {
/*  65 */     if (null != this.consumer) this.consumer.shutdown(); 
/*  66 */     if (null != this.orderConsumer) this.orderConsumer.shutdown();
/*     */   
/*     */   }
/*     */   
/*     */   public void unSubscribe() {
/*  71 */     if (this.config.getNumThreads() > 1) {
/*  72 */       this.consumer.unsubscribe(this.config.getTopic());
/*     */     } else {
/*  74 */       throw new ONSClientException("Order consumer not support unSubscribe topic.");
/*     */     } 
/*     */   }
/*     */   
/*     */   private class OnsMessageOrderListener
/*     */     implements MessageOrderListener {
/*     */     private OnsMessageOrderListener() {}
/*     */     
/*     */     public OrderAction consume(Message message, ConsumeOrderContext context) {
/*  83 */       OnsAction action = OnsConsumerStrategyFactory.getInstance().getStrategy(OnsConsumerBinding.this.config.getPriority()).onMessage(OnsConsumerBinding.this.config, message, OnsConsumerBinding.this.convert2OnsContext(context));
/*  84 */       if (OnsAction.CommitMessage.equals(action)) {
/*  85 */         return OrderAction.Success;
/*     */       }
/*  87 */       return OrderAction.Suspend;
/*     */     }
/*     */   }
/*     */   
/*     */   private class OnsMessageConcurrentListener
/*     */     implements MessageListener
/*     */   {
/*     */     private OnsMessageConcurrentListener() {}
/*     */     
/*     */     public Action consume(Message message, ConsumeContext context) {
/*  97 */       OnsAction action = OnsConsumerStrategyFactory.getInstance().getStrategy(OnsConsumerBinding.this.config.getPriority()).onMessage(OnsConsumerBinding.this.config, message, OnsConsumerBinding.this.convert2OnsContext(context));
/*  98 */       if (OnsAction.CommitMessage.equals(action)) {
/*  99 */         return Action.CommitMessage;
/*     */       }
/* 101 */       return Action.ReconsumeLater;
/*     */     }
/*     */   }
/*     */ 
/*     */   
/*     */   private OnsConsumeContext convert2OnsContext(ConsumeOrderContext context) {
/* 107 */     return null;
/*     */   }
/*     */   
/*     */   private OnsConsumeContext convert2OnsContext(ConsumeContext context) {
/* 111 */     return null;
/*     */   }
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\binding\OnsConsumerBinding.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */