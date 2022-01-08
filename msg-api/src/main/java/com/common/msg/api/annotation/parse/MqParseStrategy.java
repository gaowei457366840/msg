package com.common.msg.api.annotation.parse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface MqParseStrategy {
    void parseConsumer(Object paramObject, Method paramMethod);

    void parseProducer(Object paramObject, Field paramField);

    String getMqType();
}

