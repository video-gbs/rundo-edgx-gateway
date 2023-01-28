package com.runjian.runner;

import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.SipConfig;
import com.runjian.dao.GatewayInfoMapper;
import com.runjian.domain.dto.EdgeGatewayInfoDto;
import com.runjian.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
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
    private RedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {
        //初始化ssrconfig  并使用redis缓存进行统一管理 防止未来集群化调用同一个流媒体使用ssrc推流冲突
        iRedisCatchStorageService.ssrcInit();

        //获取配置并装配
        String ip = sipConfig.getIp();
        int port = Integer.parseInt(serverPort);

        EdgeGatewayInfoDto config = new EdgeGatewayInfoDto();
        config.setPort(port);
        config.setIp(ip);
        config.setGatewayId(serialNum);
        config.setGatewayType(0);
        config.setProtocal(GatewayProtocalEnum.GB28181.getTypeName());
        gatewayInfoConf.setEdgeGatewayInfoDto(config);

        //进行mq消息发送
        String sn = iRedisCatchStorageService.getSn(GatewayCacheConstants.GATEWAY_INFO_SN_INCR);
        GatewayMqDto dataRes = new GatewayMqDto();

        dataRes.setMsgId(GatewayCacheConstants.GATEWAY_INFO_SN_prefix+sn);
        dataRes.setSerialNum(serialNum);
        dataRes.setMsgType(GatewayMsgType.GATEWAY_SIGN_IN.getTypeName());
        dataRes.setData(config);
        dataRes.setTime(LocalDateTime.now());
        //消息组装
        rabbitMqSender.sendMsg(MarkConstant.SIGIN_SG,  ""+Instant.now().toEpochMilli() + ConstantUtils.RANDOM_UTIL.nextInt(100), dataRes, true);
    }
}
