package com.common.msg.api.exception;


public class InvalidStateStoreException
        extends MqClientException {
    private static final long serialVersionUID = 1L;

    public InvalidStateStoreException(String message) {
        super(message);
    }

    public InvalidStateStoreException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public InvalidStateStoreException(Throwable throwable) {
        super(throwable);
    }
}


