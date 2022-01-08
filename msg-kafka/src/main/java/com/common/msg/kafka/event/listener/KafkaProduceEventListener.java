package com.common.msg.kafka.event.listener;

import com.common.msg.api.MessageAccessor;
import com.common.msg.api.event.Event;
import com.common.msg.api.event.EventAdviceService;
import com.common.msg.api.event.EventListener;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.kafka.producer.KafkaMessageProducer;
import com.common.msg.kafka.core.SpecialMessageProducer;

import java.util.Properties;


public class KafkaProduceEventListener implements EventListener<KafkaProduceEvent> {

    private final KafkaMessageProducer producer;
    private final EventAdviceService service;
    private final SpecialMessageProducer specialProducer;

    KafkaProduceEventListener(KafkaMessageProducer producer, EventAdviceService service, SpecialMessageProducer specialProducer) {
        this.producer = producer;
        this.service = service;
        this.specialProducer = specialProducer;
    }


    public void listen(KafkaProduceEvent event) {
        try {
            this.service.before((Event) event);

            sendMessage(event);

            this.service.after((Event) event);
        } catch (Throwable e) {

            this.service.fail((Event) event, e);
        }
    }


    public String getIdentity() {
        return KafkaProduceEvent.class.getSimpleName();
    }

    private void sendMessage(KafkaProduceEvent event) {
        Properties properties = MessageAccessor.getSystemProperties(event.getMessageRecord());
        if (properties == null) {
            this.producer.send(event);
        } else if (properties.containsKey("__TXMSG")) {
            this.specialProducer.sendTransactionMessage(event);
        } else if (event.getMessageRecord().getStartDeliverTime() > 1000L) {

            this.specialProducer.sendDelayMessage(event);
        } else {
            this.producer.send(event);
        }
    }
}

