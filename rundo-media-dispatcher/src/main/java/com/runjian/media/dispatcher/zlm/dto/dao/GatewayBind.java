package com.runjian.media.dispatcher.zlm.dto.dao;

import lombok.Data;

import java.util.Date;

/**
 * 网关与流媒体绑定信息
 *
 * @author chenjialing
 */
@Data
public class GatewayBind {

    private int id;

    private String gatewayId;

    private String mediaServerId;

    private String mqExchange;

    private String mqRouteKey;


    private String mqQueueName;

    private Date createdAt;


    private Date updatedAt;






}
