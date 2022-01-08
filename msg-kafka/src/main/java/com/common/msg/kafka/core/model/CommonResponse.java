package com.common.msg.kafka.core.model;

import com.common.msg.kafka.common.ResultCodeEnum;

import java.net.InetSocketAddress;
import java.util.Objects;


public class CommonResponse {
    private long requestId;
    private int resultCode;
    private String resultMsg;
    private InetSocketAddress node;
    private RuntimeException localException;

    public void setRequestId(long requestId) {
        this.requestId = requestId;
    }

    public void setNode(InetSocketAddress node) {
        this.node = node;
    }

    public void setLocalException(RuntimeException localException) {
        this.localException = localException;
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof CommonResponse))
            return false;

        CommonResponse other = (CommonResponse) o;
        if (!other.canEqual(this)) return false;
        if (getRequestId() != other.getRequestId())
            return false;
        if (getResultCode() != other.getResultCode()) return false;
        Object this$resultMsg = getResultMsg(), other$resultMsg = other.getResultMsg();
        if (!Objects.equals(this$resultMsg, other$resultMsg))
            return false;
        Object this$node = getNode(), other$node = other.getNode();
        if (!Objects.equals(this$node, other$node)) return false;
        Object this$localException = getLocalException(), other$localException = other.getLocalException();
        return !(!Objects.equals(this$localException, other$localException));
    }

    protected boolean canEqual(Object other) {
        return other instanceof CommonResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        long requestId = getRequestId();
        result = result * 59 + (int) (requestId >>> 32L ^ requestId);
        result = result * 59 + getResultCode();
        Object $resultMsg = getResultMsg();
        result = result * 59 + (($resultMsg == null) ? 43 : $resultMsg.hashCode());
        Object $node = getNode();
        result = result * 59 + (($node == null) ? 43 : $node.hashCode());
        Object $localException = getLocalException();
        return result * 59 + (($localException == null) ? 43 : $localException.hashCode());
    }

    public String toString() {
        return "CommonResponse(requestId=" + getRequestId() + ", resultCode=" + getResultCode() + ", resultMsg=" + getResultMsg() + ", node=" + getNode() + ", localException=" + getLocalException() + ")";
    }


    public long getRequestId() {

        return this.requestId;
    }


    public InetSocketAddress getNode() {

        return this.node;
    }

    public RuntimeException getLocalException() {

        return this.localException;
    }

    public CommonResponse() {
    }

    public CommonResponse(ResultCodeEnum resultCodeEnum, String resultMsg) {
        this.resultCode = resultCodeEnum.getCode();
        this.resultMsg = resultMsg;
    }

    public int getResultCode() {

        return this.resultCode;
    }

    public void setResultCode(int resultCode) {

        this.resultCode = resultCode;
    }

    public String getResultMsg() {

        return this.resultMsg;
    }

    public void setResultMsg(String resultMsg) {

        this.resultMsg = resultMsg;
    }
}


