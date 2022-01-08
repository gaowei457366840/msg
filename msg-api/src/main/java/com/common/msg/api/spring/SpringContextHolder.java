package com.common.msg.api.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;


@Component
public class SpringContextHolder
        implements BeanFactoryPostProcessor {
    private static BeanFactory beanFactory;

    public static BeanFactory getApplicationContext() {
        checkBeanFactory();
        return beanFactory;
    }


    public static <T> T getBean(String name) {
        checkBeanFactory();
        return (T) beanFactory.getBean(name);
    }


    public static <T> T getBean(Class<T> clazz) {
        checkBeanFactory();
        return (T) beanFactory.getBean(clazz);
    }

    private static void checkBeanFactory() {
        if (beanFactory == null) {
            throw new IllegalStateException("beanFactory is not found.");
        }
    }


    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        SpringContextHolder.beanFactory = (BeanFactory) beanFactory;
    }
}


