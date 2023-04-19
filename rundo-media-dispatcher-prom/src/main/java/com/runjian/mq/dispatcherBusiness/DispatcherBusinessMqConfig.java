package com.runjian.mq.dispatcherBusiness;

import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.mq.RabbitMqProperties;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/** todo 暂时将调度服务的mq消息与业务队列共用
 * @author chenjialing
 */
@Configuration
public class DispatcherBusinessMqConfig {

    @Autowired
    RabbitMqProperties rabbitMqProperties;

    @Autowired
    DispatcherBusinessMqListener gatewayBusinessMqListener;



    @Bean
    @DependsOn("createExchangeQueue")
    public SimpleMessageListenerContainer dispatcherBusinessMqListenerContainer(ConnectionFactory connectionFactory) throws BusinessException {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(gatewayBusinessMqListener);
        container.setConcurrentConsumers(8);
        container.setMaxConcurrentConsumers(32);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }
}
