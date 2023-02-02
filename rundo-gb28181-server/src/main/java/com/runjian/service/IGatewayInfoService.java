package com.runjian.service;

import com.runjian.common.commonDto.Gateway.dto.GatewayBindMedia;

public interface IGatewayInfoService {

    void addMqListener(String queueName);

    /**
     *
     */
    void sendRegisterInfo();


}
