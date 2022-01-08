package com.common.msg.kafka.event.listener;

import com.common.msg.api.event.Event;
import com.common.msg.api.event.EventAdviceService;
import com.common.msg.api.event.EventListener;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.kafka.event.KafkaReportEvent;
import com.common.msg.kafka.producer.KafkaMessageProducer;


class KafkaReportEventListener
        implements EventListener<KafkaReportEvent> {
    private final KafkaMessageProducer producer;
    private final EventAdviceService service;

    KafkaReportEventListener(KafkaMessageProducer producer, EventAdviceService service) {
        this.producer = producer;
        this.service = service;
    }


    public void listen(KafkaReportEvent event) {
        try {
            this.producer.send(convert2((Event) event));

            this.service.after((Event) event);
        } catch (Throwable e) {

            this.service.fail((Event) event, e);
        }
    }


    public String getIdentity() {
        return KafkaReportEvent.class.getSimpleName();
    }


    private KafkaProduceEvent convert2(Event event) {
        KafkaProduceEvent produceEvent = new KafkaProduceEvent();


        return produceEvent;
    }
}


