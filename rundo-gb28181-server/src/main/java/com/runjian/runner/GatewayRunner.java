package com.runjian.runner;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.GatewayProtocalEnum;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.BaseDto;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.SipConfig;
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

    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;

    @Override
    public void run(String... args) throws Exception {
        //获取配置并装配
        EdgeGatewayInfoDto config = gatewayInfoMapper.getConfig();
        String ip = sipConfig.getIp();
        int port = Integer.parseInt(serverPort);

        if(config != null){
            //更新ip和端口
            config.setIp(ip);
            config.setPort(port);
            gatewayInfoMapper.update(config);
        }else {
            config = new EdgeGatewayInfoDto();
            config.setPort(port);
            config.setIp(ip);
            config.setGatewayId(UuidUtil.toUuid());
            config.setGatewayType(0);
            config.setProtocal(GatewayProtocalEnum.GB28181.getTypeName());
            gatewayInfoMapper.add(config);
        }
        gatewayInfoConf.setEdgeGatewayInfoDto(config);

        //进行mq消息发送
        //消息组装
        CommonResponse<EdgeGatewayInfoDto> success = CommonResponse.success(config);


        String sn = iRedisCatchStorageService.getSn(GatewayCacheConstants.GATEWAY_INFO_SN_INCR);
        BaseDto baseDto = new BaseDto();
        baseDto.setGatewayId(config.getGatewayId());
        baseDto.setMsgType(GatewayMsgType.GATEWAY_REGISTER.getTypeName());
        baseDto.setMqSn(sn);
        baseDto.setData(success);
        rabbitMqSender.sendMsg("SIGIN-SG",  ""+Instant.now().toEpochMilli() + ConstantUtils.RANDOM_UTIL.nextInt(100), config, true);
    }
}
