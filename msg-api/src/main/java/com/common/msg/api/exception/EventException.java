package com.common.msg.api.exception;


public class EventException
        extends MqClientException {
    public EventException(String message) {
        super(message);
    }

    public EventException(Throwable e) {
        super(e);
    }

    public EventException(String message, Throwable e) {
        super(message, e);
    }
}


