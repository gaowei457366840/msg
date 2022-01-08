package com.common.msg.api.consumer;

import com.common.msg.api.bootstrap.ServiceLifecycle;

public interface MqConsumer extends ServiceLifecycle {
    void unSubscribe();
}


