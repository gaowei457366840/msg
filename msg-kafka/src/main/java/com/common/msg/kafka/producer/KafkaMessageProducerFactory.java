package com.common.msg.kafka.producer;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.bingding.ProducerConfig;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javax.security.auth.Destroyable;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * KafProducer 的生成工厂
 */
public class KafkaMessageProducerFactory implements Destroyable {
    private static final Logger log = LoggerFactory.getLogger(KafkaMessageProducerFactory.class);

    /*
     *存放KafkaProducer
     * key:svrUrl
     *value{
     *  map:
     *      key:client.id
     *      value ：KafkaProducer}
     */
    private final Map<String, Map<String, KafkaProducer<String, byte[]>>> urlKafkaProducerMap = new ConcurrentHashMap<>(3);

    /**
     * 設置cliendid
     * @param config
     */
    public void buildClientId(ProducerConfig config) {
        StringBuilder buffer = new StringBuilder();

        buffer.append(config.getType().getCode()).append(config.getSvrUrl()).append(config.getPriority().getCode());
        if (null != config.getProps()) {
            TreeMap<Object, Object> sortMap = new TreeMap<>();

            for (Map.Entry<Object, Object> element : config.getProps().entrySet()) {
                sortMap.put(element.getKey(), element.getValue());
            }
            for (Map.Entry<Object, Object> element : sortMap.entrySet()) {
                buffer.append(element.getKey()).append(element.getValue());
            }
        }

        String clientId = config.getGroupId() + "_" + StringUtil.getMd5_16(buffer.toString());
        config.put("client.id", clientId);
        log.debug("Build clientId:" + buffer.toString() + " " + config);
    }


    /**
     * 获取urlMap中获取KafkaProducer
     * 创建 kafka producer
     * @param config
     * @return
     */
    KafkaProducer<String, byte[]> getProducer(ProducerConfig config) {
        KafkaProducer<String, byte[]> producer = null;
        try {
            producer = (KafkaProducer<String, byte[]>) ((Map) this.urlKafkaProducerMap.get(config.getSvrUrl())).get(config.getProps().getProperty("client.id"));
        } catch (Exception exception) {
        }
        if (producer == null) {
            producer = createProducer(config);
        }

        return producer;
    }


    public void destroy() {
        for (Map.Entry<String, Map<String, KafkaProducer<String, byte[]>>> entry : this.urlKafkaProducerMap.entrySet()) {
            for (Map.Entry<String, KafkaProducer<String, byte[]>> innerEntry : (Iterable<Map.Entry<String, KafkaProducer<String, byte[]>>>) ((Map) entry.getValue()).entrySet()) {
                ((KafkaProducer) innerEntry.getValue()).flush();
                ((KafkaProducer) innerEntry.getValue()).close();
            }
            ((Map) entry.getValue()).clear();
        }
        this.urlKafkaProducerMap.clear();
        log.info("Destroy kafka producer successful.");
    }

    /**
     *創建 KafkaProducer
     *
     */
    private synchronized KafkaProducer<String, byte[]> createProducer(ProducerConfig config) {
        KafkaProducer<String, byte[]> producer = null;
        try {
            producer = (KafkaProducer<String, byte[]>) ((Map) this.urlKafkaProducerMap.get(config.getSvrUrl())).get(config.getProps().getProperty("client.id"));
        } catch (Exception e) {
            this.urlKafkaProducerMap.put(config.getSvrUrl(), new ConcurrentHashMap<>(3));
        }

        if (producer == null) {

            configProducer(config);

            Properties props = new Properties();

            for (Map.Entry<Object, Object> element : config.getProps().entrySet()) {

                props.put(element.getKey(), element.getValue());
            }

            props.put("bootstrap.servers", config.getSvrUrl());

            props.put("acks", String.valueOf(convert2Ack(config.getPriority().getCode())));

            props.put("retries", Integer.valueOf(config.getInt("retries", 128)));

            props.put("batch.size", Integer.valueOf(config.getInt("batch.size", 65536)));

            props.put("linger.ms", Integer.valueOf(config.getInt("linger.ms", 20)));

            props.put("buffer.memory", Integer.valueOf(config.getInt("buffer.memory", 33554432)));

            props.put("compression.type", config.getString("compression.type", "lz4"));

            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

            props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

            producer = new KafkaProducer(props);

            ((Map<String, KafkaProducer<String, byte[]>>) this.urlKafkaProducerMap.get(config.getSvrUrl())).put(config.getProps().getProperty("client.id"), producer);

            log.info("Create kafka producer successful. config:" + props);
        }

        return producer;
    }

    private void configProducer(ProducerConfig config) {

        switch (config.getPriority()) {
            case HIGH:

                if (!config.containsKey("linger.ms")) {

                    config.put("linger.ms",
                            ConfigManager.getInt(getConfigName("linger.ms"), 20));
                }

                if (!config.containsKey("retries")) {

                    config.put("retries",
                            ConfigManager.getInt(getConfigName("retries"), 128));
                }
                return;
            case MEDIUM:

                if (!config.containsKey("linger.ms")) {

                    config.put("linger.ms",
                            ConfigManager.getInt(getConfigName("linger.ms"), 50));
                }

                if (!config.containsKey("retries")) {

                    config.put("retries",
                            ConfigManager.getInt(getConfigName("retries"), 64));
                }
                return;
            case LOW:

                if (!config.containsKey("linger.ms")) {

                    config.put("linger.ms",
                            ConfigManager.getInt(getConfigName("linger.ms"), 100));
                }

                if (!config.containsKey("retries")) {

                    config.put("retries",
                            ConfigManager.getInt(getConfigName("retries"), 8));
                }
                return;
        }

        throw new MqConfigException("Parameter 'priority' is invalid. priority:" + config.getPriority());
    }


    private String getConfigName(String originName) {

        return "kafka." + originName;
    }


    private int convert2Ack(int priority) {

        if (priority == -1 || priority == 1)
            return -1;

        if (priority == 2) {

            return 1;
        }

        return 0;
    }


    private boolean isCustom(ProducerConfig config) {

        return (null == config.getProps() || config.getProps().size() == 1 || (config
                .getProps().size() == 2 && "lz4".equalsIgnoreCase(config.getProps().getProperty("compression.type"))));
    }
}

