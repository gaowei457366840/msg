package com.common.msg.api.annotation;

import com.common.msg.api.common.MessageModelEnum;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.SerializerTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Documented
@Inherited
public @interface Consumer {
    String svrUrl() default "";

    String topic();

    String tag() default "";

    String isRedeliver() default "";

    String groupId() default "";

    MessagePriorityEnum priority() default MessagePriorityEnum.MEDIUM;

    SerializerTypeEnum serializer() default SerializerTypeEnum.BYTEARRAY;

    MessageModelEnum pattern() default MessageModelEnum.CLUSTERING;

    int numThreads() default 1;

    int batchSize() default 10;

    int retries() default 3;
}
