package com.common.msg.kafka.common;


public enum ResultCodeEnum {
    UNKNOWN(9999, "Unknown"),

    SUCCESS(1000, "Success"),

    ARGS_INVALID(2001, "Args invalid."),

    VERSION_INVALID(2002, "Server api [%s] version [%s] is not equals client version [%s]."),

    NOT_SUPPORT(2003, "Not support this api."),

    NOT_AVAILABLE_CLIENT(2004, "Not have available client for this sarName:[%s] jobName:[%s]."),

    DESERIALIZER_FAILED(2005, "Deserializer failed request name:[%s] requestId:[%s]."),

    TIMEOUT(2006, "Send data failed in timeout [%d]ms."),

    DISCONNECTION(2007, "Send data failed in disconnection from the server [%s]."),

    NO_AVAILABLE_CONN(2008, "Not found available connection."),

    SERVER_ERROR(3001, "Server error [%s]"),

    SERVER_BUSY(3003, "Server busy."),

    SERVER_NOT_READY(3004, "Server is not ready."),

    REQUEST_TOO_MANY(3005, "server limit request.");

    private int code;

    private String memo;


    ResultCodeEnum(int code, String memo) {
        this.code = code;
        this.memo = memo;
    }

    public int getCode() {
        return this.code;
    }

    public String getMemo() {
        return this.memo;
    }

    public static String getResultMsg(int resultCode) {
        for (ResultCodeEnum codeEnum : values()) {
            if (codeEnum.code == resultCode) {
                return codeEnum.getMemo();
            }
        }

        return UNKNOWN.getMemo();
    }

    public static ResultCodeEnum getResultCodeEnum(int resultCode) {
        for (ResultCodeEnum codeEnum : values()) {
            if (codeEnum.code == resultCode) {
                return codeEnum;
            }
        }

        return UNKNOWN;
    }
}


