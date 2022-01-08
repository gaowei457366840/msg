package com.common.msg.api.storage;

public interface StateStore {
    String name();

    void init(StoreContext paramStoreContext);

    void flush();

    void close();

    boolean persistent();

    boolean isOpen();
}

