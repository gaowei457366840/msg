package com.common.msg.api.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SynEventBus<T> {

    /**
     * 监听信息
     * key：kafkaProduceEventListener.class
     * value：kafkaProduceEventListener
     */
    private final Map<Class<?>, EventListener<T>> listeners = new ConcurrentHashMap<>(3);

    private final String identifier;

    public SynEventBus() {
        this("default");
    }

    public SynEventBus(String identifier) {
        this.identifier = identifier;
    }

    public void register(EventListener<T> listener) {
        this.listeners.put(listener.getClass(), listener);
    }

    public void unregister(EventListener<T> listener) {
        this.listeners.remove(listener.getClass());
    }

    public void post(T t) {
        for (Map.Entry<Class<?>, EventListener<T>> entry : this.listeners.entrySet()) {
            ((EventListener<T>) entry.getValue()).listen(t);
        }
    }

    public void clear() {
        this.listeners.clear();
    }

    public final String identifier() {
        return this.identifier;
    }
}

