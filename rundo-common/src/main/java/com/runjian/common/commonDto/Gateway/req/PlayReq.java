package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * 点播传参
 * @author chenjialing
 */
@Data
public class PlayReq {

    /**
     * 流传输模式
     */
    String streamMode;
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
    Boolean enableAudio =false;
    /**
     * 是否ssrc校验
     */
    Boolean ssrcCheck = true;
    /**
     * 业务消息id
     */
    String msgId;
}
