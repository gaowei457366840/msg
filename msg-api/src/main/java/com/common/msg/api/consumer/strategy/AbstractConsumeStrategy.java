package com.common.msg.api.consumer.strategy;

import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.util.StringUtil;
import com.common.msg.api.consumer.ReceiveRecord;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.api.consumer.binding.ConsumerConfigAccessor;
import com.common.msg.api.exception.MqClientException;
import com.common.msg.api.exception.MqConfigException;

import java.lang.reflect.Method;


public abstract class AbstractConsumeStrategy {
    protected boolean isRedeliver(ConsumerConfig config, ReceiveRecord message) {
        try {
            if (config.getListener() != null) {
                return config.getListener().isRedeliver(message);
            }
            if (!StringUtil.isNullOrEmpty(ConsumerConfigAccessor.getIsRedeliver(config))) {
                String name = ConsumerConfigAccessor.getIsRedeliver(config);
                Class klass = ConsumerConfigAccessor.getBean(config).getClass();

                Method method1 = ConsumerConfigAccessor.getMethod(config);
                method1.getParameterTypes();

                Method method = klass.getDeclaredMethod(name, method1.getParameterTypes());
                if (!method.getParameterTypes()[0].equals(ReceiveRecord.class)) {
                    throw new MqConfigException("@Consumer method [" + method + "] should only to be ReceiveRecord<?> ");
                }
                return ((Boolean) method.invoke(ConsumerConfigAccessor.getBean(config), new Object[]{message})).booleanValue();
            }
            return false;
        } catch (Throwable t) {
            throw new MqClientException("Check redeliver failed. message:" + message, t);
        }
    }


    protected AckAction onMessage(ConsumerConfig config, ReceiveRecord message) {
        try {
            if (config.getListener() != null) {
                return config.getListener().onMessage(message);
            }

            return (AckAction) ConsumerConfigAccessor.getMethod(config).invoke(
                    ConsumerConfigAccessor.getBean(config), new Object[]{message});
        } catch (Throwable t) {
            throw new MqClientException("Consume message failed, message:" + message, t);
        }
    }

    protected String messageInfo(ReceiveRecord message) {
        StringBuilder builder = new StringBuilder();
        builder.append(" message:topic[").append(message.getTopic()).append("]");
        builder.append(" msgId[").append(message.getMsgId()).append("]");
        builder.append(" key[").append(StringUtil.isNullOrEmpty(message.getKey()) ? "" : message.getKey()).append("]");
        return builder.toString();
    }
}

