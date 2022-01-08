package com.common.msg.kafka.network.retry;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.kafka.network.Callback;
import com.common.msg.api.util.ThreadUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RetryMessageQueue<T> {
    private static final Logger log = LoggerFactory.getLogger(RetryMessageQueue.class);


    private final Map<Long, RetryMessage<T>> queue = new ConcurrentHashMap<>();


    private volatile int queueCurrentSize;


    private Integer queueMaxSize;


    private volatile boolean isClose = false;


    private Callback<RetryMessage<T>, Boolean> callback;

    private static final String QUEUE_SIZE = "mq.retry.queueSize";


    public RetryMessageQueue(Callback<RetryMessage<T>, Boolean> callback) {
        this.callback = callback;
    }

    public void init() {
        this.queueMaxSize = Integer.valueOf(ConfigManager.getInt("mq.retry.queueSize", 65535));
        log.info("Set retry queue max size as {}.", this.queueMaxSize);
        (new Thread(new RetryMessageQueueConsumeTask(), "RetryMessageConsumeTask")).start();
    }

    public boolean offer(RetryMessage<T> message) {
        if (this.isClose) {
            return true;
        }
        synchronized (RetryMessageQueue.class) {

            for (int i = 0; i < 10 &&
                    this.queueCurrentSize > this.queueMaxSize.intValue() && !this.isClose; i++) {
                if (i == 9) {

                    log.warn("Retry queue is full, queueMaxSize:{}, failed to add {}", this.queueMaxSize, message);
                    return false;
                }
                ThreadUtil.sleep(500L, log);
            }


            this.queue.put(Long.valueOf(message.getId()), message);
            this.queueCurrentSize++;
        }
        return true;
    }

    public void remove(long key) {
        this.queue.remove(Long.valueOf(key));
    }

    public void close() {
        this.isClose = true;
        this.queue.clear();
    }

    private class RetryMessageQueueConsumeTask implements Runnable {
        private RetryMessageQueueConsumeTask() {
        }

        public void run() {
            while (!RetryMessageQueue.this.isClose) {
                try {
                    for (Map.Entry<Long, RetryMessage<T>> entry : (Iterable<Map.Entry<Long, RetryMessage<T>>>) RetryMessageQueue.this.queue.entrySet()) {
                        if (RetryMessageQueue.this.isClose)
                            break;
                        tryConsume(((Long) entry.getKey()).longValue(), entry.getValue());
                    }
                } catch (Throwable t) {
                    RetryMessageQueue.log.warn("Retry message queue consume failed. ", t);
                    ThreadUtil.sleep(10000L);
                }
                ThreadUtil.sleep(10000L);
            }
        }

        private boolean tryConsume(long id, RetryMessage<T> message) {
            if (null != message) {
                if (System.currentTimeMillis() < message.getStartTime()) {
                    return false;
                }
                RetryMessageQueue.log.debug("Retry send jobLogRequest {}", message);
                return ((Boolean) RetryMessageQueue.this.callback.call(message)).booleanValue();
            }
            RetryMessageQueue.this.queue.remove(Long.valueOf(id));
            return true;
        }
    }
}

