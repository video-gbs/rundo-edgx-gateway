package com.runjian.domain.resp;

import lombok.Data;

/**
 * 网关信息注册返回
 * @author chenjialing
 */
@Data
public class GatewaySignInRsp {

    /**
     * 是否第一次注册
     */
    private Boolean isFirstSignIn;

    /**
     * 网关id
     */
    private Long gatewayId;

    /**
     * 注册类型 MQ RESTFUL
     */
    private String signType;

    /**
     * 交换器
     */
    private String mqExchange;

    /**
     * 发送消息 --消息解析引擎发送队列
     */
    private String mqSetQueue;

    /**
     * 监听消息 --消息解析引擎接收队列
     */
    private String mqGetQueue;
}
