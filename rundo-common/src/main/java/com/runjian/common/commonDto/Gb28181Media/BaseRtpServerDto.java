package com.runjian.common.commonDto.Gb28181Media;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 国标28181相关的创建端口的参数
 * @author chenjialing
 */
@Data
public class BaseRtpServerDto {

    /**
     * 未来消息同时的mq的路由key
     */
    @NotNull(message = "路由key不得为null")
    private String mqRouteKey;

    @NotNull(message = "流id不得为null")
    private String streamId;


    @NotNull(message = "设备id不得为null")
    private String deviceId;

    @NotNull(message = "通道id不得为null")
    private String channelId;

    /**
     * ssrc的参数
     */
    private String ssrc;

    /**
     * 自定义端口
     */
    private Integer port;

    /**
     * 默认不开启音频
     */
    private Boolean enableAudio = false;

    /**
     * 默认进行ssrc校验
     */
    private Boolean ssrcCheck = true;




}
