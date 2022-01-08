package com.common.msg.api.storage;

import com.common.msg.api.exception.SerializationException;
import com.common.msg.api.util.ProtobufUtil;
import com.common.msg.api.util.StringUtil;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Objects;


public final class StoreSerializer<K, V> {
    private final Serde<K> keySerde;
    private final Serde<V> valueSerde;

    public static <K, V> StoreSerializer<K, V> withBuiltinTypes(Class<K> keyClass, Class<V> valueClass) {
        return new StoreSerializer<>(serdeFrom(keyClass), serdeFrom(valueClass));
    }


    public StoreSerializer(Serde<K> keySerde, Serde<V> valueSerde) {
        Objects.requireNonNull(keySerde, "key serde cannot be null");
        Objects.requireNonNull(valueSerde, "value serde cannot be null");

        this.keySerde = keySerde;
        this.valueSerde = valueSerde;
    }


    public K keyFrom(byte[] rawKey) {
        return this.keySerde.deserialize(rawKey);
    }


    public V valueFrom(byte[] rawValue) {
        return this.valueSerde.deserialize(rawValue);
    }


    public byte[] rawKey(K key) {
        return this.keySerde.serialize(key);
    }


    public byte[] rawValue(V value) {
        return this.valueSerde.serialize(value);
    }


    public static final class StringSerde
            implements Serde<String> {
        public void close() {
        }


        public byte[] serialize(String data) {
            try {
                return StringUtil.toBytes(data, -1);
            } catch (Exception e) {
                throw new SerializationException("Error when serializing string to byte[]. data:" + data, e);
            }
        }


        public String deserialize(byte[] data) {
            try {
                return StringUtil.toString(data);
            } catch (Exception e) {
                throw new SerializationException("Error when serializing  byte[] to string due to unsupported encoding UTF-8");
            }
        }
    }


    public static final class ByteArraySerde
            implements Serde<byte[]> {
        public void close() {
        }


        public byte[] serialize(byte[] data) {
            return data;
        }


        public byte[] deserialize(byte[] data) {
            return data;
        }
    }

    public static final class ProtobufSerde
            implements Serde<Object> {
        private final Class type;

        ProtobufSerde(Class type) {
            this.type = type;
        }


        public void close() {
        }


        public byte[] serialize(Object data) {
            byte[] bytes;
            try {
                bytes = ProtobufUtil.serializer(data);
            } catch (Throwable e) {
                throw new SerializationException("Error when serializing object to byte[] with protobuf. data:" + data, e);
            }
            return bytes;
        }


        public Object deserialize(byte[] data) {
            try {
                return ProtobufUtil.deserializer(data, this.type);
            } catch (Throwable e) {
                throw new SerializationException("Error when serializing object to byte[] with protobuf. data:" + data, e);
            }
        }
    }


    public static final class StoreEventSerde
            implements Serde<StoreEvent> {
        public void close() {
        }


        public byte[] serialize(StoreEvent data) {
            byte[] bytes;
            try {
                StringBuilder builder = new StringBuilder(15);
                builder.append(data.getNextRetryTime()).append(";");
                builder.append(data.getType()).append(";");
                builder.append(data.getTopic()).append(";");
                builder.append(data.getGroupId()).append(";");
                builder.append(StringUtil.isNullOrEmpty(data.getMsgId()) ? "" : data.getMsgId()).append(";");
                builder.append(data.getRetries()).append(";");
                builder.append(data.getCurrentRetriedCount());

                bytes = builder.toString().getBytes("UTF-8");
            } catch (Throwable e) {
                throw new SerializationException("Error when serializing object to byte[] with protobuf. data:" + data, e);
            }
            return bytes;
        }


        public StoreEvent deserialize(byte[] data) {
            try {
                String str = new String(data, "UTF-8");
                String[] args = str.split(";");
                StoreEvent event = new StoreEvent();
                event.setNextRetryTime(Long.parseLong(args[0]));
                event.setType(args[1]);
                event.setTopic(args[2]);
                event.setGroupId(args[3]);
                event.setMsgId(args[4]);
                event.setRetries(Integer.parseInt(args[5]));
                event.setCurrentRetriedCount(Integer.parseInt(args[6]));
                return event;
            } catch (Throwable e) {
                throw new SerializationException("Error when serializing object to byte[] with protobuf. data:" + Arrays.toString(data), e);
            }
        }
    }

    public static Serde<String> String() {
        return new StringSerde();
    }

    public static Serde<byte[]> ByteArray() {
        return new ByteArraySerde();
    }

    public static ProtobufSerde Protobuf(Class type) {
        return new ProtobufSerde(type);
    }

    private static StoreEventSerde StoreEvent() {
        return new StoreEventSerde();
    }


    public static <T> Serde<T> serdeFrom(Class<T> type) {
        if (String.class.isAssignableFrom(type)) {
            return (Serde) String();
        }

        if (byte[].class.isAssignableFrom(type)) {
            return (Serde) ByteArray();
        }

        if (StoreEvent.class.isAssignableFrom(type)) {
            return (Serde<T>) StoreEvent();
        }


        throw new IllegalArgumentException("Unknown class for built-in serializer. Supported types are: String, ByteArray, Brotobuff");
    }

    static interface Serde<T> extends Closeable {
        void close();

        byte[] serialize(T param1T);

        T deserialize(byte[] param1ArrayOfbyte);
    }
}


