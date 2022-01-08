package com.common.msg.api.common;


public enum RetryTimeEnum {
    ONE(1, 1000L),


    TWO(2, 60000L),


    THREE(3, 600000L),


    FOUR(4, 3600000L),


    FIVE(5, 21600000L),


    SIX(6, 43200000L),


    SEVEN(7, 86400000L),


    OTHER(-1, 3600000L);


    private int code;


    private long memo;


    RetryTimeEnum(int code, long memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return this.code;
    }

    public long getMeme() {
        return this.memo;
    }

    public static long getMemo(int code) {
        if (code <= 7) {
            for (RetryTimeEnum type : values()) {
                if (type.code == code) {
                    return type.memo;
                }
            }
        }
        return OTHER.memo;
    }

    public static RetryTimeEnum getEnum(int code) {
        if (code <= 7) {
            for (RetryTimeEnum item : values()) {

                if (code == item.getCode()) {
                    return item;
                }
            }
        }
        return OTHER;
    }
}


