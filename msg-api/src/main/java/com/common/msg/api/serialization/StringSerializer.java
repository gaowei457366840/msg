package com.common.msg.api.serialization;

import com.common.msg.api.util.StringUtil;
import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.exception.SerializationException;


public class StringSerializer extends AbstractSerializer<String> {
    public byte[] serializer(String data) {
        try {
            //1M
            return StringUtil.toBytes(data, this.maxSize);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing string to byte[]. data:" + data, e);
        }
    }


    public String deserializer(MqConfig config, byte[] data) {
        try {
            return StringUtil.toString(data);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing  byte[] to string due to unsupported encoding UTF-8");
        }
    }
}

