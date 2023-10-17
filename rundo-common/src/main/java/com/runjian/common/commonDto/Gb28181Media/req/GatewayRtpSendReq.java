package com.runjian.common.commonDto.Gb28181Media.req;

import com.runjian.common.config.response.GatewayBusinessSceneResp;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotNull;

/**
 * @author chenjialing
 */
@Data
public class GatewayRtpSendReq {

    @NotNull(message = "设备id不得为null")
    String deviceId;

    @NotNull(message = "通道id不得为null")
    String channelId;

    @Range(min = 0,max = 1,message = "媒体信息为0，1范围")
    Integer mediaType;

    @Range(min = 0,max = 2,message = "媒体信息为0，2范围")
    Integer streamMode;

    Integer onlyAudio=1;

    String dstUrl;

    String dstPort;

    /**
     * 来自请求方的ssrc
     */
    String ssrc;



}
