package com.common.msg.api.consumer.binding;

import java.lang.reflect.Method;


public class ConsumerConfigAccessor {
    public static Method getMethod(ConsumerConfig config) {
        return config.getMethod();
    }

    public static void setMethod(ConsumerConfig config, Method method) {
        config.setMethod(method);
    }

    public static Object getBean(ConsumerConfig config) {
        return config.getBean();
    }

    public static void setBean(ConsumerConfig config, Object object) {
        config.setBean(object);
    }

    public static String getIsRedeliver(ConsumerConfig config) {
        return config.getIsRedeliver();
    }

    public static void setIsRedeliver(ConsumerConfig config, String isRedeliver) {
        config.setIsRedeliver(isRedeliver);
    }
}


