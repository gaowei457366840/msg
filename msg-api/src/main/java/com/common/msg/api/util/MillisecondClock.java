package com.common.msg.api.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MillisecondClock {
    private static final Logger log = LoggerFactory.getLogger(MillisecondClock.class);

    private long rate = 100L;


    private volatile long now = 0L;

    private static volatile boolean isClose;

    private MillisecondClock() {
        this.now = System.currentTimeMillis();
        start();
    }

    private static class MillisecondClockHolder {
        public static MillisecondClock instance = new MillisecondClock();
    }

    private static MillisecondClock getInstance() {
        return MillisecondClockHolder.instance;
    }

    private void start() {
        (new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !MillisecondClock.isClose) {
                    try {
                        Thread.sleep(MillisecondClock.this.rate);
                    } catch (InterruptedException e) {
                        MillisecondClock.log.error("MillisecondClock failed.", e);
                        Thread.currentThread().interrupt();
                    }
                    MillisecondClock.this.now = System.currentTimeMillis();
                }
            }
        })).start();
    }

    public static long now() {
        return (getInstance()).now;
    }

    public static void stop() {
        isClose = true;
    }
}


