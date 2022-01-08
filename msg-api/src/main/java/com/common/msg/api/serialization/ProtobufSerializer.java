package com.common.msg.api.serialization;

import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
import com.common.msg.api.exception.SerializationException;
import com.common.msg.api.util.ProtobufUtil;
import com.common.msg.api.util.TypeReferenceUtil;
import com.common.msg.api.bootstrap.MqConfig;


import java.lang.reflect.Type;


public class ProtobufSerializer
        extends AbstractSerializer<Object> {
    public byte[] serializer(Object data) {
        byte[] bytes;
        try {
            bytes = ProtobufUtil.serializer(data);
        } catch (Throwable e) {
            throw new SerializationException("Error when serializing object to byte[] with protobuf. data:" + data, e);
        }
        if (bytes.length > this.maxSize) {
            throw new SerializationException("Data size over max size. size:" + bytes.length + ", max size:" + this.maxSize);
        }
        return bytes;
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
        return ProtobufUtil.deserializer(data, (Class) type);
    }
}

