package com.common.msg.api.event;

import com.common.msg.api.exception.MqClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EventAdviceServiceImpl
        implements EventAdviceService {
    private static final Logger log = LoggerFactory.getLogger(EventAdviceServiceImpl.class);


    public Event before(Event event) throws Exception {
        return null;
    }


    public void after(Event event) {
    }


    public void fail(Event event, Throwable throwable) {

        throw new MqClientException(throwable);
    }
}

