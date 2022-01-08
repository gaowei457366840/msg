/*    */ package com.common.msg.ons.event.listener;
/*    */ 
/*    */ import com.common.msg.ons.producer.OnsMessageProducer;
import com.common.msg.api.event.EventAdviceService;
/*    */ import com.common.msg.api.event.EventBusFactory;
/*    */ import com.common.msg.ons.event.OnsProduceEvent;
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
/*    */ public class OnsEventListenerManager
/*    */ {
/*    */   private final OnsProduceEventListener onsProduceEventListener;
/*    */   
/*    */   public OnsEventListenerManager(OnsMessageProducer producer, EventAdviceService service) {
/* 35 */     this.onsProduceEventListener = new OnsProduceEventListener(producer, service);
/*    */     
/* 37 */     init();
/*    */   }
/*    */ 
/*    */   
/*    */   private void init() {
/* 42 */     EventBusFactory.getInstance().register(OnsProduceEvent.class, this.onsProduceEventListener);
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\event\listener\OnsEventListenerManager.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */