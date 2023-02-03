package com.runjian.test;

import com.runjian.common.mq.RabbitMqConfig;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.mq.gatewayBusiness.GatewayBusinessMqListener;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@CrossOrigin
@RestController
@RequestMapping("/api/test")
@Tag(name = "mq测试")
public class mqController {

    @Autowired
    public RabbitAdmin rabbitAdmin;
    public static String exchangeName="public_test";

    @Autowired
    private RabbitMqConfig rabbitMqConfig;

    @RequestMapping("produce_queue")
    @Operation(summary="生产队列")
    public void product(@RequestParam String queueName) {
        createMQIfNotExist(queueName, exchangeName);
    }


    @Autowired
    SimpleMessageListenerContainer container;

    @Autowired
    GatewayBusinessMqListener message;

    @Autowired
    GatewaySignInConf gatewaySignInConf;
    //动态创建监听
    @Operation(summary="动态监听队列")
    @RequestMapping("addListener")
    public void addListener(@RequestParam String queueName) {

        String[] strings = container.getQueueNames();
        List<String> list= Arrays.asList(strings);
        if (!list.contains(queueName)) {
            container.addQueueNames(queueName);
            container.setMessageListener(message);
        }

    }

    //动态创建队列
    public void createMQIfNotExist(String queueName,String exchangeName) {
        Properties properties=rabbitAdmin.getQueueProperties(queueName);
        if (properties==null) {
            Queue queue = new Queue(queueName, true, false, false);
            AbstractExchange exchange = new TopicExchange(exchangeName);
            rabbitAdmin.declareExchange(exchange);
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(queueName).noargs();
            rabbitMqConfig.addQueue(queue, binding);

        }
    }


}
