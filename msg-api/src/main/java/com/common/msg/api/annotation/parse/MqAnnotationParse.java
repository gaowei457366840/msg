package com.common.msg.api.annotation.parse;

import com.common.msg.api.config.ConfigManager;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.annotation.Consumer;
import com.common.msg.api.annotation.Producer;
import com.common.msg.api.exception.MqConfigException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;


@Component
public class MqAnnotationParse implements InitializingBean, BeanPostProcessor {
    private static final Logger log = LoggerFactory.getLogger(MqAnnotationParse.class);

    /**
     *   存储（MqParseStrategy）
     *   key：MqTypeEnum（消息类型） type
     *   value：MqParseStrategy（生成消费者，生成者的接口）
     */
    private Map<String, MqParseStrategy> strategyMap = new HashMap<>();

    private String defaultType;

    public void afterPropertiesSet() throws Exception {

        //配置文件中获取 mq.default.url对应的信息  是以下划线为分分界线的数组mq.default.url=kafka|kafka.test.za.net:9092
        String defaultSvrUrl = ConfigManager.getString("mq.default.url", "");

        if (!StringUtil.isNullOrEmpty(defaultSvrUrl)) {
            //获取数组中的第一个元素 “kafka” 设置 defaultType
            this.defaultType = StringUtil.parseSvrUrl(ConfigManager.getString("mq.default.url", ""))[0];
        }

        //类加载机制  获取继承MqParseStrategy类的所有类
        ServiceLoader<MqParseStrategy> serviceLoader = ServiceLoader.load(MqParseStrategy.class);
        Iterator<MqParseStrategy> parseStrategies = serviceLoader.iterator();

        while (parseStrategies.hasNext()) {
            MqParseStrategy parseStrategy = parseStrategies.next();
            //MqParseStrategy  与type对应
            this.strategyMap.put(parseStrategy.getMqType(), parseStrategy);
        }
    }


    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        try {
            //获取目标bean
            Object targetBean = getTargetBean(bean);
            //getDeclaredFields()：获得某个类的所有声明的字段，即包括public、private和proteced，但是不包括父类的申明字段。
            Field[] fields = targetBean.getClass().getDeclaredFields();

            //循环目标bean中所有的字段
            for (Field field : fields) {
                parseProducer(targetBean, field);
            }


            Method[] methods = targetBean.getClass().getDeclaredMethods();
            for (Method method : methods) {
                parseConsumer(targetBean, method);
            }
        } catch (Throwable t) {
            throw new MqConfigException("Annotation parse failed. bean:" + bean.getClass().getName(), t);
        }
        return bean;
    }

    private void parseProducer(Object bean, Field field) {
        //获取字段的注解“Producer”
        Producer annotation = field.getAnnotation(Producer.class);
        if (annotation != null) {
            //获取设置的 srvUrl
            String type = getType(annotation.svrUrl());
            //在strategyMap中通过 type 找到  MqParseStrategy的对应的类  并调用parseProducer方法

            getStrategy(type).parseProducer(bean, field);
        }
    }

    private void parseConsumer(Object bean, Method method) {
        Consumer annotation = method.<Consumer>getAnnotation(Consumer.class);
        if (annotation != null) {
            String type = getType(annotation.svrUrl());
            //在strategyMap中通过 type 找到  MqParseStrategy的对应的类
            getStrategy(type).parseConsumer(bean, method);
        }
    }

    private MqParseStrategy getStrategy(String type) {
        return this.strategyMap.get(type);
    }

    /**
     *   获取注解的urlTpye ，如果为null 则去默认的defaultType
     *   默认的 defaultType 在执行afterPropertiesSet的时候 设置过
     *
     */
    private String getType(String type) {
        type = StringUtil.getValue(type);
        if (StringUtil.isNullOrEmpty(type)) {
            if (StringUtil.isNullOrEmpty(this.defaultType)) {
                throw new MqConfigException("[mq.default.url] and [svrUrl] is null.");
            }
            return this.defaultType;
        }

        return StringUtil.parseSvrUrl(type)[0];
    }

    /**
     * 获取bean对象的目标对象 bean bean 继承了advised ，则获取其目标对象
     *
     * @param bean
     * @return 具体描述
     * instanceof的作用：在运行时判断对象是否是指定类及其父类的一个实例
     */
    private Object getTargetBean(Object bean) {
        if (bean instanceof Advised) {
            try {
                return ((Advised) bean).getTargetSource().getTarget();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return bean;
    }
}


