package com.common.msg.kafka.network;

public interface Callback<P, R> {
    R call(P paramP);
}


