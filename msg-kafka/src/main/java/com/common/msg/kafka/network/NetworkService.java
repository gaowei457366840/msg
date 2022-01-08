package com.common.msg.kafka.network;

import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.kafka.core.model.CommonRequest;
import com.common.msg.kafka.core.model.CommonResponse;

import java.net.InetSocketAddress;
import java.util.List;

public interface NetworkService {
    void connect();

    void connect(InetSocketAddress paramInetSocketAddress, boolean paramBoolean);

    void join(List<MqConfig> paramList);

    void send(CommonRequest paramCommonRequest, String paramString, InetSocketAddress paramInetSocketAddress, long paramLong, Callback<CommonResponse, Boolean> paramCallback);

    void close();
}


