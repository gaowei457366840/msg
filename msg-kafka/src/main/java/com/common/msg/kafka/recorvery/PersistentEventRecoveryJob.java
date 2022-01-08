package com.common.msg.kafka.recorvery;

import com.common.msg.api.common.RetryTimeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.event.Event;
import com.common.msg.api.event.EventBusFactory;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.storage.KeyValue;
import com.common.msg.api.storage.KeyValueIterator;
import com.common.msg.api.storage.StoreEvent;
import com.common.msg.api.util.ClassUtil;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.bootstrap.ServiceLifecycle;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PersistentEventRecoveryJob
        implements ServiceLifecycle {
    private static final Logger log = LoggerFactory.getLogger(PersistentEventRecoveryJob.class);


    private final PersistentEventService service = (PersistentEventService) BeanHolder.getBean(PersistentEventService.class.getSimpleName());
    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2);


    public void startup() {
        this.scheduled.scheduleAtFixedRate(new Runnable() {
                                               public void run() {
                                                   try {
                                                       PersistentEventRecoveryJob.log.debug("Kafka recovery service start.");
                                                       PersistentEventRecoveryJob.this.execute(PersistentEventRecoveryJob.this.fetchData());
                                                   } catch (Throwable t) {
                                                       PersistentEventRecoveryJob.log.error("Kafka recovery service execute failed.", t);
                                                   }
                                               }
                                           }, ConfigManager.getInt("mq.recovery.initialDelay.time", 90),
                ConfigManager.getInt("mq.recovery.interval.time", 60), TimeUnit.SECONDS);

        this.scheduled.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    if (PersistentEventRecoveryJob.this.service.approximateNumEntries() > 100000L) {
                        PersistentEventRecoveryJob.log.warn("Need reconsume message > 100,000, please check the consume business. ");
                    }
                    Calendar calendar = Calendar.getInstance();
                    if (calendar.get(11) == 3) {
                        PersistentEventRecoveryJob.this.clearData();
                    }
                } catch (Throwable t) {
                    PersistentEventRecoveryJob.log.error("Check the recovery size failed.", t);
                }
            }
        }, 3600L, 3600L, TimeUnit.SECONDS);
    }

    private void clearData() {
        try {
            KeyValueIterator<StoreEvent, byte[]> iterator = this.service.range(new StoreEvent(MillisecondClock.now() - 180L * RetryTimeEnum.SEVEN.getMeme()), new StoreEvent(8L * RetryTimeEnum.SEVEN
                    .getMeme()));


            while (iterator.hasNext()) {
                KeyValue<StoreEvent, byte[]> keyValue = (KeyValue<StoreEvent, byte[]>) iterator.next();
                StoreEvent event = (StoreEvent) keyValue.key;
                log.warn("Message reconsume > 7 days, report and discard it:" + event);
                event.setPayload((byte[]) keyValue.value);
                this.service.report(event);
                this.service.delete((StoreEvent) keyValue.key);
            }
        } catch (Throwable t) {
            log.error("Check the message time failed.", t);
        }
    }

    private KeyValueIterator<StoreEvent, byte[]> fetchData() {
        return this.service.range(new StoreEvent(MillisecondClock.now() - 7L * RetryTimeEnum.SEVEN.getMeme()), new StoreEvent(
                MillisecondClock.now() - RetryTimeEnum.TWO.getMeme()));
    }

    private void execute(KeyValueIterator<StoreEvent, byte[]> iterator) {
        int count = 0;
        int successCount = 0;

        StoreEvent event = null;
        while (!Thread.currentThread().isInterrupted() && iterator.hasNext()) {
            try {
                KeyValue<StoreEvent, byte[]> keyValue = (KeyValue<StoreEvent, byte[]>) iterator.next();
                event = (StoreEvent) keyValue.key;
                if (keyValue.value == null) {
                    this.service.delete((StoreEvent) keyValue.key);
                    log.warn("Value is null, delete it. key:" + keyValue.key);
                }
                event.setPayload((byte[]) keyValue.value);
                router(event);
                this.service.delete((StoreEvent) keyValue.key);
                successCount++;
            } catch (Throwable t) {
                log.error("Retry message failed. report and discard it:" + event, t);
                this.service.report(event, t);
                this.service.delete(event);
            }
            count++;
        }
        if (count != 0) {
            log.debug("Kafka recovery service executed, data count:" + count + ", success count:" + successCount);
        }
    }


    private void router(StoreEvent storeEvent) {
        StoreEvent event = (StoreEvent) ClassUtil.newInstance(storeEvent.getType(), StoreEvent.class);
        event.setType(storeEvent.getType());
        event.setTopic(storeEvent.getTopic());
        event.setGroupId(storeEvent.getGroupId());
        event.setNextRetryTime(storeEvent.getNextRetryTime());
        event.setPayload(storeEvent.getPayload());
        event.setRetries(storeEvent.getRetries());
        event.setCurrentRetriedCount(storeEvent.getCurrentRetriedCount());
        event.setMsgId(storeEvent.getMsgId());
        EventBusFactory.getInstance().post((Event) event);
    }


    public void shutdown() {
        this.scheduled.shutdownNow();
    }
}

