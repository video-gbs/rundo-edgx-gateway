package com.runjian.common.mq.domain;

/**
 * mq发送的基础数据格式
 * @author chenjialing
 */
public class BaseDto {

    /**
     * 网关id
     */
    private String gatewayId;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * mq消息的sn,唯一性，用作消息追踪
     */
    private String mqSn;
}
