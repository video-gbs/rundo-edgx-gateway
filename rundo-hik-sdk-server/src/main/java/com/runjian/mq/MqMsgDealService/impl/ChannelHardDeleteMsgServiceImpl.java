package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceChannelService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelHardDeleteMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.CHANNEL_DELETE_HARD.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        String channelId = dataJson.getString("channelId");
        String deviceId = dataJson.getString("deviceId");
        long channelDbId = Long.parseLong(channelId);
        CommonResponse<Object> response = CommonResponse.success(true);
        try {
            deviceChannelService.channelHardDelete(channelDbId);


        }catch (Exception e){
            response = CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR,e.getMessage());
        }
        //mq消息发送
        gatewayBusinessAsyncSender.sendforAllScene(response, commonMqDto.getMsgId(), GatewayBusinessMsgType.CHANNEL_DELETE_SOFT);
    }


}
