package com.runjian.media.dispatcher.conf.mq;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * 网关注册返回(队列均已创建好,仅用收发)
 * @author chenjialing
 */
@Component
@Data
public class DispatcherSignInConf {
    /**
     * 是否第一次注册
     */
    private Boolean isFirstSignIn;

    /**
     * 网关序列号
     */
    private String serialNum;;

    /**
     * 注册类型 MQ RESTFUL
     */
    private String signType;

    /**
     * 交换器
     */
    private String mqExchange;

    /**
     * 发送消息 --网关监听队列
     */
    private String mqSetQueue;

    /**
     * 监听消息 --网关发送队列
     */
    private String mqGetQueue;
}
