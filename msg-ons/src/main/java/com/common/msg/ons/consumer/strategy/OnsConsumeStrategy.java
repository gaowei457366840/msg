package com.common.msg.ons.consumer.strategy;

import com.aliyun.openservices.ons.api.Message;
import com.common.msg.api.consumer.binding.ConsumerConfig;
import com.common.msg.ons.consumer.OnsAction;

public interface OnsConsumeStrategy {
  OnsAction onMessage(ConsumerConfig paramConsumerConfig, Message paramMessage, OnsConsumeContext paramOnsConsumeContext);
}


/* Location:              D:\worksofeware\mvn\com\zhongan\za-msg-ons\2.0.0-rc1-fix\za-msg-ons-2.0.0-rc1-fix.jar!\com\zhongan\msg\ons\consumer\strategy\OnsConsumeStrategy.class
 * Java compiler version: 7 (51.0)
 * JD-Core Version:       1.1.3
 */