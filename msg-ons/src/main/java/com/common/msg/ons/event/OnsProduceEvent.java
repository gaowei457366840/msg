/*    */ package com.common.msg.ons.event;
/*    */ 
/*    */ import com.common.msg.api.MessageRecord;
/*    */ import com.common.msg.api.bootstrap.MqConfig;
/*    */ import com.common.msg.api.event.MqEvent;
/*    */ import com.common.msg.api.producer.SendCallback;
/*    */ import com.common.msg.api.producer.SendResult;
/*    */ import com.common.msg.api.producer.bingding.ProducerConfig;
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
/*    */ public class OnsProduceEvent<V>
/*    */   extends MqEvent
/*    */ {
/*    */   private String key;
/*    */   private transient MessageRecord<V> messageRecord;
/*    */   private transient SendResult result;
/*    */   private transient SendCallback callback;
/*    */   
/*    */   public void setKey(String key) {
/* 31 */     this.key = key; } public void setMessageRecord(MessageRecord<V> messageRecord) { this.messageRecord = messageRecord; } public void setResult(SendResult result) { this.result = result; } public void setCallback(SendCallback callback) { this.callback = callback; } public String toString() {
/* 32 */     return "OnsProduceEvent(super=" + super.toString() + ", key=" + getKey() + ", messageRecord=" + getMessageRecord() + ", result=" + getResult() + ", callback=" + getCallback() + ")";
/*    */   }
/*    */   public String getKey() {
/* 35 */     return this.key;
/*    */   }
/*    */   public MessageRecord<V> getMessageRecord() {
/* 38 */     return this.messageRecord;
/*    */   }
/*    */   public SendResult getResult() {
/* 41 */     return this.result;
/*    */   }
/*    */   public SendCallback getCallback() {
/* 44 */     return this.callback;
/*    */   }
/*    */   public OnsProduceEvent() {
/* 47 */     setGroup(OnsProduceEvent.class);
/*    */   }
/*    */ 
/*    */   
/*    */   public ProducerConfig getConfig() {
/* 52 */     return (ProducerConfig)super.getConfig();
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\event\OnsProduceEvent.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */