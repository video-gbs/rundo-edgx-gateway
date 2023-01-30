package com.runjian.runner;

import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.SipConfig;
import com.runjian.dao.GatewayInfoMapper;
import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import com.runjian.service.IGatewayInfoService;
import com.runjian.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 服务启动,生成网关配置信息
 * @author chenjialing
 */
@Component
@Order(value = 0)
public class GatewayRunner implements CommandLineRunner {

    @Autowired
    SipConfig sipConfig;

    @Autowired
    private GatewayInfoConf gatewayInfoConf;

    @Autowired
    GatewayInfoMapper gatewayInfoMapper;

    @Value("${server.port}")
    private String serverPort;

    @Value("${gateway-info.serialNum}")
    private String serialNum;

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;

    @Autowired
    private IGatewayInfoService gatewayInfoService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        //初始化ssrconfig  并使用redis缓存进行统一管理 防止未来集群化调用同一个流媒体使用ssrc推流冲突
        iRedisCatchStorageService.ssrcInit();

        //发送注册
        gatewayInfoService.sendRegisterInfo();
    }
}
