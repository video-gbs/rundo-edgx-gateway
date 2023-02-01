package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * ptz/设备控制操作请求指令
 * @author chenjialing
 */
@Data
public class DeviceControlReq {
    private String deviceId;
    private String channelId;
    private int cmdCode;
    private int horizonSpeed;
    private int verticalSpeed;
    private int zoomSpeed;
    /**
     * 业务消息id
     */
    String msgId;
}
