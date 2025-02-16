package com.runjian.common.commonDto.Gb28181Media;

import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 国标28181相关的创建端口的参数
 * @author chenjialing
 */
@Data
public class BaseRtpServerDto {

    /**
     * 网关的id
     */

    @NotNull(message = "流id不得为null")
    private String streamId;


    @NotNull(message = "设备id不得为null")
    private String deviceId;

    @NotNull(message = "通道id不得为null")
    private String channelId;

    /**
     * 流传输模式
     */
    String streamMode;
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
    /**
     * 录像状态
     */
    private Integer recordState;

    /**
     * 流媒体id
     */
    private String mediaServerId;

    /**
     * 业务队列信息
     */
    private GatewayBindReq gatewayBindReq;


}
