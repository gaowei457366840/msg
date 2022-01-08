/*    */ package com.common.msg.ons.consumer.strategy;
/*    */ 
/*    */ import com.common.msg.api.common.MessagePriorityEnum;
/*    */ import java.util.concurrent.ConcurrentHashMap;
/*    */ import java.util.concurrent.ConcurrentMap;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OnsConsumerStrategyFactory
/*    */ {
/*    */   private static volatile OnsConsumerStrategyFactory instance;
/* 31 */   private final ConcurrentMap<Integer, OnsConsumeStrategy> strategyMap = new ConcurrentHashMap<>(3);
/*    */ 
/*    */   
/*    */   public static OnsConsumerStrategyFactory getInstance() {
/* 35 */     if (null == instance) {
/* 36 */       synchronized (OnsConsumerStrategyFactory.class) {
/* 37 */         if (null == instance) {
/* 38 */           instance = new OnsConsumerStrategyFactory();
/*    */         }
/*    */       } 
/*    */     }
/* 42 */     return instance;
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   public OnsConsumeStrategy getStrategy(MessagePriorityEnum priorityEnum) {
/* 52 */     int priority = priorityEnum.getCode();
/* 53 */     if (this.strategyMap.containsKey(Integer.valueOf(priority))) {
/* 54 */       return this.strategyMap.get(Integer.valueOf(priority));
/*    */     }
/* 56 */     return createStrategy(priority);
/*    */   }
/*    */   
/*    */   private synchronized OnsConsumeStrategy createStrategy(int priority) {
/* 60 */     if (this.strategyMap.containsKey(Integer.valueOf(priority))) {
/* 61 */       return this.strategyMap.get(Integer.valueOf(priority));
/*    */     }
/*    */ 
/*    */     
/* 65 */     switch (priority)
/*    */     
/*    */     { case 1:
/* 68 */         strategy = new OnsConsumeHighStrategy();
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
/* 81 */         this.strategyMap.put(Integer.valueOf(priority), strategy);
/* 82 */         return strategy;case 2: strategy = new OnsConsumeMediumStrategy(); this.strategyMap.put(Integer.valueOf(priority), strategy); return strategy;case 3: strategy = new OnsConsumeLowStrategy(); this.strategyMap.put(Integer.valueOf(priority), strategy); return strategy; }  OnsConsumeStrategy strategy = new OnsConsumeMediumStrategy(); this.strategyMap.put(Integer.valueOf(priority), strategy); return strategy;
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\strategy\OnsConsumerStrategyFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */