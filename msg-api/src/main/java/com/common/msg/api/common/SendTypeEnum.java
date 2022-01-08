package com.common.msg.api.common;


public enum SendTypeEnum {
    UNKNOWN(-1, "unknown"),

    //同步
    SYCN(1, "sycn"),

    //异步
    ASYCN(2, "Async"),

    ONEWAY(3, "oneway");


    private int code;


    private String memo;


    SendTypeEnum(int code, String memo) {
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
        for (SendTypeEnum type : values()) {
            if (type.code == code) {
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static SendTypeEnum getEnum(int code) {
        for (SendTypeEnum item : values()) {

            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static SendTypeEnum getEnum(String code) {
        for (SendTypeEnum item : values()) {

            if (code.equals(item.getMemo())) {
                return item;
            }
        }
        return UNKNOWN;
    }
}


