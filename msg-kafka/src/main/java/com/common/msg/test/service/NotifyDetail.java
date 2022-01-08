package com.common.msg.test.service;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class NotifyDetail implements Serializable {
    private static final long serialVersionUID = -5900066856627340752L;
    //交易类型
    /**
     * 枚举 ctrl+点击link
     * {@link com.common.zaab.life.common.enums.NotifyTypeEnum}
     */
    private Long transType;
    //订单号
    private String orderNo;
    //保单号
    private String policyNo;
    //渠道订单号
    private String orgOrderNo;
    //批单号
    private String endorNo;
    //批单订单号
    private String endorOrderNo;
    //通知次数，传0
    private Byte notifyTime;
    //保单状态
    private String policyStatus;
    //通知内容
    private String content;
    //通知header内容
    private String headerContent;
    //渠道id
    private String channelId;
    //渠道名称
    private String channelName;
    //供应商id
    private String supplierId;
    //供应商名称
    private String supplierName;
    //通知地址
    private String notifyUrl;
    //通知类型 1 http 2 https
    private String type;
    //格式 json   form
    private String formate;
    //备注
    private String remark;
    //请求类型 get post 默认post
    private String reqType;
    //摘要
    private String summary;
    //渠道返回码判断（本期先不处理）
    private String resCode;

}
