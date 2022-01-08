package com.common.msg.api.common;


public enum MqTypeEnum {
    UNKNOWN(-1, "unknown"),

    KAFKA(1, "kafka"),

    ONS(2, "ons"),

    ROCKETMQ(3, "rocketmq"),

    PULSAR(4, "pulsar");


    private int code;


    private String memo;


    MqTypeEnum(int code, String memo) {
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
        for (MqTypeEnum type : values()) {
            if (type.code == code) {
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static MqTypeEnum getEnum(int code) {
        for (MqTypeEnum item : values()) {

            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static MqTypeEnum getEnum(String code) {
        for (MqTypeEnum item : values()) {

            if (code.equals(item.getMemo())) {
                return item;
            }
        }
        return UNKNOWN;
    }
}
