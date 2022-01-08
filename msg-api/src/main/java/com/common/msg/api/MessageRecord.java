package com.common.msg.api;

import java.util.Objects;
import java.util.Properties;


public class MessageRecord<V> {
    private Properties userProperties;
    private Properties systemProperties;
    private String key;
    private V message;

    public void setKey(String key) {
        this.key = key;
    }

    public void setMessage(V message) {
        this.message = message;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MessageRecord)) return false;
        MessageRecord<?> other = (MessageRecord) o;
        if (!other.canEqual(this)) return false;
        Object this$userProperties = getUserProperties(), other$userProperties = other.getUserProperties();
        if (!Objects.equals(this$userProperties, other$userProperties))
            return false;
        Object this$systemProperties = getSystemProperties(), other$systemProperties = other.getSystemProperties();
        if (!Objects.equals(this$systemProperties, other$systemProperties))
            return false;
        Object this$key = getKey(), other$key = other.getKey();
        if (!Objects.equals(this$key, other$key)) return false;
        Object this$message = getMessage(), other$message = other.getMessage();
        return !(!Objects.equals(this$message, other$message));
    }

    protected boolean canEqual(Object other) {
        return other instanceof MessageRecord;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $userProperties = getUserProperties();
        result = result * 59 + (($userProperties == null) ? 43 : $userProperties.hashCode());
        Object $systemProperties = getSystemProperties();
        result = result * 59 + (($systemProperties == null) ? 43 : $systemProperties.hashCode());
        Object $key = getKey();
        result = result * 59 + (($key == null) ? 43 : $key.hashCode());
        Object $message = getMessage();
        return result * 59 + (($message == null) ? 43 : $message.hashCode());
    }

    public String toString() {
        return "MessageRecord(userProperties=" + getUserProperties() + ", systemProperties=" + getSystemProperties() + ", key=" + getKey() + ", message=" + getMessage() + ")";
    }


    public String getKey() {
        return this.key;
    }

    public V getMessage() {
        return this.message;
    }

    public MessageRecord() {
    }

    public MessageRecord(V message) {
        this.message = message;
    }

    public MessageRecord(String key, V message) {
        this(message);
        this.key = key;
    }


    public void setStartDeliverTime(long value) {
        putSystemProperties("__DELIVERTIME", String.valueOf(value));
    }

    public long getStartDeliverTime() {
        String pro = getSystemProperties("__DELIVERTIME");
        if (pro != null) {
            return Long.parseLong(pro);
        }

        return 0L;
    }

    Properties getSystemProperties() {
        return this.systemProperties;
    }

    void setSystemProperties(Properties systemProperties) {
        this.systemProperties = systemProperties;
    }

    public Properties getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(Properties userProperties) {
        this.userProperties = userProperties;
    }

    public void setTag(String tag) {
        putSystemProperties("__TAG", tag);
    }

    public String getTag() {
        return getSystemProperties("__TAG");
    }

    public void putUserProperties(String key, String value) {
        if (null == this.userProperties) {
            this.userProperties = new Properties();
        }

        if (key != null && value != null) {
            this.userProperties.put(key, value);
        }
    }

    public String getUserProperties(String key) {
        if (null != this.userProperties) {
            return (String) this.userProperties.get(key);
        }

        return null;
    }

    private String getSystemProperties(String key) {
        if (null != this.systemProperties) {
            return this.systemProperties.getProperty(key);
        }

        return null;
    }

    void putSystemProperties(String key, String value) {
        if (null == this.systemProperties) {
            this.systemProperties = new Properties();
        }

        if (key != null && value != null)
            this.systemProperties.put(key, value);
    }

    public static class SystemPropKey {
        public static final String TAG = "__TAG";
        public static final String DELIVERTIME = "__DELIVERTIME";
        public static final String SHARDINGKEY = "__SHARDINGKEY";
        public static final String TXMSG = "__TXMSG";
        public static final String SID = "__SID";
    }
}


