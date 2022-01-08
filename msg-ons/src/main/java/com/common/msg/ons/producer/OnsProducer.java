package com.common.msg.ons.producer;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendCallback;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.transaction.LocalTransactionExecuter;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import com.common.msg.api.bootstrap.ServiceLifecycle;
import java.util.concurrent.ExecutorService;

public interface OnsProducer extends ServiceLifecycle {
  SendResult send(Message paramMessage);
  
  void sendOneway(Message paramMessage);
  
  void sendAsync(Message paramMessage, SendCallback paramSendCallback);
  
  SendResult send(Message paramMessage, LocalTransactionExecuter paramLocalTransactionExecuter, Object paramObject) throws MQClientException;
  
  void setCallbackExecutor(ExecutorService paramExecutorService);
}

