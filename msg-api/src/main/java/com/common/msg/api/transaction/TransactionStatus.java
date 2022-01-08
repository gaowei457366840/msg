package com.common.msg.api.transaction;


public enum TransactionStatus {
    Unknown(0, "Unknown"),


    Commit(1, "Commit"),


    Rollback(2, "Rollback");


    private int code;


    private String memo;


    TransactionStatus(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return this.code;
    }

    public String getMemo() {
        return this.memo;
    }
}

