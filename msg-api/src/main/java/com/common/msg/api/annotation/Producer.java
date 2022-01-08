package com.common.msg.api.annotation;

import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.SerializerTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
@Inherited
public @interface Producer {
    //消息的类型|服务的地址
    String svrUrl() default "";

    String topic();

    String checker() default "";

    //优先级
    MessagePriorityEnum priority() default MessagePriorityEnum.MEDIUM;

    SerializerTypeEnum serializer() default SerializerTypeEnum.BYTEARRAY;
}


