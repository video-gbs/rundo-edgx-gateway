package com.runjian.mq.MqMsgDealService.impl;

import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.entity.DeviceEntity;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeviceTotalSyncMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceService deviceService;
    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.DEVICE_TOTAL_SYNC.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        CommonResponse<List<DeviceEntity>> listCommonResponse = deviceService.deviceList();
        //mq消息发送
        gatewayBusinessAsyncSender.sendforAllScene(listCommonResponse, commonMqDto.getMsgId(), GatewayMsgType.DEVICE_TOTAL_SYNC);

    }


}
