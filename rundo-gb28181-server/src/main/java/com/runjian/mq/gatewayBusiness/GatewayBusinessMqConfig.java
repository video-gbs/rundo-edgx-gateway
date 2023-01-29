package com.runjian.mq.gatewayBusiness;

import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.mq.RabbitMqProperties;
import com.runjian.mq.gatewayBusiness.GatewayBusinessMqListener;
import com.runjian.mq.gatewayInfo.GatewayInfoMqListener;
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
public class GatewayBusinessMqConfig {

    @Autowired
    RabbitMqProperties rabbitMqProperties;

    @Autowired
    GatewayBusinessMqListener gatewayBusinessMqListener;

    //监听队列
    private String queueName = MarkConstant.PLAY_CALL_BACK_QUEUE;

    @Bean
    @DependsOn("createExchangeQueue")
    public SimpleMessageListenerContainer gatewayBusinessMqListenerContainer(ConnectionFactory connectionFactory) throws BusinessException {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setMessageListener(gatewayBusinessMqListener);
        container.setConcurrentConsumers(1);
        container.setMaxConcurrentConsumers(1);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return container;
    }
}
