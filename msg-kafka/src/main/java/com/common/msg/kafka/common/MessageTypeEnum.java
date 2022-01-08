package com.common.msg.kafka.common;

import com.common.msg.kafka.core.model.BrokerCommandRequest;
import com.common.msg.kafka.core.model.BrokerMetadataRequest;
import com.common.msg.kafka.core.model.BrokerMetadataResponse;
import com.common.msg.kafka.core.model.CheckTransactionStatusRequest;
import com.common.msg.kafka.core.model.CommonResponse;
import com.common.msg.kafka.core.model.EndTransactionRequest;
import com.common.msg.kafka.core.model.HandshakeRequest;
import com.common.msg.kafka.core.model.HandshakeResponse;
import com.common.msg.kafka.core.model.InnerJoinGroupRequest;
import com.common.msg.kafka.core.model.JoinGroupRequest;
import com.common.msg.kafka.core.model.JoinGroupResponse;
import com.common.msg.kafka.core.model.SendMessageRequest;
import com.common.msg.kafka.core.model.SendMessageResponse;
import com.common.msg.kafka.core.model.TopicGroupReportRequest;
import com.common.msg.kafka.core.model.TopicGroupReportResponse;


public enum MessageTypeEnum {
    HEARTBEAT_REQ("00", String.class, "01", "heartbeat request"),


    HEARTBEAT_RESP("01", String.class, null, "heartbeat response"),


    HANDSHAKE_REQ("02", HandshakeRequest.class, "03", "handshake request"),


    HANDSHAKE_RESP("03", HandshakeResponse.class, null, "handshake response"),


    BROKER_METADATA_REQ("04", BrokerMetadataRequest.class, "05", "broker metadata request"),


    BROKER_METADATA_RESP("05", BrokerMetadataResponse.class, null, "broker metadata response"),


    JOIN_GROUP_REQ("06", JoinGroupRequest.class, "07", "join group request"),


    JOIN_GROUP_RESP("07", JoinGroupResponse.class, null, "join group response"),


    SEND_MESSAGE_REQ("08", SendMessageRequest.class, "09", "send message request"),


    SEND_MESSAGE_RESP("09", SendMessageResponse.class, null, "send message response"),


    END_TRANSACTION_REQ("10", EndTransactionRequest.class, null, "end transaction request"),


    CHECK_TRANSACTION_STATUS_REQ("11", CheckTransactionStatusRequest.class, null, "check transaction status request"),


    BROKER_COMMAND_REQUEST("12", BrokerCommandRequest.class, null, "broker command request"),


    TOPIC_GROUP_REPORT_REQ("13", TopicGroupReportRequest.class, "14", "topic group report request"),


    TOPIC_GROUP_REPORT_RESP("14", TopicGroupReportResponse.class, null, "topic group report response"),


    INNER_JOIN_GROUP_REQ("30", InnerJoinGroupRequest.class, null, "inner join group request"),


    COMMON_RESP("33", CommonResponse.class, null, "common response"),


    UNKNOWN("99", null, "99", "unknown");

    private String code;
    private Class clz;
    private String nextCode;
    private String memo;

    MessageTypeEnum(String code, Class clz, String nextCode, String memo) {
        this.code = code;
        this.clz = clz;
        this.nextCode = nextCode;
        this.memo = memo;
    }

    public String getCode() {
        return this.code;
    }

    public <T> Class<T> getClz() {
        return this.clz;
    }

    public String getNextCode() {
        return this.nextCode;
    }

    public String getMemo() {
        return this.memo;
    }

    public static MessageTypeEnum getEnum(String code) {
        for (MessageTypeEnum item : values()) {

            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return UNKNOWN;
    }
}


