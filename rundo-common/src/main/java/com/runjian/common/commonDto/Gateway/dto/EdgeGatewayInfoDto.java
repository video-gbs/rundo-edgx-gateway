package com.runjian.common.commonDto.Gateway.dto;

import lombok.Data;

/**
 * 网关的信息
 * @author chenjialing
 */
@Data
public class EdgeGatewayInfoDto {
    private int id;

    /**
     * 网关的id
     */
    private String gatewayId;

    private String ip;

    private int port;

    /**
     * 协议类型
     */
    private String protocal;
    /**
     * 网关类型
     */
    private int gatewayType;

}
