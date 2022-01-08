package com.common.msg.api.storage;

import java.util.List;

public interface KeyValueStore<K, V> extends StateStore {
    V get(K paramK);

    void put(K paramK, V paramV);

    V putIfAbsent(K paramK, V paramV);

    void putAll(List<KeyValue<K, V>> paramList);

    V delete(K paramK);

    KeyValueIterator<K, V> range(K paramK1, K paramK2);

    KeyValueIterator<K, V> all();

    long approximateNumEntries();
}

