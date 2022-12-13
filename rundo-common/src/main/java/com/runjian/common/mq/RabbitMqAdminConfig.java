package com.runjian.common.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Miracle
 * @date 2022/5/23 18:21
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(RabbitMqProperties.class)
@ConditionalOnProperty(prefix = "mq", value = "enabled", havingValue = "true")
public class RabbitMqAdminConfig {

    /**
     * 配置rabbitAdmin
     * @param connectionFactory
     * @return
     */
    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        //只有设置为 true, spring才会加载RabbitAdmin这个类
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }



}
