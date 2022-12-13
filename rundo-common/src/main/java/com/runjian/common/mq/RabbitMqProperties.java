package com.runjian.common.mq;


import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.exception.BusinessException;
import lombok.Data;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Miracle
 * @date 2022/5/23 16:36
 */
@Data
@ConfigurationProperties(prefix = "mq")
public class RabbitMqProperties {

    /**
     * 队列信息
     */
    private final List<QueueData> queueDataList;

    /**
     * 交换器信息
     */
    private final List<ExchangeData> exchangeDataList;

    /**
     * 以VideoType为key的队列消息
     */
    private Map<String, QueueData> queueDataMap;

    /**
     * 以ExchangeType为key的交换器消息
     */
    private Map<String, ExchangeData> exchangeDataMap;

    /**
     * 获取消息队列
     * @param queueId
     * @return
     * @throws BusinessException
     */
    public QueueData getQueueData(String queueId) throws BusinessException {
        QueueData queueData = queueDataMap.get(queueId);
        if (Objects.isNull(queueData)){
            throw new BusinessException(BusinessErrorEnums.MQ_QUEUE_IS_NOT_FOUND, "queueId:" + queueId);
        }
        return queueData;
    }

    /**
     * 获取交换器
     * @param exchangeId
     * @return
     * @throws BusinessException
     */
    public ExchangeData getExchangeData(String exchangeId) throws BusinessException {
        ExchangeData exchangeData = exchangeDataMap.get(exchangeId);
        if (Objects.isNull(exchangeData)){
            throw new BusinessException(BusinessErrorEnums.MQ_EXCHANGE_IS_NOT_FOUND, "exchangeId:" + exchangeId);
        }
        return exchangeData;
    }

    /**
     * 通过queueId获取交换器
     * @param queueId
     * @return
     * @throws BusinessException
     */
    public ExchangeData getExchangeDataByQueueId(String queueId) throws BusinessException {
        QueueData queueData = getQueueData(queueId);
        return getExchangeData(queueData.getExchangeId());
    }

    /**
     * 队列信息
     */
    @Data
    public static class QueueData{

        /**
         * 应用区域
         */
        private String id;

        /**
         * 路由key
         */
        private String routingKey;

        /**
         * 队列名称
         */
        private String queueName;

        /**
         * 交换器名称
         */
        private String exchangeId;
    }

    @Data
    public static class ExchangeData{

        /**
         * id
         */
        private String id;

        /**
         * 交换器名字
         */
        private String name;

        /**
         * 交换器类型
         */
        private String type;

        /**
         * 交换器类型ENUM
         */
        private AbstractExchange exchange;
    }



}
