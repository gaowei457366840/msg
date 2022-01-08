package com.common.msg.api.storage;

import java.util.Objects;


public class KeyValue<K, V> {
    public final K key;
    public final V value;

    public KeyValue(K key, V value) {
        this.key = key;
        this.value = value;
    }


    public static <K, V> KeyValue<K, V> pair(K key, V value) {
        return new KeyValue<>(key, value);
    }


    public String toString() {
        return "KeyValue(" + this.key + ", " + this.value + ")";
    }


    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof KeyValue)) {
            return false;
        }

        KeyValue other = (KeyValue) obj;

        return ((Objects.equals(this.key, other.key)) && (Objects.equals(this.value, other.value)));
    }


    public int hashCode() {
        return Objects.hash(new Object[]{this.key, this.value});
    }
}


