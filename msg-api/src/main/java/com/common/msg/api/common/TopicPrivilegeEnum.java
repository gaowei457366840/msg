package com.common.msg.api.common;


public enum TopicPrivilegeEnum {
    DENY(0, "deny"),

    ANY(1, "any"),

    PUB(2, "publish"),

    SUB(3, "subscribe");

    private int code;

    private String memo;

    TopicPrivilegeEnum(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {

        return this.code;
    }

    public String getMemo() {

        return this.memo;
    }

    public static TopicPrivilegeEnum getEnum(int resultCode) {
        for (TopicPrivilegeEnum codeEnum : values()) {
            if (codeEnum.code == resultCode) {
                return codeEnum;
            }
        }

        return DENY;
    }
}


