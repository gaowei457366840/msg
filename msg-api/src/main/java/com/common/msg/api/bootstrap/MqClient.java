package com.common.msg.api.bootstrap;

import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.MqConsumer;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.event.EventBusFactory;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.bingding.ProducerConfig;
import com.common.msg.api.spring.BeanHolder;
import com.common.msg.api.util.ClassUtil;
import com.common.msg.api.util.MillisecondClock;
import com.common.msg.api.util.StringUtil;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MqClient {
    private static final Logger log = LoggerFactory.getLogger(MqClient.class);

    //存储的KafkaClientFactory
    private static final ConcurrentMap<String, MqFactory> FACTORY_MAP = new ConcurrentHashMap<>(2);

    private static boolean mqRouterSwitch;

    static {
        init();
    }


    /**
     * 初始化工厂MqFactory
     */
    private static void init() {
        ServiceLoader<MqFactory> serviceLoader = ServiceLoader.load(MqFactory.class);
        Iterator<MqFactory> mqFactories = serviceLoader.iterator();

        while (mqFactories.hasNext()) {

            MqFactory mqFactory = mqFactories.next();  //子类的构造方法KafkaClientFactory
            FACTORY_MAP.put(mqFactory.getMqType().getMemo(), mqFactory);
        }


        String routerSwitch = ConfigManager.getString("mq.router.switch", "off");
        mqRouterSwitch = ("on".equalsIgnoreCase(routerSwitch) || "true".equalsIgnoreCase(routerSwitch));

        String autoDestroySwitch = ConfigManager.getString("mq.auto.destroy", "on");
        boolean autoDestroy = ("on".equalsIgnoreCase(autoDestroySwitch) || "true".equalsIgnoreCase(autoDestroySwitch));

        if (autoDestroy) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    MqClient.shutdown();
                }
            }, "mq-shutdown-hook"));
        }
    }
    public static MqProducer buildProducer(ProducerConfig config) {
        verifyConfig((MqConfig) config);
        if (FACTORY_MAP.containsKey(config.getType().getMemo())) {
            return ((MqFactory) FACTORY_MAP.get(config.getType().getMemo())).buildProducer(config);
        }
        throw new MqClientException("Not support this MQ type:" + config.getType().getMemo());
    }


    public static MqConsumer buildConsumer(ConsumerConfig config) {
        verifyConfig((MqConfig) config);
        if (FACTORY_MAP.containsKey(config.getType().getMemo())) {
            return ((MqFactory) FACTORY_MAP.get(config.getType().getMemo())).buildConsumer(config);
        }
        throw new MqClientException("Not support this MQ type:" + config.getType().getMemo());
    }


    public static MqFactory getMqFactory(String mqType) {
        if (FACTORY_MAP.containsKey(mqType)) {
            return FACTORY_MAP.get(mqType);
        }
        return null;
    }


    public static void shutdown() {
        for (Map.Entry<String, MqFactory> entry : FACTORY_MAP.entrySet()) {
            ((MqFactory) entry.getValue()).shutdown();
        }
        BeanHolder.clear();
        EventBusFactory.getInstance().clearAllListener();
        ClassUtil.clear();
        MillisecondClock.stop();
    }

    private static void verifyConfig(MqConfig config) {
        if (StringUtil.isNullOrEmpty(config.getTopic())) {
            throw new MqConfigException("Parameter 'topic' can't be null." + config);
        }

        if (config instanceof ProducerConfig) {
            ProducerConfig producerConfig = (ProducerConfig) config;
            if (producerConfig.getPriority().getCode() > MessagePriorityEnum.LOW.getCode() || producerConfig.getPriority().getCode() < MessagePriorityEnum.HIGH.getCode()) {
                throw new MqConfigException("Not support this priority! ProducerConfig:" + producerConfig);
            }
        }

        boolean isNotKafka = (!StringUtil.isNullOrEmpty(config.getSvrUrl()) && !config.getSvrUrl().contains(MqTypeEnum.KAFKA.getMemo()));
        if (!mqRouterSwitch || isNotKafka) {
            String svrUrl;

            if (StringUtil.isNullOrEmpty(config.getSvrUrl())) {
                svrUrl = ConfigManager.getString("mq.default.url", "");
                if (StringUtil.isNullOrEmpty(svrUrl)) {
                    throw new MqConfigException("Parameter 'svrUrl' and 'mq.default.url' can't both be null. " + config);
                }
            } else {
                svrUrl = config.getSvrUrl();
            }
            String[] strs = StringUtil.parseSvrUrl(svrUrl);
            config.setType(MqTypeEnum.getEnum(strs[0]));
            config.setSvrUrl(strs[1]);
        }

        if (config instanceof ConsumerConfig && MqTypeEnum.KAFKA.equals(config.getType())) {
            ConsumerConfig consumerConfig = (ConsumerConfig) config;
            if (!StringUtil.isNullOrEmpty(consumerConfig.getTag()))
                throw new MqConfigException("Kafka consumer not support 'tag' yet. " + config);
        }
    }
}


