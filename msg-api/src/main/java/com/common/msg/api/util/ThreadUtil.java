package com.common.msg.api.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;


public class ThreadUtil {
    public static final String THREAD_NAME = "_T%d";

    public static void sleep(long millis) {

        sleep(millis, null);
    }

    public static void sleep(long millis, Logger log) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            if (log != null) {
                log.warn("Thread interrupted.", e);
            }
            Thread.currentThread().interrupt();
        }
    }


    public static ExecutorService newSinglePool(String name) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(name + "_T%d").build();
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }


    public static ExecutorService newPool(String name, int corePoolSize, int maxPoolSize, int queueCapacity) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(name + "_T%d").build();
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, 120L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }


    public static ExecutorService newPool(String name, int corePoolSize, int maxPoolSize, long keepAliveTime, int queueCapacity, RejectedExecutionHandler executionHandler) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(name + "_T%d").build();
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueCapacity), threadFactory, executionHandler);
    }


    public static ExecutorService newCachedPool(String name) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(name + "_T%d").build();
        return new ThreadPoolExecutor(0, 65535, 120L, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    }


    public static ExecutorService newCachedPool(String name, int corePoolSize, int maxPoolSize, long keepAliveTime, RejectedExecutionHandler executionHandler) {
        ThreadFactory threadFactory = (new ThreadFactoryBuilder()).setNameFormat(name + "_T%d").build();
        return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new SynchronousQueue<>(), threadFactory, executionHandler);
    }
}


