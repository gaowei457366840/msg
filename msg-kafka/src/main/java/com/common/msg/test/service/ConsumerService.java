package com.common.msg.test.service;

import com.common.msg.api.annotation.Consumer;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.consumer.AckAction;
import com.common.msg.api.consumer.ReceiveRecord;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ConsumerService {

    private static Random random = new Random();

    // 参考API开发中的消费
    @Consumer(topic = "${kafka.topic}", priority = MessagePriorityEnum.HIGH, serializer = SerializerTypeEnum.STRING, isRedeliver = "isRedeliver")
    public AckAction consumerHigh(ReceiveRecord<String> message) throws Exception {
        System.out.println("ConsumerService.consumerHigh message:" + message);
        return AckAction.Commit;
    }

    //判断消息是否重发，重发消息会跳过消费逻辑自动Commit，true: 重复消息 false: 非重复消息
    public boolean isRedeliver(ReceiveRecord<String> message) {
        boolean redeliver = false;
        if (random.nextInt(100) == 50) {
            redeliver = true;
        }
        System.out.println("ConsumerService.isRedeliver " + redeliver + ", message:" + message);

        return redeliver;
    }

}