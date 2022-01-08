package com.common.msg.kafka.core.model;

import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.producer.bingding.ProducerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class JoinGroupRequest
        extends CommonRequest {
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof JoinGroupRequest)) return false;
        JoinGroupRequest other = (JoinGroupRequest) o;
        if (!other.canEqual(this)) return false;
        if (!super.equals(o)) return false;
        Object this$sarname = getSarname(), other$sarname = other.getSarname();
        if (!Objects.equals(this$sarname, other$sarname)) return false;
        Object this$version = getVersion(), other$version = other.getVersion();
        if (!Objects.equals(this$version, other$version)) return false;
        Object this$language = getLanguage(), other$language = other.getLanguage();
        if (!Objects.equals(this$language, other$language)) return false;
        ConsumerConfig this$consumerConfigs = (ConsumerConfig) getConsumerConfigs(), other$consumerConfigs = (ConsumerConfig) other.getConsumerConfigs();
        if (!Objects.equals(this$consumerConfigs, other$consumerConfigs))
            return false;
        ProducerConfig this$producerConfigs = (ProducerConfig) getProducerConfigs(), other$producerConfigs = (ProducerConfig) other.getProducerConfigs();
        return !(!Objects.equals(this$producerConfigs, other$producerConfigs));
    }

    protected boolean canEqual(Object other) {
        return other instanceof JoinGroupRequest;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = super.hashCode();
        Object $sarname = getSarname();
        result = result * 59 + (($sarname == null) ? 43 : $sarname.hashCode());
        Object $version = getVersion();
        result = result * 59 + (($version == null) ? 43 : $version.hashCode());
        Object $language = getLanguage();
        result = result * 59 + (($language == null) ? 43 : $language.hashCode());
        ConsumerConfig $consumerConfigs = (ConsumerConfig) getConsumerConfigs();
        result = result * 59 + (($consumerConfigs == null) ? 43 : $consumerConfigs.hashCode());
        ProducerConfig $producerConfigs = (ProducerConfig) getProducerConfigs();
        return result * 59 + (($producerConfigs == null) ? 43 : $producerConfigs.hashCode());
    }

    public String toString() {
        return "JoinGroupRequest(super=" + super.toString() + ", sarname=" + getSarname() + ", version=" + getVersion() + ", language=" + getLanguage() + ", consumerConfigs=" + getConsumerConfigs() + ", producerConfigs=" + getProducerConfigs() + ")";
    }

    public void setSarname(String sarname) {
        this.sarname = sarname;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setConsumerConfigs(List<ConsumerConfig> consumerConfigs) {
        this.consumerConfigs = consumerConfigs;
    }

    public void setProducerConfigs(List<ProducerConfig> producerConfigs) {
        this.producerConfigs = producerConfigs;
    }

    public String getSarname() {

        return this.sarname;
    }

    public String getVersion() {

        return this.version;
    }

    public String getLanguage() {

        return this.language;
    }

    public List<ConsumerConfig> getConsumerConfigs() {
        return this.consumerConfigs;
    }

    public List<ProducerConfig> getProducerConfigs() {

        return this.producerConfigs;
    }

    private List<ConsumerConfig> consumerConfigs = new ArrayList<>();
    private List<ProducerConfig> producerConfigs = new ArrayList<>();
    private String language = "java";
    private String sarname;
    private String version;

    public JoinGroupRequest(String sarname, String version) {
        this();
        this.sarname = sarname;
        this.version = version;
    }

    public JoinGroupRequest(String sarname, String version, String language) {
        this(sarname, version);
        this.language = language;
    }

    public boolean setConfigList(List<MqConfig> list) {
        boolean result = false;
        for (MqConfig config : list) {
            if (config instanceof ConsumerConfig) {
                this.consumerConfigs.add((ConsumerConfig) config);
            }
            if (config instanceof ProducerConfig) {
                this.producerConfigs.add((ProducerConfig) config);
                if (((ProducerConfig) config).getChecker() != null) {

                    result = true;
                }
            }
        }
        return result;
    }

    public JoinGroupRequest() {
    }
}


