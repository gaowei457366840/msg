/*     */ package com.common.msg.ons.annotation.parse;
/*     */ 
/*     */ import com.common.msg.api.annotation.Consumer;
/*     */ import com.common.msg.api.annotation.Producer;
/*     */ import com.common.msg.api.annotation.parse.MqParseStrategy;
/*     */ import com.common.msg.api.bootstrap.MqClient;
/*     */ import com.common.msg.api.common.MessagePriorityEnum;
/*     */ import com.common.msg.api.common.MqTypeEnum;
/*     */ import com.common.msg.api.common.SerializerTypeEnum;
/*     */ import com.common.msg.api.config.ConfigManager;
/*     */ import com.common.msg.api.consumer.AckAction;
/*     */ import com.common.msg.api.consumer.ReceiveRecord;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfig;
/*     */ import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
/*     */ import com.common.msg.api.exception.MqClientException;
/*     */ import com.common.msg.api.exception.MqConfigException;
/*     */ import com.common.msg.api.producer.MqProducer;
/*     */ import com.common.msg.api.producer.bingding.ProducerConfig;
/*     */ import com.common.msg.api.util.StringUtil;
/*     */ import java.lang.reflect.Field;
/*     */ import java.lang.reflect.Method;
/*     */ import java.lang.reflect.Type;
/*     */ import org.slf4j.Logger;
/*     */ import org.slf4j.LoggerFactory;
/*     */ import org.springframework.util.ReflectionUtils;
/*     */ import org.springframework.util.StringUtils;
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
/*     */ public class OnsAnnotationParse
/*     */   implements MqParseStrategy
/*     */ {
/*  46 */   private static final Logger log = LoggerFactory.getLogger(OnsAnnotationParse.class);
/*     */ 
/*     */ 
/*     */   
/*     */   public void parseProducer(Object bean, Field field) {
/*  51 */     Producer annotation = field.<Producer>getAnnotation(Producer.class);
/*     */ 
/*     */     
/*  54 */     String svrUrl = StringUtils.trimWhitespace(annotation.svrUrl());
/*  55 */     svrUrl = StringUtil.getValue(svrUrl);
/*  56 */     if (StringUtil.isNullOrEmpty(svrUrl)) {
/*  57 */       svrUrl = ConfigManager.getString("mq.default.url", "");
/*  58 */       if (StringUtil.isNullOrEmpty(svrUrl)) {
/*  59 */         throw new MqConfigException("@Producer's [svrUrl] is required. bean [" + bean.getClass().getName() + "].");
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/*  64 */     String topic = StringUtils.trimWhitespace(annotation.topic());
/*  65 */     topic = StringUtil.getValue(topic);
/*  66 */     if (StringUtil.isNullOrEmpty(topic)) {
/*  67 */       throw new MqConfigException("@Producer's [topic] is required. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/*  71 */     MessagePriorityEnum priority = annotation.priority();
/*  72 */     if (priority.equals(MessagePriorityEnum.UNKNOWN)) {
/*  73 */       throw new MqConfigException("@Producer's [priority] is invalid. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/*  77 */     SerializerTypeEnum serializer = annotation.serializer();
/*  78 */     if (serializer.equals(SerializerTypeEnum.UNKNOWN)) {
/*  79 */       throw new MqConfigException("@Producer's [serializer] is invalid. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/*  83 */     ProducerConfig config = new ProducerConfig(svrUrl, topic, annotation.priority());
/*  84 */     config.setSerializer(serializer);
/*     */     try {
/*  86 */       MqProducer mqProducer = MqClient.buildProducer(config);
/*  87 */       ReflectionUtils.makeAccessible(field);
/*  88 */       ReflectionUtils.setField(field, bean, mqProducer);
/*  89 */     } catch (Throwable t) {
/*  90 */       throw new MqConfigException("Create producer failed. bean [" + bean.getClass().getName() + "] config:" + config, t);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public void parseConsumer(Object bean, Method method) {
/*  96 */     Consumer annotation = method.<Consumer>getAnnotation(Consumer.class);
/*     */ 
/*     */     
/*  99 */     String svrUrl = StringUtils.trimWhitespace(annotation.svrUrl());
/* 100 */     svrUrl = StringUtil.getValue(svrUrl);
/* 101 */     if (StringUtil.isNullOrEmpty(svrUrl)) {
/* 102 */       svrUrl = ConfigManager.getString("mq.default.url", "");
/* 103 */       if (StringUtil.isNullOrEmpty(svrUrl)) {
/* 104 */         throw new MqConfigException("@Consumer's [svrUrl] is required [" + method + "]. bean [" + bean.getClass().getName() + "].");
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 109 */     String topic = StringUtils.trimWhitespace(annotation.topic());
/* 110 */     topic = StringUtil.getValue(topic);
/* 111 */     if (StringUtil.isNullOrEmpty(topic)) {
/* 112 */       throw new MqConfigException("@Consumer's [topic] is required [" + method + "]. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/* 116 */     String groupId = StringUtils.trimWhitespace(annotation.groupId());
/*     */ 
/*     */     
/* 119 */     String tag = StringUtils.trimWhitespace(annotation.tag());
/*     */     
/* 121 */     Type[] types = method.getGenericParameterTypes();
/* 122 */     if (types.length != 1 && !ReceiveRecord.class.isAssignableFrom(types[0].getClass())) {
/* 123 */       throw new MqClientException("@Consumer's method [" + method + "] should only have 1 parameter and which type supposed to be ReceiveRecord<?>. bean [" + bean
/* 124 */           .getClass().getName() + "].");
/*     */     }
/* 126 */     if (!method.getParameterTypes()[0].equals(ReceiveRecord.class)) {
/* 127 */       throw new MqConfigException("@Consumer's method [" + method + "] should only to be ReceiveRecord<?>. bean [" + bean
/* 128 */           .getClass().getName() + "].");
/*     */     }
/*     */     
/* 131 */     if (!method.getReturnType().equals(AckAction.class)) {
/* 132 */       throw new MqConfigException("@Consumer's method [" + method + "] should only return AckAction. bean [" + bean
/* 133 */           .getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/* 137 */     int numThreads = annotation.numThreads();
/* 138 */     if (numThreads <= 0) {
/* 139 */       throw new MqConfigException("@Consumer's [numThreads] can not <= 0. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/* 143 */     int batchSize = annotation.batchSize();
/* 144 */     if (batchSize <= 0) {
/* 145 */       throw new MqConfigException("@Consumer's [batchSize] can not <= 0. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/* 149 */     int retries = annotation.retries();
/* 150 */     if (retries <= 0) {
/* 151 */       throw new MqConfigException("@Consumer's [retries] can not <= 0. bean [" + bean.getClass().getName() + "].");
/*     */     }
/*     */ 
/*     */     
/* 155 */     String isRedeliver = annotation.isRedeliver();
/*     */ 
/*     */     
/* 158 */     ConsumerConfig config = new ConsumerConfig(topic);
/* 159 */     config.setSvrUrl(svrUrl);
/* 160 */     config.setTag(tag);
/* 161 */     config.setGroupId(groupId);
/* 162 */     config.setPriority(annotation.priority());
/* 163 */     ConsumerConfigAccessor.setBean(config, bean);
/* 164 */     ConsumerConfigAccessor.setBean(config, method);
/* 165 */     ConsumerConfigAccessor.setBean(config, isRedeliver);
/* 166 */     config.setNumThreads(numThreads);
/* 167 */     config.setBatchSize(batchSize);
/* 168 */     config.setRetries(retries);
/*     */     try {
/* 170 */       MqClient.buildConsumer(config);
/* 171 */     } catch (Throwable t) {
/* 172 */       throw new MqConfigException("Create consumer failed. bean [" + bean.getClass().getName() + "] config:" + config, t);
/*     */     } 
/*     */   }
/*     */ 
/*     */   
/*     */   public String getMqType() {
/* 178 */     return MqTypeEnum.ONS.getMemo();
/*     */   }
/*     */ }


