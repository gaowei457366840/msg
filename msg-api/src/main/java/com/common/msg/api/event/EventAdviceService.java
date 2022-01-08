package com.common.msg.api.event;

public interface EventAdviceService {
    Event before(Event paramEvent) throws Exception;

    void after(Event paramEvent);

    void fail(Event paramEvent, Throwable paramThrowable);
}

