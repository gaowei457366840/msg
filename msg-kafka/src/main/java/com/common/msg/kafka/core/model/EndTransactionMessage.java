package com.common.msg.kafka.core.model;

import com.common.msg.api.transaction.TransactionStatus;

import java.util.Objects;


public class EndTransactionMessage {
    private String msgId;
    private int status;
    private int fromCheck;
    private String remark;

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof EndTransactionMessage)) return false;
        EndTransactionMessage other = (EndTransactionMessage) o;
        if (!other.canEqual(this)) return false;
        Object this$msgId = getMsgId(), other$msgId = other.getMsgId();
        if (!Objects.equals(this$msgId, other$msgId)) return false;
        if (getStatus() != other.getStatus()) return false;
        if (getFromCheck() != other.getFromCheck()) return false;
        Object this$remark = getRemark(), other$remark = other.getRemark();
        return !(!Objects.equals(this$remark, other$remark));
    }

    protected boolean canEqual(Object other) {
        return other instanceof EndTransactionMessage;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $msgId = getMsgId();
        result = result * 59 + (($msgId == null) ? 43 : $msgId.hashCode());
        result = result * 59 + getStatus();
        result = result * 59 + (getFromCheck() ? 79 : 97);
        Object $remark = getRemark();
        return result * 59 + (($remark == null) ? 43 : $remark.hashCode());
    }

    public String toString() {
        return "EndTransactionMessage(msgId=" + getMsgId() + ", status=" + getStatus() + ", fromCheck=" + getFromCheck() + ", remark=" + getRemark() + ")";
    }

    public String getMsgId() {

        return this.msgId;
    }

    public int getStatus() {

        return this.status;
    }

    public String getRemark() {

        return this.remark;
    }

    public EndTransactionMessage() {
    }

    public EndTransactionMessage(String msgId, TransactionStatus status, Throwable localException) {
        this.msgId = msgId;
        this.status = status.getCode();
        this.remark = (null == localException) ? "" : localException.toString();
    }

    public void setFromCheck(boolean isCheck) {

        this.fromCheck = isCheck ? 1 : 0;
    }

    public boolean getFromCheck() {

        return (this.fromCheck == 1);
    }
}


