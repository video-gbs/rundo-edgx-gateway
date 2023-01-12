package com.runjian.mq.gatewayInfo;

import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.mq.RabbitMqProperties;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;

/**
 * @author chenjialing
 */
@Configuration
public class GatewayInfoMqConfig {

    @Autowired
    RabbitMqProperties rabbitMqProperties;

    @Autowired
    GatewayInfoMqListener gatewayInfoMqListener;

    //监听队列
    private String queueName = MarkConstant.SIGIN_GS;

    @Bean
    @DependsOn("createExchangeQueue")
    @Primary
    public SimpleMessageListenerContainer gatewayInfoMqListenerContainer(ConnectionFactory connectionFactory) throws BusinessException {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(rabbitMqProperties.getQueueData(queueName).getQueueName());
        container.setMessageListener(gatewayInfoMqListener);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }
}
