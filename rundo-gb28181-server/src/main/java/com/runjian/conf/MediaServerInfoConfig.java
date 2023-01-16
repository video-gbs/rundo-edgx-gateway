package com.runjian.conf;

import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * 获取的调度服务的信息
 * @author chenjialing
 */
@Configuration
@Data
public class MediaServerInfoConfig {

    private String mediaUrl;

    private String mqRoutingKey;

    private String mqQueueName;

    private String sdpIp;



}
