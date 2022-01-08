package com.common.msg.api.serialization;

import com.alibaba.fastjson.JSON;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
import com.common.msg.api.exception.SerializationException;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.util.TypeReferenceUtil;
import com.common.msg.api.bootstrap.MqConfig;


import java.lang.reflect.Type;


public class JsonSerializer
        extends AbstractSerializer<Object> {
    public byte[] serializer(Object data) {
        try {
            return StringUtil.toBytes(data, this.maxSize);
        } catch (Throwable e) {
            throw new SerializationException("Error when serializing object to byte[] with json. data:" + data, e);
        }
    }


    public Object deserializer(MqConfig config, byte[] data) {
        Type type;
        ConsumerConfig consumerConfig = (ConsumerConfig) config;
        if (consumerConfig.getListener() != null) {

            type = TypeReferenceUtil.getType(consumerConfig.getListener());
        } else {

            type = TypeReferenceUtil.getType(ConsumerConfigAccessor.getBean(consumerConfig),
                    ConsumerConfigAccessor.getMethod(consumerConfig).getName());
        }
        return JSON.parseObject(data, type, new com.alibaba.fastjson.parser.Feature[0]);
    }
}


