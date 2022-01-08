package com.common.msg.api.event;


public enum EventStatusEnum {
    UNKNOWN(9, "unknown"),

    WAIT(0, "等待处理"),

    PROCESSING(1, "处理中"),

    SUCCESS(2, "处理成功"),


    MANUAL_WAIT(3, "等待人工处理"),

    MANUAL_SUCCESS(4, "人工处理完成"),

    MANUAL_IGNORE(5, "人工忽略");

    private int code;

    private String memo;

    EventStatusEnum(int code, String memo) {
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
        for (EventStatusEnum type : values()) {
            if (type.code == code) {
                return type.memo;
            }
        }
        return UNKNOWN.getMemo();
    }

    public static EventStatusEnum getEnum(int code) {
        for (EventStatusEnum item : values()) {

            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }
}


