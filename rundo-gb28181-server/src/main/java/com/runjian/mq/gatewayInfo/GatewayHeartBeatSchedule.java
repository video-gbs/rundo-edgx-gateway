package com.runjian.mq.gatewayInfo;

import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.MarkConstant;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.ConstantUtils;
import com.runjian.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * 定时发送心跳
 * @author chenjialing
 */
public class GatewayHeartBeatSchedule {

    @Value("${gateway-info.serialNum}")
    private String serialNum;
    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;
    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Scheduled(cron="0 0/1 * * * ?")   //每1分钟执行一次
    public void sendMsg(){
        GatewayMqDto gatewayMqDto = new GatewayMqDto();
        gatewayMqDto.setMsgType(GatewayMsgType.HEARTBEAT.getTypeName());
        gatewayMqDto.setTime(LocalDateTime.now());
        gatewayMqDto.setSerialNum(serialNum);

        String sn = iRedisCatchStorageService.getSn(GatewayCacheConstants.GATEWAY_INFO_SN_INCR);

        gatewayMqDto.setMsgId(GatewayCacheConstants.GATEWAY_INFO_SN_prefix+sn);
//消息组装
        rabbitMqSender.sendMsg(MarkConstant.SIGIN_SG,  ""+ Instant.now().toEpochMilli() + ConstantUtils.RANDOM_UTIL.nextInt(100), gatewayMqDto, true);
    }
}
