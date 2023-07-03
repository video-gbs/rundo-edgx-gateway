package com.runjian.mq.gatewayInfo;

import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.UuidUtil;
import com.runjian.service.IRedisCatchStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 定时发送心跳
 * @author chenjialing
 */
@Component
public class GatewayHeartBeatSchedule {

    @Value("${gateway-info.serialNum}")
    private String serialNum;
    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;
    @Autowired
    private RabbitMqSender rabbitMqSender;

    @Value("${gateway-info.expire}")
    private int expire;

    //发送队列
    @Value("${mq-defualt.public.queue-id-set:PUBLIC-SG}")
    private String queueId;

    @Scheduled(cron="0 0/1 * * * ?")   //每1分钟执行一次
    public void sendMsg(){
        CommonMqDto commonMqDto = new CommonMqDto();
        commonMqDto.setMsgType(GatewayMsgType.GATEWAY_HEARTBEAT.getTypeName());
        commonMqDto.setTime(LocalDateTime.now());
        commonMqDto.setSerialNum(serialNum);

        String sn = iRedisCatchStorageService.getSn(GatewayCacheConstants.GATEWAY_INFO_SN_INCR);

        commonMqDto.setMsgId(GatewayCacheConstants.GATEWAY_INFO_SN_prefix+sn);
        commonMqDto.setData(String.valueOf(DateUtils.getExpireTimestamp(expire)));
        //消息组装
        rabbitMqSender.sendMsg(queueId, UuidUtil.toUuid(), commonMqDto, true);
    }
}
