package com.common.msg.kafka.event.listener;

import com.common.msg.api.event.EventAdviceService;
import com.common.msg.api.event.EventBusFactory;
import com.common.msg.kafka.event.KafkaConsumeEvent;
import com.common.msg.kafka.event.KafkaProduceEvent;
import com.common.msg.kafka.producer.KafkaMessageProducer;
import com.common.msg.kafka.core.SpecialMessageProducer;
import com.common.msg.kafka.event.KafkaReportEvent;


public class KafkaEventListenerManager {
    private final KafkaProduceEventListener kafkaProduceEventListener;
    private final KafkaReportEventListener kafkaReportEventListener;
    private final KafkaConsumeEventListener kafkaConsumeEventListener;

    public KafkaEventListenerManager(KafkaMessageProducer producer, SpecialMessageProducer specialProducer, EventAdviceService service) {
        this.kafkaProduceEventListener = new KafkaProduceEventListener(producer, service, specialProducer);
        this.kafkaReportEventListener = new KafkaReportEventListener(producer, service);
        this.kafkaConsumeEventListener = new KafkaConsumeEventListener();
        init();
    }


    private void init() {
        EventBusFactory.getInstance().register(KafkaProduceEvent.class, this.kafkaProduceEventListener);
        EventBusFactory.getInstance().register(KafkaReportEvent.class, this.kafkaReportEventListener);
        EventBusFactory.getInstance().register(KafkaConsumeEvent.class, this.kafkaConsumeEventListener);
    }
}


