package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;
import com.runjian.common.commonDto.Gateway.req.PresetControlReq;

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
     * @param presetControlReq
     */
    void presetControl(PresetControlReq presetControlReq,String msgId);
}
