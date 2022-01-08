package com.common.msg.api.common;


public enum MessagePriorityEnum {
    UNKNOWN(-1, "unknown"),

    HIGH(1, "HIGH"),

    MEDIUM(2, "MEDIUM"),

    LOW(3, "LOW");


    private int code;


    private String memo;


    MessagePriorityEnum(int code, String memo) {
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
        for (MessagePriorityEnum type : values()) {
            if (type.code == code) {
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static MessagePriorityEnum getEnum(int code) {
        for (MessagePriorityEnum item : values()) {

            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static MessagePriorityEnum getEnum(String memo) {
        for (MessagePriorityEnum item : values()) {

            if (memo.equalsIgnoreCase(item.getMemo())) {
                return item;
            }
        }
        return UNKNOWN;
    }
}


