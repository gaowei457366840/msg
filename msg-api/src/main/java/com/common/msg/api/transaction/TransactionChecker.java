package com.common.msg.api.transaction;

import com.common.msg.api.consumer.ReceiveRecord;

public interface TransactionChecker<T> {
    TransactionStatus check(ReceiveRecord<T> paramReceiveRecord);
}

