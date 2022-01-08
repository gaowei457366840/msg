package com.common.msg.api.common;


public enum ConsumePatternEnum {
    UNKNOWN(-1, "unknown"),

    NORMAL(1, "NORMAL"),

    CONCURRENCY(2, "CONCURRENCY");


    private int code;


    private String memo;


    ConsumePatternEnum(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return this.code;
    }

    public String getMemo() {
        return this.memo;
    }

    public static String getMemo(int code) {
        for (ConsumePatternEnum type : values()) {
            if (type.code == code) {
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static ConsumePatternEnum getEnum(int code) {
        for (ConsumePatternEnum item : values()) {

            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}


