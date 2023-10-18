package com.runjian.common.commonDto.Gb28181Media.req;

import lombok.Data;

/**
 * 点播传参
 * @author chenjialing
 */
@Data
public class WebRTCTalkReq {

    /**
     * 设备id
     */
    String deviceId;
    /**
     * channelId
     */
    String channelId;


    /**
     * 调度服务地址
     */
    private String dispatchUrl;

    /**
     * 流id
     */
    private String streamId;

    /**
     * 录像状态
     */
    private Integer recordState;

    /**
     * 交换机
     */
    private String gatewayMqExchange;

    /**
     * 路由
     */
    private String gatewayMqRouteKey;
    /**
     *
     */
    /**
     * 业务消息id
     */
    String msgId;
}
