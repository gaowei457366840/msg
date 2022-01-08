package com.common.msg.api.spring;

import com.common.msg.api.event.EventAdviceService;
import com.common.msg.api.event.EventAdviceServiceImpl;
import com.common.msg.api.exception.MqConfigException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BeanHolder {
    private static final Map<String, Object> BEAN_MAP = new ConcurrentHashMap<>();

    private static final BeanHolder INSTANCE = new BeanHolder();

    private BeanHolder() {
        EventAdviceServiceImpl eventAdviceServiceImpl = new EventAdviceServiceImpl();
        BEAN_MAP.put(EventAdviceService.class.getSimpleName(), eventAdviceServiceImpl);
    }


    public static <T> T getBean(String name) {
        if (BEAN_MAP.containsKey(name)) {
            return (T) BEAN_MAP.get(name);
        }
        return getSpringBean(name);
    }


    public static <T> void addBean(String name, T t) {
        if (BEAN_MAP.containsKey(name)) {
            throw new MqConfigException("This bean is already existed. name:" + name);
        }
        BEAN_MAP.put(name, t);
    }


    public static <T> T getBean(Class<T> clazz) {
        return getSpringBean(clazz);
    }


    private static <T> T getSpringBean(String name) {
        try {
            return SpringContextHolder.getBean(name);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private static <T> T getSpringBean(Class<T> clazz) {
        try {
            return SpringContextHolder.getBean(clazz);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    public static void clear() {
        BEAN_MAP.clear();
    }
}


