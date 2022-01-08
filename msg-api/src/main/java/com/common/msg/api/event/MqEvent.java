package com.common.msg.api.event;

import com.common.msg.api.bootstrap.MqConfig;
import com.common.msg.api.common.SendTypeEnum;
import com.common.msg.api.transaction.TransactionExecuter;


public class MqEvent extends Event {
    private MqConfig config;
    private TransactionExecuter executer;
    private Object arg;
    private SendTypeEnum sendType;

    public void setConfig(MqConfig config) {
        this.config = config;
    }

    public void setExecuter(TransactionExecuter executer) {
        this.executer = executer;
    }

    public void setArg(Object arg) {
        this.arg = arg;
    }

    public void setSendType(SendTypeEnum sendType) {
        this.sendType = sendType;
    }

    public String toString() {
        return "MqEvent(config=" + getConfig() + ", executer=" + getExecuter() + ", arg=" + getArg() + ", sendType=" + getSendType() + ")";
    }

    public MqConfig getConfig() {

        return this.config;
    }

    public TransactionExecuter getExecuter() {
        return this.executer;
    }

    public Object getArg() {
        return this.arg;
    }

    public SendTypeEnum getSendType() {

        return this.sendType;
    }
}
