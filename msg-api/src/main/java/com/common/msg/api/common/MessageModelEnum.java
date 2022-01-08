package com.common.msg.api.common;


public enum MessageModelEnum {
    CLUSTERING(1, "CLUSTERING"),


    BROADCASTING(2, "BROADCASTING");


    private int code;


    private String memo;


    MessageModelEnum(int code, String memo) {
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
        for (MessageModelEnum type : values()) {
            if (type.code == code) {
                return type.memo;
            }
        }
        return null;
    }

    public static MessageModelEnum getEnum(int code) {
        for (MessageModelEnum item : values()) {

            if (code == item.getCode()) {
                return item;
            }
        }
        return null;
    }

    public static MessageModelEnum getEnum(String code) {
        for (MessageModelEnum item : values()) {

            if (code.equals(item.getMemo())) {
                return item;
            }
        }
        return null;
    }
}

