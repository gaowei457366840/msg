package com.common.msg.kafka.annotation.parse;

import com.common.msg.api.common.MessageModelEnum;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.MqTypeEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.annotation.Consumer;
import com.common.msg.api.annotation.Producer;
import com.common.msg.api.annotation.parse.MqParseStrategy;
import com.common.msg.api.bootstrap.MqClient;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.bingding.ProducerConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;


public class KafkaAnnotationParse implements MqParseStrategy {
    private static final Logger log = LoggerFactory.getLogger(KafkaAnnotationParse.class);

    /**
     * bean：被 Producer 注解的对象
     * field ： field 具体bean中被注入的属性
     *革根据bean 与filed生成 mqProducer
     * @param bean
     * @param field
     */
    public void parseProducer(Object bean, Field field) {

        //获取注解信息
        Producer annotation = field.getAnnotation(Producer.class);

        //获取注解中svrUrl
        String svrUrl = StringUtils.trimWhitespace(annotation.svrUrl());
        svrUrl = StringUtil.getValue(svrUrl);
        if (StringUtil.isNullOrEmpty(svrUrl)) {
            svrUrl = ConfigManager.getString("mq.default.url", "");
            if (StringUtil.isNullOrEmpty(svrUrl)) {
                throw new MqConfigException("@Producer's [svrUrl] is required. bean [" + bean.getClass().getName() + "].");
            }
        }


        //获取topic
        String topic = StringUtils.trimWhitespace(annotation.topic());
        topic = StringUtil.getValue(topic);
        if (StringUtil.isNullOrEmpty(topic)) {
            throw new MqConfigException("@Producer's [topic] is required. bean [" + bean.getClass().getName() + "].");
        }


        //获取优先级
        MessagePriorityEnum priority = annotation.priority();
        if (priority.equals(MessagePriorityEnum.UNKNOWN)) {
            throw new MqConfigException("@Producer's [priority] is invalid. bean [" + bean.getClass().getName() + "].");
        }

        //获取序列化方式
        SerializerTypeEnum serializer = annotation.serializer();
        if (serializer.equals(SerializerTypeEnum.UNKNOWN)) {
            throw new MqConfigException("@Producer's [serializer] is invalid. bean [" + bean.getClass().getName() + "].");
        }


        ProducerConfig config = new ProducerConfig(svrUrl, topic, annotation.priority());
        config.setSerializer(serializer);
        try {
            MqProducer mqProducer = MqClient.buildProducer(config);
            ReflectionUtils.makeAccessible(field);
            ReflectionUtils.setField(field, bean, mqProducer);
        } catch (Throwable t) {
            throw new MqConfigException("Create producer failed. bean [" + bean.getClass().getName() + "] config:" + config, t);
        }
    }


    public void parseConsumer(Object bean, Method method) {
        Consumer annotation = method.<Consumer>getAnnotation(Consumer.class);


        String svrUrl = StringUtils.trimWhitespace(annotation.svrUrl());
        svrUrl = StringUtil.getValue(svrUrl);
        if (StringUtil.isNullOrEmpty(svrUrl)) {
            svrUrl = ConfigManager.getString("mq.default.url", "");
            if (StringUtil.isNullOrEmpty(svrUrl)) {
                throw new MqConfigException("@Consumer's [svrUrl] is required [" + method + "]. bean [" + bean.getClass().getName() + "].");
            }
        }

        String topic = StringUtils.trimWhitespace(annotation.topic());
        topic = StringUtil.getValue(topic);
        if (StringUtil.isNullOrEmpty(topic)) {
            throw new MqConfigException("@Consumer's [topic] is required [" + method + "]. bean [" + bean.getClass().getName() + "].");
        }

        String groupId = StringUtils.trimWhitespace(annotation.groupId());

        String tag = StringUtils.trimWhitespace(annotation.tag());

        Type[] types = method.getGenericParameterTypes();
        if (types.length != 1 && !ReceiveRecord.class.isAssignableFrom(types[0].getClass())) {
            throw new MqClientException("@Consumer's method [" + method + "] should only have 1 parameter and which type supposed to be ReceiveRecord<?>. bean [" + bean
                    .getClass().getName() + "].");
        }
        if (!method.getParameterTypes()[0].equals(ReceiveRecord.class)) {
            throw new MqConfigException("@Consumer's method [" + method + "] should only to be ReceiveRecord<?>. bean [" + bean
                    .getClass().getName() + "].");
        }

        if (!method.getReturnType().equals(AckAction.class)) {
            throw new MqConfigException("@Consumer's method [" + method + "] should only return AckAction. bean [" + bean
                    .getClass().getName() + "].");
        }


        MessageModelEnum pattern = annotation.pattern();


        int numThreads = annotation.numThreads();
        if (numThreads <= 0) {
            throw new MqConfigException("@Consumer's [numThreads] can not <= 0. bean [" + bean.getClass().getName() + "].");
        }

        int batchSize = annotation.batchSize();
        if (batchSize <= 0) {
            throw new MqConfigException("@Consumer's [batchSize] can not <= 0. bean [" + bean.getClass().getName() + "].");
        }
        int retries = annotation.retries();
        if (retries <= 0) {
            throw new MqConfigException("@Consumer's [retries] can not <= 0. bean [" + bean.getClass().getName() + "].");
        }


        String isRedeliver = annotation.isRedeliver();


        SerializerTypeEnum serializer = annotation.serializer();
        if (serializer.equals(SerializerTypeEnum.UNKNOWN)) {
            throw new MqConfigException("@Consumer's [serializer] is invalid. bean [" + bean.getClass().getName() + "].");
        }


        ConsumerConfig config = new ConsumerConfig(topic);
        config.setSvrUrl(svrUrl);
        config.setTag(tag);
        config.setGroupId(groupId);
        config.setPattern(pattern);
        config.setPriority(annotation.priority());
        ConsumerConfigAccessor.setBean(config, bean);
        ConsumerConfigAccessor.setMethod(config, method);
        ConsumerConfigAccessor.setIsRedeliver(config, isRedeliver);
        config.setNumThreads(numThreads);
        config.setBatchSize(batchSize);
        config.setRetries(retries);
        config.setSerializer(serializer);
        try {
            MqClient.buildConsumer(config);
        } catch (Throwable t) {
            throw new MqConfigException("Create consumer failed. bean [" + bean.getClass().getName() + "] config:" + config, t);
        }
    }


    public String getMqType() {
        return MqTypeEnum.KAFKA.getMemo();
    }
}


