package com.common.msg.api.util;

import com.common.msg.api.exception.MqClientException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


public class ClassUtil {
    private static final ConcurrentMap<String, Class<?>> CLASSES = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Object> INSTANCES = new ConcurrentHashMap<>();

    public static <T> T newInstance(String clz, Class<T> base) {
        return getInstance(clz, getClass(clz), base);
    }

    private static Class<?> getClass(String clz) {
        if (CLASSES.containsKey(clz)) {
            return CLASSES.get(clz);
        }

        synchronized (ClassUtil.class) {
            if (CLASSES.containsKey(clz)) {
                return CLASSES.get(clz);
            }
            try {
                Class<?> newCls = Class.forName(clz, true, getClassLoader());
                CLASSES.put(clz, newCls);
                return newCls;
            } catch (ClassNotFoundException e) {
                throw new MqClientException("Class not found from '" + clz + "'.", e);
            }
        }
    }


    private static <T> T getInstance(String clz, Class<?> c, Class<T> base) {
        if (INSTANCES.containsKey(clz)) {
            return (T) INSTANCES.get(clz);
        }

        try {
            synchronized (ClassUtil.class) {
                T instance = (T) c.newInstance();
                if (!base.isInstance(instance))
                    throw new MqClientException(c.getName() + " is not an instance of " + base.getName());
                INSTANCES.put(clz, instance);
                return instance;
            }
        } catch (IllegalAccessException e) {
            throw new MqClientException("Could not instantiate class " + c.getName(), e);
        } catch (InstantiationException e) {
            throw new MqClientException("Could not instantiate class " + c.getName() + " Does it have a public no-argument constructor?", e);
        } catch (NullPointerException e) {
            throw new MqClientException("Requested class was null", e);
        }
    }

    private static ClassLoader getClassLoader() {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            return getDefaultClassLoader();
        }
        return cl;
    }


    private static ClassLoader getDefaultClassLoader() {
        return ClassUtil.class.getClassLoader();
    }

    public static void clear() {
        CLASSES.clear();
        INSTANCES.clear();
    }
}


