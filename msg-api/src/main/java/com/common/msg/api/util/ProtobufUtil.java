package com.common.msg.api.util;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.util.concurrent.ConcurrentHashMap;


public class ProtobufUtil {
    private static final ConcurrentHashMap<String, Schema> SCHEMAS = new ConcurrentHashMap<>();

    public static <T> byte[] serializer(T o) {
        Schema schema = getSchema(o);
        return ProtostuffIOUtil.toByteArray(o, schema, LinkedBuffer.allocate(1024));
    }

    public static <T> T deserializer(byte[] bytes, Class<T> clazz) {
        T obj = null;
        try {
            obj = clazz.newInstance();
            Schema schema = getSchema(obj);
            ProtostuffIOUtil.mergeFrom(bytes, obj, schema);
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }

    private static <T> Schema getSchema(T o) {
        String className = getClassName(o);
        if (SCHEMAS.containsKey(className)) {
            return SCHEMAS.get(className);
        }
        return createSchema(o);
    }

    private static synchronized <T> Schema createSchema(T o) {
        String className = getClassName(o);
        if (SCHEMAS.containsKey(className)) {
            return SCHEMAS.get(className);
        }
        Schema schema = RuntimeSchema.getSchema(o.getClass());
        SCHEMAS.put(className, schema);
        return schema;
    }

    private static <T> String getClassName(T o) {
        return o.getClass().getName();
    }
}

