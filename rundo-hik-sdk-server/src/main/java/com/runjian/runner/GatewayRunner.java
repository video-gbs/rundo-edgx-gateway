package com.runjian.runner;

import com.runjian.common.mq.RabbitMqSender;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.service.IDeviceService;
import com.runjian.service.IGatewayInfoService;
import com.runjian.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 服务启动,生成网关配置信息
 * @author chenjialing
 */
@Component
@Order(value = 0)
public class GatewayRunner implements CommandLineRunner {


    @Autowired
    private GatewayInfoConf gatewayInfoConf;


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

    @Autowired
    private IDeviceService deviceService;

    @Override
    public void run(String... args) throws Exception {

        //发送注册
        gatewayInfoService.sendRegisterInfo();
        //设备全部重新注册
        deviceService.startOnline();
    }
}
