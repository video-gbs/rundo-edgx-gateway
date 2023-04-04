package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * ptz/设备控制操作请求指令
 * @author chenjialing
 */
@Data
public class ChannelPtzControlReq {
    private String deviceId;
    private String channelId;
    private int operationValue;
    private String ptzOperationType;

    /**
     * 业务消息id
     */
    String msgId;
}
