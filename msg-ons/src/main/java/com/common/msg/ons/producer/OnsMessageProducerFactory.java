/*     */  package com.common.msg.ons.producer;
/*     */ 
/*     */ import com.aliyun.openservices.ons.api.Message;
/*     */ import com.aliyun.openservices.ons.api.impl.rocketmq.ONSUtil;
/*     */ import com.aliyun.openservices.ons.api.transaction.LocalTransactionChecker;
/*     */ import com.aliyun.openservices.ons.api.transaction.TransactionStatus;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.LocalTransactionState;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.TransactionCheckListener;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
/*     */ import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.MessageExt;
/*     */ import com.common.msg.ons.consumer.strategy.AbstractOnsConsumeStrategy;
import com.common.msg.api.bootstrap.Destroyable;
/*     */ import com.common.msg.api.consumer.ReceiveRecord;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
/*     */ import com.common.msg.api.exception.MqClientException;
/*     */ import com.common.msg.api.producer.bingding.ProducerConfig;
/*     */ import com.common.msg.api.transaction.TransactionStatus;
/*     */ import com.common.msg.api.util.StringUtil;
/*     */
/*     */ import java.lang.reflect.Method;
/*     */ import java.util.Map;
/*     */ import java.util.Properties;
/*     */ import java.util.concurrent.ConcurrentHashMap;
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
/*     */ 
/*     */ 
/*     */ 
/*     */ public class OnsMessageProducerFactory
/*     */   implements Destroyable
/*     */ {
/*  46 */   private static final Logger log = LoggerFactory.getLogger(OnsMessageProducerFactory.class);
/*     */ 
/*     */   
/*  49 */   private static Map<String, OnsProducer> producerMap = new ConcurrentHashMap<>(3);
/*     */   
/*  51 */   private static Map<String, ConsumerConfig> checkConfig = new ConcurrentHashMap<>();
/*     */   
/*  53 */   private static TransactionCheckerConsumer checkerConsumer = new TransactionCheckerConsumer();
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public OnsProducer getProducer(ProducerConfig config) {
/*  62 */     String configId = config.getTopic();
/*  63 */     if (producerMap.containsKey(configId)) {
/*  64 */       return producerMap.get(configId);
/*     */     }
/*  66 */     return createProducer(config, configId);
/*     */   }
/*     */   
/*     */   private synchronized OnsProducer createProducer(ProducerConfig config, String configId) {
/*     */     try {
/*     */       OnsProducer producer;
/*  72 */       if (producerMap.containsKey(configId)) {
/*  73 */         return producerMap.get(configId);
/*     */       }
/*  75 */       Properties props = config.getProps();
/*  76 */       String onsVersion = props.getProperty("OnsVersion");
/*  77 */       if (!StringUtil.isNullOrEmpty(onsVersion) && onsVersion.equalsIgnoreCase("1.7")) {
/*  78 */         props.setProperty("ONSAddr", config.getSvrUrl());
/*     */       } else {
/*  80 */         props.setProperty("NAMESRV_ADDR", config.getSvrUrl());
/*     */       } 
/*  82 */       if (StringUtil.isNullOrEmpty(props.getProperty("GROUP_ID"))) {
/*  83 */         if (StringUtil.isNullOrEmpty(config.getGroupId())) {
/*  84 */           throw new MqClientException("[groupId] can't be null.");
/*     */         }
/*  86 */         props.put("GROUP_ID", config.getGroupId());
/*     */       } 
/*     */       
/*  89 */       if (null == config.getChecker()) {
/*  90 */         producer = createProducer(props);
/*     */       } else {
/*  92 */         producer = createTransactionProducer(props, convertToOnsChecker(config));
/*     */       } 
/*  94 */       producerMap.put(configId, producer);
/*  95 */       producer.startup();
/*  96 */       log.info("Create ons producer successful. config:" + props);
/*  97 */       return producer;
/*  98 */     } catch (Throwable e) {
/*  99 */       throw new MqClientException("Create ons producer failed. " + config, e);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void destroy() {
/* 106 */     for (Map.Entry<String, OnsProducer> entry : producerMap.entrySet()) {
/* 107 */       ((OnsProducer)entry.getValue()).shutdown();
/*     */     }
/* 109 */     producerMap.clear();
/* 110 */     checkConfig.clear();
/* 111 */     log.info("Destroy ons producer successful.");
/*     */   }
/*     */   
/*     */   private OnsProducer createProducer(Properties properties) {
/* 115 */     return new OnsProducerImpl(ONSUtil.extractProperties(properties));
/*     */   }
/*     */   
/*     */   private OnsProducer createTransactionProducer(Properties properties, final LocalTransactionChecker checker) {
/* 119 */     return new OnsTransactionProducerImpl(ONSUtil.extractProperties(properties), new TransactionCheckListener()
/*     */         {
/*     */           public LocalTransactionState checkLocalTransactionState(MessageExt msg) {
/*     */             try {
/* 123 */               String msgId = msg.getProperty("__transactionId__");
/* 124 */               Message message = ONSUtil.msgConvert((Message)msg);
/* 125 */               message.setMsgID(msgId);
/* 126 */               TransactionStatus check = checker.check(message);
/* 127 */               if (TransactionStatus.CommitTransaction == check)
/* 128 */                 return LocalTransactionState.COMMIT_MESSAGE; 
/* 129 */               if (TransactionStatus.RollbackTransaction == check) {
/* 130 */                 return LocalTransactionState.ROLLBACK_MESSAGE;
/*     */               }
/* 132 */               return LocalTransactionState.UNKNOW;
/* 133 */             } catch (Throwable t) {
/* 134 */               OnsMessageProducerFactory.log.error("Check local transactionState failed." + msg, t);
/* 135 */               throw new MqClientException("Check local transactionState failed.", t);
/*     */             } 
/*     */           }
/*     */         });
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private LocalTransactionChecker convertToOnsChecker(final ProducerConfig config) {
/* 144 */     return new LocalTransactionChecker()
/*     */       {
/*     */         public TransactionStatus check(Message msg) {
/* 147 */           ReceiveRecord record = OnsMessageProducerFactory.checkerConsumer.deserializerMessage(config, msg);
/* 148 */           TransactionStatus status = config.getChecker().check(record);
/* 149 */           return OnsMessageProducerFactory.this.convertToStatus(status);
/*     */         }
/*     */       };
/*     */   }
/*     */   
/*     */   private TransactionStatus convertToStatus(TransactionStatus status) {
/* 155 */     TransactionStatus transactionStatus = TransactionStatus.Unknow;
/*     */     
/* 157 */     if (status.equals(TransactionStatus.Rollback)) {
/* 158 */       transactionStatus = TransactionStatus.RollbackTransaction;
/*     */     }
/* 160 */     if (status.equals(TransactionStatus.Commit)) {
/* 161 */       transactionStatus = TransactionStatus.CommitTransaction;
/*     */     }
/* 163 */     return transactionStatus;
/*     */   }
/*     */   
/*     */   private static class TransactionCheckerConsumer
/*     */     extends AbstractOnsConsumeStrategy
/*     */   {
/*     */     private TransactionCheckerConsumer() {}
/*     */     
/*     */     ReceiveRecord deserializerMessage(ProducerConfig config, Message message) {
/* 172 */       return deserializerMessage(getConfig(config), message, null);
/*     */     }
/*     */     
/*     */     private ConsumerConfig getConfig(ProducerConfig config) {
/* 176 */       if (OnsMessageProducerFactory.checkConfig.containsKey(config.getTopic())) {
/* 177 */         return (ConsumerConfig)OnsMessageProducerFactory.checkConfig.get(config.getTopic());
/*     */       }
/* 179 */       synchronized (TransactionCheckerConsumer.class) {
/* 180 */         if (OnsMessageProducerFactory.checkConfig.containsKey(config.getTopic())) {
/* 181 */           return (ConsumerConfig)OnsMessageProducerFactory.checkConfig.get(config.getTopic());
/*     */         }
/* 183 */         ConsumerConfig consumerConfig = new ConsumerConfig(config.getTopic());
/* 184 */         ConsumerConfigAccessor.setBean(consumerConfig, config.getChecker());
/* 185 */         ConsumerConfigAccessor.setMethod(consumerConfig, getMethod(config.getChecker()));
/* 186 */         consumerConfig.setSerializer(config.getSerializer());
/* 187 */         OnsMessageProducerFactory.checkConfig.put(config.getTopic(), consumerConfig);
/*     */       } 
/*     */ 
/*     */       
/* 191 */       return (ConsumerConfig)OnsMessageProducerFactory.checkConfig.get(config.getTopic());
/*     */     }
/*     */     
/*     */     private Method getMethod(Object bean) {
/* 195 */       Method method = null;
/* 196 */       for (Method m : bean.getClass().getMethods()) {
/* 197 */         if ("check".equalsIgnoreCase(m.getName())) {
/* 198 */           method = m;
/*     */           break;
/*     */         } 
/*     */       } 
/* 202 */       return method;
/*     */     }
/*     */   }
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\producer\OnsMessageProducerFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */