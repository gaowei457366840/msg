package com.common.msg.kafka.recorvery;

import com.common.msg.api.common.RetryTimeEnum;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.storage.KeyValueIterator;
import com.common.msg.api.storage.KeyValueStore;
import com.common.msg.api.storage.StoreEvent;
import com.common.msg.api.util.MillisecondClock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;


public class PersistentEventService {
    private static final Logger log = LoggerFactory.getLogger(PersistentEventService.class);

    private final KeyValueStore<StoreEvent, byte[]> store;


    public PersistentEventService() {
        KeyValueStore<StoreEvent, byte[]> store1;
        try {
            store1 = (KeyValueStore<StoreEvent, byte[]>) BeanHolder.getBean(KeyValueStore.class.getSimpleName());
        } catch (NoSuchBeanDefinitionException e) {
            store1 = null;
        }
        this.store = store1;
    }

    public void retry(StoreEvent event) {
        if (null == event || null == event.getPayload())
            return;
        if (event.getRetries() == event.getCurrentRetriedCount()) {
            report(event);
        } else {
            event.setCurrentRetriedCount(event.getCurrentRetriedCount() + 1);
            event.setNextRetryTime(MillisecondClock.now() + RetryTimeEnum.getMemo(event.getCurrentRetriedCount()));
            save(event);
        }
    }

    public void report(StoreEvent event) {
        event.setPayload(null);
    }

    public void report(StoreEvent event, Throwable throwable) {
        event.setPayload(null);
    }

    public void save(StoreEvent event) {
        byte[] payload = event.getPayload();
        event.setPayload(null);
        this.store.put(event, payload);
    }

    public void delete(StoreEvent event) {
        if (null != event.getPayload()) {
            event.setPayload(null);
        }
        this.store.delete(event);
    }

    public long approximateNumEntries() {
        return this.store.approximateNumEntries();
    }

    public KeyValueIterator<StoreEvent, byte[]> range(StoreEvent from, StoreEvent to) {
        return this.store.range(from, to);
    }
}


