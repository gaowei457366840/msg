package com.common.msg.api.exception;


public class MqConfigException
        extends MqClientException {
    private static final long serialVersionUID = 1L;

    public MqConfigException(String message) {
        super(message);
    }

    public MqConfigException(String name, Object value) {
        this(name, value, null);
    }

    public MqConfigException(String name, Object value, String message) {
        super("Invalid value " + value + " for configuration " + name + ((message == null) ? "" : (": " + message)));
    }
}


