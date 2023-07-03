package com.runjian.service;

public interface IGatewayInfoService {

    void addMqListener(String queueName);

    /**
     *
     */
    void sendRegisterInfo();


}
