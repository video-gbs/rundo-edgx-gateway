package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;

/**
 * ptz处理服务
 * @author chenjialing
 */
public interface IPtzService {

    /**
     * 云台控制
     * @param deviceControlReq
     */
    void deviceControl(DeviceControlReq deviceControlReq);

    /**
     * 预置位控制
     * @param channelPtzControlReq
     */
    void ptzControl(ChannelPtzControlReq channelPtzControlReq);
}
