package com.common.msg.api.event;

import com.common.msg.api.exception.EventException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class EventBusFactory {
    private static final Logger log = LoggerFactory.getLogger(EventBusFactory.class);


    private static volatile EventBusFactory instance;


    private static final String DEFAULT_GROUP = "default";

    /**
     * key: KafkaProduceEvent.getName();
     * vakye: SynEventBus
     */

    private final Map<String, SynEventBus> itemMap = new ConcurrentHashMap<>();

    public static EventBusFactory getInstance() {
        if (null == instance) {
            synchronized (EventBusFactory.class) {
                if (null == instance) {
                    instance = new EventBusFactory();
                }
            }
        }
        return instance;
    }


    public synchronized void register(Class group, EventListener listener) {
        if (group == null || listener == null) throw new EventException("Group and Listener can't be null.");
        if (!this.itemMap.containsKey(group.getName())) {
            this.itemMap.put(group.getName(), new SynEventBus());
        }
        ((SynEventBus) this.itemMap.get(group.getName())).register(listener);
    }


    public void post(Event event) {
        String group;
        if (event == null) throw new EventException("Event can't be null.");

        if (event.getGroup() != null) {

            group = event.getGroup().getName();
        } else {

            group = event.getClass().getName();
        }
        //调用SynEventBus 的post方法， post方法会轮训listeners 监听 ，然后发送消息
        if (this.itemMap.containsKey(group)) {
            ((SynEventBus<Event>) this.itemMap.get(group)).post(event);
        } else {
            throw new EventException("This event has no EventBus instance.");
        }
    }


    public synchronized void clearListener(Class group) {
        if (group == null) throw new EventException("Group can't be null.");
        if (this.itemMap.containsKey(group.getName())) {
            ((SynEventBus) this.itemMap.get(group.getName())).clear();
        }
    }


    public synchronized void clearAllListener() {
        for (Map.Entry<String, SynEventBus> entry : this.itemMap.entrySet()) {
            ((SynEventBus) entry.getValue()).clear();
        }
        this.itemMap.clear();
    }
}


