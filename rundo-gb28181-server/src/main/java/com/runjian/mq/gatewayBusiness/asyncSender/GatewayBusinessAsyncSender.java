package com.runjian.mq.gatewayBusiness.asyncSender;

import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.bean.Device;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

/**
 * @author chenjialing
 */
@Component
@Slf4j
public class GatewayBusinessAsyncSender {

    @Autowired
    RabbitMqSender rabbitMqSender;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    //注册mq消息发送
    @Async("taskExecutor")
    public void sendRegister(Device device){
        //构造消息请求体
        String mqGetQueue = gatewaySignInConf.getMqGetQueue();
        if(ObjectUtils.isEmpty(mqGetQueue)){
            //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "设备注册-信令发送失败，业务队列暂时未初始化", device);
            return;
        }
        GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.REGISTER.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix);
        mqInfo.setData(device);
        rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),mqInfo,true);
        return;
    }
}
