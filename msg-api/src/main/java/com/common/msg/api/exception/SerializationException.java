package com.common.msg.api.exception;


public class SerializationException
        extends MqClientException {
    private static final long serialVersionUID = 1L;

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }


    public SerializationException() {
    }


    public Throwable fillInStackTrace() {
        return this;
    }
}


