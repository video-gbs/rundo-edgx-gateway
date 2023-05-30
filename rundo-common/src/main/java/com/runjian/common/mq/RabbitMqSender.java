package com.runjian.common.mq;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.config.exception.BusinessException;
import com.runjian.common.utils.ConstantUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author Miracle
 * @date 2022/5/23 18:47
 */
@Slf4j
@Component
//@ConditionalOnBean(RabbitMqConfig.class)
public class RabbitMqSender {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMqProperties rabbitMqProperties;

    /**
     *
     * @param queueId 队列名称
     * @param msgId 消息ID
     * @param msg 消息体
     * @param convertStrJson 是否转化为字符串JSON
     * @throws BusinessException
     */
    public void sendMsg(String queueId, String msgId, Object msg, boolean convertStrJson) throws BusinessException {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(msgId);
        RabbitMqProperties.QueueData queueData = rabbitMqProperties.getQueueData(queueId);
        RabbitMqProperties.ExchangeData exchangeData = rabbitMqProperties.getExchangeData(queueData.getExchangeId());
        if (convertStrJson){
            try {
                rabbitTemplate.convertAndSend(exchangeData.getName(), queueData.getRoutingKey(), ConstantUtils.OBJECT_MAPPER.writeValueAsString(msg), correlationData);
            } catch (JsonProcessingException e) {
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "MQ发送消息服务", "消息发送失败，消息格式化异常", msg, e);
            }
        }else {
            rabbitTemplate.convertAndSend(exchangeData.getName(), queueData.getRoutingKey(), msg, correlationData);
        }
    }

    public void sendMsgByRoutingKey(String exchangeId, String routingKey, String msgId, Object msg, boolean convertStrJson) throws BusinessException {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(msgId);
        RabbitMqProperties.ExchangeData exchangeData = rabbitMqProperties.getExchangeData(exchangeId);
        if (convertStrJson){
            try {
                rabbitTemplate.convertAndSend(exchangeData.getName(), routingKey, ConstantUtils.OBJECT_MAPPER.writeValueAsString(msg), correlationData);
            } catch (JsonProcessingException e) {
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "MQ发送消息服务", "消息发送失败，消息格式化异常", msg, e);
            }
        }else {
            rabbitTemplate.convertAndSend(exchangeData.getName(), routingKey, msg, correlationData);
        }
    }

    public void sendMsgByExchange(String exchangeName, String routingKey, String msgId, Object msg, boolean convertStrJson) throws BusinessException {
        CorrelationData correlationData = new CorrelationData();
        correlationData.setId(msgId);
        if (convertStrJson){
            try {
                rabbitTemplate.convertAndSend(exchangeName, routingKey, ConstantUtils.OBJECT_MAPPER.writeValueAsString(msg), correlationData);
            } catch (JsonProcessingException e) {
                log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "MQ发送消息服务", "消息发送失败，消息格式化异常", msg, e);
            }
        }else {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, msg, correlationData);
        }
    }
}
