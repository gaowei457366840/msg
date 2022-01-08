package com.common.msg.test.service;

import com.alibaba.fastjson.JSONObject;
import com.common.msg.api.MessageRecord;
import com.common.msg.api.common.MessagePriorityEnum;
import com.common.msg.api.common.SerializerTypeEnum;
import com.common.msg.api.annotation.Producer;
import com.common.msg.api.producer.MqProducer;
import com.common.msg.api.producer.OnExceptionContext;
import com.common.msg.api.producer.SendCallback;
import com.common.msg.api.producer.SendResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@Service
@RestController
public class ProducerService {
    @Producer(
            topic = "${kafka.topic}",
            serializer = SerializerTypeEnum.STRING,
            priority = MessagePriorityEnum.HIGH
    )
    public MqProducer highProducer;

    public ProducerService() {
        // Do nothing because of X and Y.
    }

    @RequestMapping("/high")
    public void sendHigh(NotifyDetail detail) {
//        SendResult result = this.highProducer.send(new MessageRecord("test message1"));
//        this.highProducer.sendOneway(new MessageRecord("testKey", "test message body2"));
        //String msg = JSONObject.toJSONString(detail);
        //String msg ="{\"transType\":\"4\",\"orderNo\":\"20200813172943019600042\",\"policyNo\":\"86000020201700011058\",\"orgOrderNo\":\"SX15973109821977805\",\"endorNo\":\"\",\"endorOrderNo\":\"\",\"notifyTime\":\"0\",\"policyStatus\":\"13\",\"headerContent\":\"\",\"channelId\":\"1810006\",\"channelName\":\"\",\"supplierId\":\"90001\",\"supplierName\":\"弘康人寿\",\"notifyUrl\":\"http://mgw-daily.zhongan.com/dmapi/za-arthas/v1/front/policy/zaibRenewCallback\",\"content\":\"{\\\"paidMoney\\\":\\\"168.3\\\",\\\"paidTime\\\":\\\"20200814200955\\\",\\\"payMoney\\\":\\\"168.3\\\",\\\"payStatus\\\":\\\"1\\\",\\\"payTerm\\\":\\\"2\\\",\\\"payTime\\\":\\\"20200814000000\\\",\\\"payType\\\":\\\"1\\\",\\\"policyNo\\\":\\\"86000020201700011058\\\",\\\"remark\\\":\\\"{\\\\\\\"payType\\\\\\\":\\\\\\\"1\\\\\\\",\\\\\\\"payTerm\\\\\\\":\\\\\\\"20\\\\\\\",\\\\\\\"policyTerm\\\\\\\":\\\\\\\"WL\\\\\\\"}\\\",\\\"revokeFlag\\\":\\\"N\\\",\\\"signValue\\\":\\\"e29cbf339231768537f6abf9978050327deaed3b\\\"}\",\"type\":\"https\",\"formate\":\"json\",\"remark\":\"\",\"reqType\":\"post\",\"summary\":\"\",\"resCode \":\"\"}";
        this.highProducer.sendAsync(new MessageRecord("one", JSONObject.toJSONString(detail)), new SendCallback() {
            public void onSuccess(SendResult sendResult) {
                System.out.println("Send successful. " + sendResult);
            }

            public void onException(OnExceptionContext context) {
                System.out.println("Send failed. message:" + context.getMessageRecord() + ", error:" + context.getException());
            }
        });
    }

}

