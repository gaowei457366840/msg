/*    */ package com.common.msg.ons.event.listener;
/*    */ 
/*    */ import com.common.msg.ons.event.OnsProduceEvent;
import com.common.msg.ons.producer.OnsMessageProducer;
import com.common.msg.api.event.Event;
/*    */ import com.common.msg.api.event.EventAdviceService;
/*    */ import com.common.msg.api.event.EventListener;
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
/*    */ public class OnsProduceEventListener
/*    */   implements EventListener<OnsProduceEvent>
/*    */ {
/*    */   private final OnsMessageProducer producer;
/*    */   private final EventAdviceService service;
/*    */   
/*    */   OnsProduceEventListener(OnsMessageProducer producer, EventAdviceService service) {
/* 33 */     this.producer = producer;
/* 34 */     this.service = service;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void listen(OnsProduceEvent event) {
/*    */     try {
/* 41 */       this.service.before((Event)event);
/*    */       
/* 43 */       this.producer.send(event);
/*    */       
/* 45 */       this.service.after((Event)event);
/* 46 */     } catch (Throwable e) {
/*    */       
/* 48 */       this.service.fail((Event)event, e);
/*    */     } 
/*    */   }
/*    */ 
/*    */   
/*    */   public String getIdentity() {
/* 54 */     return OnsProduceEvent.class.getSimpleName();
/*    */   }
/*    */ }


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\event\listener\OnsProduceEventListener.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */