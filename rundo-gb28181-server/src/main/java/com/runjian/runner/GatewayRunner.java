package com.runjian.runner;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.GatewayProtocalEnum;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.SipConfig;
import com.runjian.conf.SsrcConfig;
import com.runjian.dao.GatewayInfoMapper;
import com.runjian.domain.dto.EdgeGatewayInfoDto;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.annotation.Order;
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

    @Override
    public void run(String... args) throws Exception {
        //初始化ssrconfig
        new SsrcConfig(null, sipConfig.getDomain());

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
