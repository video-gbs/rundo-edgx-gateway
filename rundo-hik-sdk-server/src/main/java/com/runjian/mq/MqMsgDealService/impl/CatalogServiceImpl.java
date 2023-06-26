package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.domain.dto.CatalogSyncDto;
import com.runjian.domain.dto.Device;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.CATALOG.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        //设备通道信息同步
        String deviceId = dataJson.getString("deviceId");

        //encodeId转换
        Long encodeId = Long.parseLong(deviceId);
        CommonResponse<CatalogSyncDto> listCommonResponse = deviceChannelService.channelSync(encodeId);

        //mq消息发送
        gatewayBusinessAsyncSender.sendforAllScene(listCommonResponse, commonMqDto.getMsgId(), GatewayBusinessMsgType.CATALOG);
    }


}
