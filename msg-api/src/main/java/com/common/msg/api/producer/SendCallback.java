package com.common.msg.api.producer;

public interface SendCallback {
    void onSuccess(SendResult paramSendResult);

    void onException(OnExceptionContext paramOnExceptionContext);
}

