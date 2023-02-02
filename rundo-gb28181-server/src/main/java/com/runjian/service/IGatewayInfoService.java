package com.runjian.service;

import com.runjian.common.commonDto.Gateway.dto.GatewayBindMedia;

public interface IGatewayInfoService {

    void addMqListener(String queueName);

    /**
     *
     */
    void sendRegisterInfo();

    /**
     * 网关绑定调度服务
     * @param gatewayBindMedia
     */
    void gatewayBindMedia(GatewayBindMedia gatewayBindMedia);
}
