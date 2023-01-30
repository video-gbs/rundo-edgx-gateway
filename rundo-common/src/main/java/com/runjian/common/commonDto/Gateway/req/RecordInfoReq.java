package com.runjian.common.commonDto.Gateway.req;

import lombok.Data;

/**
 * 国标录像传参
 * @author chenjialing
 */
@Data
public class RecordInfoReq {
    /**
     * 设备id
     */
    String deviceId;
    /**
     * channelId
     */
    String channelId;
    /**
     * 开始时间
     */
    String startTime;
    /**
     * 结束时间
     */
    String endTime;
    /**
     * 业务消息id
     */
    String msgId;
}
