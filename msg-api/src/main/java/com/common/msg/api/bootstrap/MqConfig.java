package com.common.msg.api.bootstrap;

import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.util.StringUtil;

import java.util.Objects;
import java.util.Properties;


public class MqConfig {

    private String topic;
    private String groupId;
    private MqTypeEnum type = MqTypeEnum.KAFKA;
    private String svrUrl;
    private Properties props;


    public void setType(MqTypeEnum type) {
        this.type = type;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setSvrUrl(String svrUrl) {
        this.svrUrl = svrUrl;
    }

    public void setSerializer(SerializerTypeEnum serializer) {
        this.serializer = serializer;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof MqConfig)) return false;
        MqConfig other = (MqConfig) o;
        if (!other.canEqual(this)) return false;
        Object this$type = getType(), other$type = other.getType();
        if (!Objects.equals(this$type, other$type)) return false;
        Object this$topic = getTopic(), other$topic = other.getTopic();
        if (!Objects.equals(this$topic, other$topic)) return false;
        Object this$groupId = getGroupId(), other$groupId = other.getGroupId();
        if (!Objects.equals(this$groupId, other$groupId)) return false;
        Object this$svrUrl = getSvrUrl(), other$svrUrl = other.getSvrUrl();
        if (!Objects.equals(this$svrUrl, other$svrUrl)) return false;
        Object this$serializer = getSerializer(), other$serializer = other.getSerializer();
        if (!Objects.equals(this$serializer, other$serializer))
            return false;
        Object this$props = getProps(), other$props = other.getProps();
        return !(!Objects.equals(this$props, other$props));
    }

    protected boolean canEqual(Object other) {
        return other instanceof MqConfig;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $type = getType();
        result = result * 59 + (($type == null) ? 43 : $type.hashCode());
        Object $topic = getTopic();
        result = result * 59 + (($topic == null) ? 43 : $topic.hashCode());
        Object $groupId = getGroupId();
        result = result * 59 + (($groupId == null) ? 43 : $groupId.hashCode());
        Object $svrUrl = getSvrUrl();
        result = result * 59 + (($svrUrl == null) ? 43 : $svrUrl.hashCode());
        Object $serializer = getSerializer();
        result = result * 59 + (($serializer == null) ? 43 : $serializer.hashCode());
        Object $props = getProps();
        return result * 59 + (($props == null) ? 43 : $props.hashCode());
    }

    public String toString() {
        return "MqConfig(type=" + getType() + ", topic=" + getTopic() + ", groupId=" + getGroupId() + ", svrUrl=" + getSvrUrl() + ", serializer=" + getSerializer() + ", props=" + getProps() + ")";
    }


    public MqTypeEnum getType() {
        return this.type;
    }

    public String getTopic() {
        return this.topic;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getSvrUrl() {
        return this.svrUrl;
    }

    private SerializerTypeEnum serializer = SerializerTypeEnum.BYTEARRAY;

    public SerializerTypeEnum getSerializer() {
        return this.serializer;
    }


    void setProps(Properties props) {
        this.props = props;
    }

    public Properties getProps() {
        return this.props;
    }

    public void put(String key, String value) {
        if (null == this.props) {
            createProperties();
        }
        this.props.setProperty(key, value);
    }

    public void put(String key, int value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, long value) {
        put(key, String.valueOf(value));
    }

    public void put(String key, boolean value) {
        put(key, String.valueOf(value));
    }

    public String getString(String key, String val) {
        if (null == this.props || StringUtil.isNullOrEmpty(this.props.getProperty(key))) {
            return val;
        }
        return this.props.getProperty(key);
    }

    public long getLong(String key, long val) {
        if (null == this.props || StringUtil.isNullOrEmpty(this.props.getProperty(key))) {
            return val;
        }
        return Long.parseLong(this.props.getProperty(key));
    }

    public int getInt(String key, int val) {
        if (null == this.props || StringUtil.isNullOrEmpty(this.props.getProperty(key))) {
            return val;
        }
        return Integer.parseInt(this.props.getProperty(key));
    }

    public boolean containsKey(String key) {
        return (null != this.props && this.props.containsKey(key));
    }

    public boolean getBoolean(String key, boolean val) {
        if (null == this.props || StringUtil.isNullOrEmpty(this.props.getProperty(key))) {
            return val;
        }
        return Boolean.parseBoolean(this.props.getProperty(key));
    }

    private synchronized void createProperties() {
        if (null == this.props)
            this.props = new Properties();
    }
}

