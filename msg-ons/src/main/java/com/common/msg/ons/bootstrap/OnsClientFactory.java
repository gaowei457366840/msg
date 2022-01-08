/*     */ package com.common.msg.ons.bootstrap;
/*     */ 
/*     */ import com.common.msg.api.bootstrap.MqFactory;
/*     */ import com.common.msg.api.common.MqTypeEnum;
/*     */ import com.common.msg.api.consumer.MqConsumer;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*     */ import com.common.msg.api.event.EventAdviceService;
/*     */ import com.common.msg.api.exception.MqConfigException;
/*     */ import com.common.msg.api.producer.MqProducer;
/*     */ import com.common.msg.api.producer.bingding.ProducerConfig;
/*     */ import com.common.msg.api.spring.BeanHolder;
/*     */ import com.common.msg.ons.consumer.binding.OnsConsumerBinding;
/*     */ import com.common.msg.ons.event.listener.OnsEventListenerManager;
/*     */ import com.common.msg.ons.producer.OnsMessageProducer;
/*     */ import com.common.msg.ons.producer.OnsMessageProducerFactory;
/*     */ import com.common.msg.ons.producer.binding.OnsProducerBinding;
/*     */ import java.util.concurrent.ConcurrentHashMap;
/*     */ import java.util.concurrent.ConcurrentMap;
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
/*     */ public class OnsClientFactory
/*     */   implements MqFactory
/*     */ {
/*  40 */   private static final Logger log = LoggerFactory.getLogger(OnsClientFactory.class);
/*     */ 
/*     */   
/*  43 */   private static final ConcurrentMap<String, OnsProducerBinding> PRODUCER_MAP = new ConcurrentHashMap<>();
/*     */   
/*  45 */   private static final ConcurrentMap<String, OnsConsumerBinding> CONSUMER_MAP = new ConcurrentHashMap<>();
/*     */   
/*  47 */   private static final Object LOCK_PRODUCER = new Object();
/*     */   
/*  49 */   private static final Object LOCK_CONSUMER = new Object();
/*     */   
/*     */   private OnsMessageProducer producer;
/*     */   
/*     */   public OnsClientFactory() {
/*  54 */     startup();
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public MqProducer buildProducer(ProducerConfig config) {
/*  65 */     if (PRODUCER_MAP.containsKey(config.getTopic())) {
/*  66 */       return (MqProducer)PRODUCER_MAP.get(config.getTopic());
/*     */     }
/*  68 */     synchronized (LOCK_PRODUCER) {
/*  69 */       if (PRODUCER_MAP.containsKey(config.getTopic())) {
/*  70 */         return (MqProducer)PRODUCER_MAP.get(config.getTopic());
/*     */       }
/*     */       
/*  73 */       OnsProducerBinding producer = new OnsProducerBinding(config);
/*  74 */       PRODUCER_MAP.put(config.getTopic(), producer);
/*  75 */       log.info("Binding ons producer on config : " + config);
/*  76 */       return (MqProducer)producer;
/*     */     } 
/*     */   }
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
/*     */   public MqConsumer buildConsumer(ConsumerConfig config) {
/*  90 */     String topicAndTag = config.getTopic() + config.getTag();
/*  91 */     if (CONSUMER_MAP.containsKey(topicAndTag))
/*     */     {
/*  93 */       throw new MqConfigException("Duplicated definition: Consumer(Consumer(type='ons', topic='" + config
/*  94 */           .getTopic() + "', tag='" + config.getTag() + "'.");
/*     */     }
/*  96 */     synchronized (LOCK_CONSUMER) {
/*  97 */       if (CONSUMER_MAP.containsKey(topicAndTag)) {
/*  98 */         return (MqConsumer)CONSUMER_MAP.get(topicAndTag);
/*     */       }
/*     */       
/* 101 */       OnsConsumerBinding consumer = new OnsConsumerBinding(config);
/* 102 */       CONSUMER_MAP.put(topicAndTag, consumer);
/* 103 */       log.info("Binding ons consumer on config : " + config);
/* 104 */       return (MqConsumer)consumer;
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public MqTypeEnum getMqType() {
/* 112 */     return MqTypeEnum.ONS;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void startup() {
/* 118 */     OnsMessageProducerFactory factory = new OnsMessageProducerFactory();
/* 119 */     this.producer = new OnsMessageProducer(factory);
/* 120 */     BeanHolder.addBean(OnsMessageProducerFactory.class.getSimpleName(), factory);
/* 121 */     EventAdviceService eventAdviceService = (EventAdviceService)BeanHolder.getBean(EventAdviceService.class.getSimpleName());
/* 122 */     OnsEventListenerManager manager = new OnsEventListenerManager(this.producer, eventAdviceService);
/*     */   }
/*     */ 
/*     */   
/*     */   public void shutdown() {
/* 127 */     if (null != this.producer);
/*     */   }
/*     */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\bootstrap\OnsClientFactory.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */