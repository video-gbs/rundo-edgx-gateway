package com.runjian.service;

import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;

/**
 * ptz处理服务
 * @author chenjialing
 */
public interface IPtzService {


    void deviceControl(DeviceControlReq deviceControlReq);
}
