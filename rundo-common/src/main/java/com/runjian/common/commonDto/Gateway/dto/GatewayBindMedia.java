package com.runjian.common.commonDto.Gateway.dto;

import lombok.Data;

/**
 * @author chenjialing
 */
@Data
public class GatewayBindMedia {
    /**
     * 调度服务的唯一序列号
     */
    private String serialNum;
    /**
     * 调度名称--可为null
     */
    private String name;

    /**
     * 调度服务地址
     */
    private String url;

    /**
     * 调度服务地址
     */
    private String ip;

    /**
     * 调度服务地址
     */
    private int port;

    private String msgId;
}
