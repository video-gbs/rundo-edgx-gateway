package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

import java.util.Map;

/**
 * ptz/设备控制操作请求指令
 * @author chenjialing
 */
@Data
public class ChannelPtzControlReq {
    private String deviceId;
    private String channelId;
    //操作的数据，如速度，预置位id
    private Map<String,Object> operationMap;
    //操作的类型
    private int ptzOperationType;

    /**
     * 业务消息id
     */
    String msgId;
}
