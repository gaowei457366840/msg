package com.common.msg.api.exception;


public class MqClientException
        extends RuntimeException {
    public MqClientException(String message) {
        super(message);
    }

    public MqClientException(Throwable e) {
        super(e);
    }

    public MqClientException(String message, Throwable e) {
        super(message, e);
    }

    public MqClientException() {
    }
}


