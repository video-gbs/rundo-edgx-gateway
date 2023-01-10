package com.runjian.mq.mediaPlayCallBack;

import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.mq.RabbitMqProperties;
import com.runjian.mq.gatewayInfo.GatewayInfoMqListener;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * @author chenjialing
 */
@Configuration
public class PlayCallBackMqConfig {

    @Autowired
    RabbitMqProperties rabbitMqProperties;

    @Autowired
    PlayCallBackMqListener playCallBackMqListener;

    //监听队列
    private String queueName = MarkConstant.PLAY_CALL_BACK_QUEUE;

    @Bean
    @DependsOn("createExchangeQueue")
    public SimpleMessageListenerContainer gatewayInfoMqListenerContainer(ConnectionFactory connectionFactory) throws BusinessException {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(rabbitMqProperties.getQueueData(queueName).getQueueName());
        container.setMessageListener(playCallBackMqListener);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }
}
