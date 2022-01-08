package com.common.msg.api.storage;

import java.io.Closeable;
import java.util.Iterator;

public interface KeyValueIterator<K, V> extends Iterator<KeyValue<K, V>>, Closeable {
    void close();

    K peekNextKey();
}


