package com.common.msg.api;

import java.util.Properties;

public class MessageAccessor {
    public static Properties getSystemProperties(MessageRecord msg) {
        return msg.getSystemProperties();
    }

    public static void setSystemProperties(MessageRecord msg, Properties properties) {
        msg.setSystemProperties(properties);
    }

    public static void putSystemProperties(MessageRecord msg, String key, String value) {
        msg.putSystemProperties(key, value);
    }
}


