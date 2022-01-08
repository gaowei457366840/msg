package com.common.msg.kafka.consumer.strategy;

import com.common.msg.api.common.MessagePriorityEnum;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class KafkaConsumeStrategyFactory {
    private static volatile KafkaConsumeStrategyFactory instance;
    private final ConcurrentMap<Integer, KafkaConsumeStrategy> strategyMap = new ConcurrentHashMap<>(3);


    public static KafkaConsumeStrategyFactory getInstance() {
        if (null == instance) {
            synchronized (KafkaConsumeStrategyFactory.class) {
                if (null == instance) {
                    instance = new KafkaConsumeStrategyFactory();
                }
            }
        }
        return instance;
    }


    public KafkaConsumeStrategy getStrategy(MessagePriorityEnum priorityEnum) {
        int priority = priorityEnum.getCode();
        if (this.strategyMap.containsKey(Integer.valueOf(priority))) {
            return this.strategyMap.get(Integer.valueOf(priority));
        }
        return createStrategy(priority);
    }

    private synchronized KafkaConsumeStrategy createStrategy(int priority) {
        if (this.strategyMap.containsKey(Integer.valueOf(priority))) {
            return this.strategyMap.get(Integer.valueOf(priority));
        }
        KafkaConsumeStrategy strategy = new KafkaConsumeMediumPriorityStrategy();


        switch (priority) {
            case 1:
                strategy = new KafkaConsumeHighStrategy();

                this.strategyMap.put(Integer.valueOf(priority), strategy);
                return strategy;
            case 2:
                strategy = new KafkaConsumeMediumPriorityStrategy();
                this.strategyMap.put(Integer.valueOf(priority), strategy);
                return strategy;
            case 3:
                strategy = new KafkaConsumeLowPriorityStrategy();
                this.strategyMap.put(Integer.valueOf(priority), strategy);
                return strategy;
        }
        this.strategyMap.put(Integer.valueOf(priority), strategy);
        return strategy;
    }
}


