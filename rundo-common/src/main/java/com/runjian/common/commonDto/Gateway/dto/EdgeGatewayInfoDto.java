package com.runjian.common.commonDto.Gateway.dto;

import lombok.Data;

import javax.validation.constraints.Pattern;

/**
 * 网关的信息
 * @author chenjialing
 */
@Data
public class EdgeGatewayInfoDto {
    private int id;



    private String ip;

    private int port;

    /**
     * 协议类型
     */
    private String protocol;
    /**
     * 网关类型
     */
    private String gatewayType;

    /**
     * 过期时间
     */
    private String outTime;

}
