package com.common.msg.api.transaction;

import com.common.msg.api.MessageRecord;

public interface TransactionExecuter<T> {
    TransactionStatus execute(MessageRecord<T> paramMessageRecord, Object paramObject);
}


