package com.common.msg.api.serialization;


abstract class AbstractSerializer<T> implements Serializer<T> {
    int maxSize;

    public void setMaxSize(int maxSize) {

        this.maxSize = maxSize;
    }
}

