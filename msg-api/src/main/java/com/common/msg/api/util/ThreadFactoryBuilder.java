package com.common.msg.api.util;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


public final class ThreadFactoryBuilder {
    private String nameFormat = null;
    private Boolean daemon = null;
    private Integer priority = null;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = null;
    private ThreadFactory backingThreadFactory = null;


    private static ThreadFactory build(ThreadFactoryBuilder builder) {
        final String nameFormat = builder.nameFormat;
        final Boolean daemon = builder.daemon;
        final Integer priority = builder.priority;
        final Thread.UncaughtExceptionHandler uncaughtExceptionHandler = builder.uncaughtExceptionHandler;


        final ThreadFactory backingThreadFactory = (builder.backingThreadFactory != null) ? builder.backingThreadFactory : Executors.defaultThreadFactory();
        final AtomicLong count = (nameFormat != null) ? new AtomicLong(0L) : null;
        return new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thread = backingThreadFactory.newThread(runnable);
                if (nameFormat != null) {
                    thread.setName(ThreadFactoryBuilder.format(nameFormat, new Object[]{Long.valueOf(count.getAndIncrement())}));
                }
                if (daemon != null) {
                    thread.setDaemon(daemon.booleanValue());
                }
                if (priority != null) {
                    thread.setPriority(priority.intValue());
                }
                if (uncaughtExceptionHandler != null) {
                    thread.setUncaughtExceptionHandler(uncaughtExceptionHandler);
                }
                return thread;
            }
        };
    }

    private static String format(String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }


    public ThreadFactoryBuilder setNameFormat(String nameFormat) {
        String unused = format(nameFormat, new Object[]{Integer.valueOf(0)});
        this.nameFormat = nameFormat;
        return this;
    }


    public ThreadFactoryBuilder setDaemon(boolean daemon) {
        this.daemon = Boolean.valueOf(daemon);
        return this;
    }


    public ThreadFactoryBuilder setPriority(int priority) {
        if (priority <= 1)
            throw new IllegalArgumentException(StringUtil.messageFormat("Thread priority (%s) must be >= %s", new Object[]{
                    Integer.valueOf(priority), Integer.valueOf(1)}));
        if (priority >= 10)
            throw new IllegalArgumentException(StringUtil.messageFormat("Thread priority (%s) must be <= %s", new Object[]{
                    Integer.valueOf(priority), Integer.valueOf(10)
            }));
        this.priority = Integer.valueOf(priority);
        return this;
    }


    public ThreadFactoryBuilder setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        if (uncaughtExceptionHandler == null) throw new NullPointerException();
        this.uncaughtExceptionHandler = uncaughtExceptionHandler;
        return this;
    }


    public ThreadFactoryBuilder setThreadFactory(ThreadFactory backingThreadFactory) {
        if (backingThreadFactory == null) throw new NullPointerException();
        this.backingThreadFactory = backingThreadFactory;
        return this;
    }


    public ThreadFactory build() {
        return build(this);
    }
}


