package com.common.msg.api.serialization;

import com.common.msg.api.bootstrap.MqConfig;

public interface Serializer<T> {
    byte[] serializer(T paramT);

    T deserializer(MqConfig paramMqConfig, byte[] paramArrayOfbyte);

    void setMaxSize(int paramInt);
}


