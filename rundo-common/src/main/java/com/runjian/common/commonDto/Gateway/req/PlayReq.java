package com.runjian.common.commonDto.Gateway.req;

import com.runjian.common.commonDto.SsrcInfo;
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
     * 调度服务地址
     */
    private String dispatchUrl;

    /**
     * 流id
     */
    private String streamId;


    private SsrcInfo ssrcInfo;
    /**
     * 业务消息id
     */
    String msgId;
}
