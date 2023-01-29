package com.runjian.service.impl;

import com.runjian.mq.gatewayBusiness.GatewayBusinessMqListener;
import com.runjian.service.IGatewayInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class GatewayInfoServiceImpl implements IGatewayInfoService {

    @Autowired
    @Qualifier("gatewayBusinessMqListenerContainer")
    SimpleMessageListenerContainer container;

    @Autowired
    GatewayBusinessMqListener gatewayBusinessMqListener;

    /**
     * 动态监听mq的队列
     * @param queueName
     */
    @Override
    public void addMqListener(String queueName) {
        String[] strings = container.getQueueNames();
        List<String> list= Arrays.asList(strings);
        if (!list.contains(queueName)) {
            container.addQueueNames(queueName);
            container.setMessageListener(gatewayBusinessMqListener);
            container.setConcurrentConsumers(1);
            container.setMaxConcurrentConsumers(1);
            container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        }
    }
}
