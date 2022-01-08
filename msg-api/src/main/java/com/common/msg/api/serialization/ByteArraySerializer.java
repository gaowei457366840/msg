package com.common.msg.api.serialization;

import com.common.msg.api.exception.SerializationException;
import com.common.msg.api.bootstrap.MqConfig;


public class ByteArraySerializer
        extends AbstractSerializer<byte[]> {
    public byte[] serializer(byte[] data) {
        if (data.length <= this.maxSize) {
            return data;
        }
        throw new SerializationException("Data size over max size. size:" + data.length + ", max size:" + this.maxSize);
    }


    public byte[] deserializer(MqConfig config, byte[] data) {

        return data;
    }
}


