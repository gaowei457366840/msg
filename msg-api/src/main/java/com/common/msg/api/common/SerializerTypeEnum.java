package com.common.msg.api.common;

public enum SerializerTypeEnum {
    UNKNOWN(-1, "unknown", "unknown"), JSON(1, "com.common.msg.api.serialization.JsonSerializer", "json"), PROTOBUF(2, "com.common.msg.api.serialization.ProtobufSerializer", "protobuf"), STRING(3, "com.common.msg.api.serialization.StringSerializer", "string"), BYTEARRAY(4, "com.common.msg.api.serialization.ByteArraySerializer", "bytearray");
    private int code;
    private String path;
    private String memo;

    SerializerTypeEnum(int code, String path, String memo) {
        this.code = code;
        this.path = path;
        this.memo = memo;
    }

    public int getCode() {
        return this.code;
    }

    public String getPath() {
        return this.path;
    }

    public String getMemo() {
        return this.memo;
    }

    public static String getPath(int code) {
        for (SerializerTypeEnum type : values()) {
            if (type.code == code) {
                return type.path;
            }
        }
        return UNKNOWN.getPath();
    }

    public static SerializerTypeEnum getEnum(int code) {
        for (SerializerTypeEnum item : values()) {
            if (code == item.getCode()) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static SerializerTypeEnum getEnum(String memo) {
        for (SerializerTypeEnum item : values()) {
            if (item.memo.equalsIgnoreCase(memo)) {
                return item;
            }
        }
        return UNKNOWN;
    }
}


