package com.runjian.common.mq.domain;

import lombok.Data;

/**
 * 网关传输消息体
 */
@Data
public class GatewayMqDto {

    /**
     * 网关序列号
     */
    private String serialNum;

    /**
     * 消息类型
     */
    private String msgType;

    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 数据体
     */
    private Object data;
}

