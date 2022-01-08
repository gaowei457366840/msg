package com.common.msg.kafka.network;

import com.common.msg.api.util.MillisecondClock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class SyncFuture<T>
        implements Future<T> {
    private CountDownLatch latch = new CountDownLatch(1);


    private T response;


    private long beginTime = MillisecondClock.now();


    private RuntimeException error;


    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }


    public boolean isCancelled() {
        return false;
    }


    public boolean isDone() {
        return (this.response != null);
    }


    public T get() throws InterruptedException, ExecutionException {
        this.latch.await();
        if (this.error != null) {
            throw new ExecutionException(this.error);
        }
        return this.response;
    }


    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        if (this.latch.await(timeout, unit)) {
            if (this.error != null) {
                throw new ExecutionException(this.error);
            }
            return this.response;
        }
        return null;
    }


    public void setResponse(T response) {
        this.response = response;
        this.latch.countDown();
    }


    public void setError(RuntimeException error) {
        this.error = error;
        this.latch.countDown();
    }

    public long getBeginTime() {
        return this.beginTime;
    }
}


