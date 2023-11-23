package com.runjian.common.commonDto.Gb28181Media.req;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 点播传参
 * @author chenjialing
 */
@Data
public class MediaPlayReq {

    /**
     * 流传输模式
     */
    Integer streamMode;
    /**
     * 设备id
     */
    String deviceId;
    /**
     * channelId
     */
    String channelId;
    /**
     * 是否开启音频
     */
    Boolean enableAudio =true;
    /**
     * 是否ssrc校验
     */
    Boolean ssrcCheck = true;

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
     * 子码流
     */
    Integer bitStreamId;
    /**
     * 协议类型
     */
    String gatewayProtocol;
    /**
     *
     */
    /**
     * 业务消息id
     */
    String msgId;
}
