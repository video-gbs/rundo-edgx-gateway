package com.runjian.runner;

import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.hik.module.service.SdkInitService;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
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
@Order(value = 1)
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

    @Autowired
    IMqMsgDealServer mqMsgDealServer;

    @Autowired
    private SdkInitService hksdkInitService;

    @Override
    public void run(String... args) throws Exception {
//        hksdkInitService.initSdk();
        //发送注册
        gatewayInfoService.sendRegisterInfo();
        //设备全部重新注册
        deviceService.startOnline();

        //进行全量的设备数据数据同步，避免出现设备数据差异
        CommonMqDto commonMqDto = new CommonMqDto();
        commonMqDto.setMsgType(GatewayMsgType.DEVICE_TOTAL_SYNC.getTypeName());
        mqMsgDealServer.msgProcess(commonMqDto);
    }
}
