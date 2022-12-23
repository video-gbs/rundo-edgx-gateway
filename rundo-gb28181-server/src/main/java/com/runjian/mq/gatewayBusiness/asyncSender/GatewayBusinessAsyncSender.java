package com.runjian.mq.gatewayBusiness.asyncSender;

import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.bean.CatalogData;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
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

    @Autowired
    CatalogDataCatch catalogDataCatch;
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
    }

    /**
     * 通道消息发送
     * @param device
     */
    @Async("taskExecutor")
    public void sendCatalog(Device device){
        //优化catalogData的数据结构
        String deviceId = device.getDeviceId();
        boolean syncRunning = catalogDataCatch.isSyncRunning(deviceId);
        while (!syncRunning){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){

            }
            syncRunning = catalogDataCatch.isSyncRunning(deviceId);
        }
        //进行mq消息发送
        CatalogData data = catalogDataCatch.getData(deviceId);
        GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.CATALOG.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix);
        mqInfo.setCode(data.getCode());
        mqInfo.setMsg(data.getErrorMsg());
        mqInfo.setData(data.getChannelList());
        rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), gatewaySignInConf.getMqGetQueue(), UuidUtil.toUuid(),mqInfo,true);
    }
}

