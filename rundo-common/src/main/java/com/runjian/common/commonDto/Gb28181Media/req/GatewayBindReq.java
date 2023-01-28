package com.runjian.common.commonDto.Gb28181Media.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 网关绑定信息
 * @author chenjialing
 */
@Data
public class GatewayBindReq {

    @NotNull(message = "网关的id")
    private String gatewayId;

    @NotNull(message = "交换机信息")
    private String mqExchange;

    @NotNull(message = "mq的路由")
    private String mqRouteKey;

    @NotNull(message = "mq的队列名称")
    private String mqQueueName;
}
