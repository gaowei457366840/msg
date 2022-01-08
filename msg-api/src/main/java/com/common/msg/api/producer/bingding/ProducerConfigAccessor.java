package com.common.msg.api.producer.bingding;

import java.lang.reflect.Method;


public class ProducerConfigAccessor {
    public static Method getMethod(ProducerConfig config) {
        return config.getMethod();
    }

    public static void setMethod(ProducerConfig config, Method method) {
        config.setMethod(method);
    }

    public static Object getBean(ProducerConfig config) {
        return config.getBean();
    }

    public static void setBean(ProducerConfig config, Object object) {
        config.setBean(object);
    }
}


