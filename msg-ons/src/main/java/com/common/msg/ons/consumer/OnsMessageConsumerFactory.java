/*    */ package com.common.msg.ons.consumer;
/*    */ 
/*    */ import com.aliyun.openservices.ons.api.Consumer;
/*    */ import com.aliyun.openservices.ons.api.ONSFactory;
/*    */ import com.aliyun.openservices.ons.api.order.OrderConsumer;
/*    */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*    */ import com.common.msg.api.exception.MqClientException;
/*    */ import com.common.msg.api.util.StringUtil;
/*    */ import java.util.Properties;
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
/*    */ 
/*    */ 
/*    */ 
/*    */ public class OnsMessageConsumerFactory
/*    */ {
/*    */   public static OrderConsumer getOrderConsumer(ConsumerConfig config) {
/*    */     try {
/* 39 */       return ONSFactory.createOrderedConsumer(adapterConfig(config));
/* 40 */     } catch (Throwable e) {
/* 41 */       throw new MqClientException("Create orderly consumer failed." + config, e);
/*    */     } 
/*    */   }
/*    */   
/*    */   public static Consumer getConsumer(ConsumerConfig config) {
/*    */     try {
/* 47 */       return ONSFactory.createConsumer(adapterConfig(config));
/* 48 */     } catch (Throwable e) {
/* 49 */       throw new MqClientException("Create concurrently consumer failed." + config, e);
/*    */     } 
/*    */   }
/*    */   
/*    */   private static Properties adapterConfig(ConsumerConfig config) {
/* 54 */     Properties props = config.getProps();
/* 55 */     if (null == props) {
/* 56 */       props = new Properties();
/*    */     }
/* 58 */     String onsVersion = props.getProperty("OnsVersion");
/* 59 */     if (!StringUtil.isNullOrEmpty(onsVersion) && onsVersion.equalsIgnoreCase("1.7")) {
/* 60 */       props.put("ONSAddr", config.getSvrUrl());
/*    */     } else {
/* 62 */       props.put("NAMESRV_ADDR", config.getSvrUrl());
/*    */     } 
/* 64 */     props.put("ConsumeThreadNums", Integer.valueOf(config.getNumThreads()));
/* 65 */     if (StringUtil.isNullOrEmpty(props.getProperty("GROUP_ID"))) {
/* 66 */       if (StringUtil.isNullOrEmpty(config.getGroupId())) {
/* 67 */         throw new MqClientException("[groupId] can't be null.");
/*    */       }
/* 69 */       props.put("GROUP_ID", config.getGroupId());
/*    */     } 
/* 71 */     if (StringUtil.isNullOrEmpty(props.getProperty("MessageModel"))) {
/* 72 */       props.put("MessageModel", config.getPattern().getMemo());
/*    */     }
/*    */     
/* 75 */     return props;
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\OnsMessageConsumerFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */