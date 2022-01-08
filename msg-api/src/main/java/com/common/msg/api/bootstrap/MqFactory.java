package com.common.msg.api.bootstrap;

import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.consumer.MqConsumer;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.bingding.ProducerConfig;

public interface MqFactory extends ServiceLifecycle {
    MqProducer buildProducer(ProducerConfig paramProducerConfig);

    MqConsumer buildConsumer(ConsumerConfig paramConsumerConfig);

    MqTypeEnum getMqType();
}


