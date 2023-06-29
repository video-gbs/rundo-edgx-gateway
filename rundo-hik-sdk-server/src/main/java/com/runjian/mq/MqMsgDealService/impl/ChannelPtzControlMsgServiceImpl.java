package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.ChannelPtzControlReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.domain.dto.CatalogSyncDto;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IPtzService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelPtzControlMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IPtzService ptzService;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.PTZ_CONTROL.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        String deviceId = dataJson.getString("deviceId");
        String channelId = dataJson.getString("channelId");
        ChannelPtzControlReq channelPtzControlReq = JSONObject.toJavaObject(dataMapJson, ChannelPtzControlReq.class);
        channelPtzControlReq.setDeviceId(deviceId);
        channelPtzControlReq.setChannelId(channelId);
        channelPtzControlReq.setMsgId(commonMqDto.getMsgId());
        CommonResponse<Object> response = CommonResponse.success(true);
        try {
            Integer statusCode = ptzService.ptzControl(channelPtzControlReq);
            if(statusCode!=0){
                response = CommonResponse.failure(BusinessErrorEnums.PTZ_OPERATION_ERROR,"网关错误码为："+statusCode);
            }
        }catch (Exception e){
            response = CommonResponse.failure(BusinessErrorEnums.PTZ_OPERATION_ERROR,e.getMessage());
        }

        //mq消息发送
        gatewayBusinessAsyncSender.sendforAllScene(response, commonMqDto.getMsgId(), GatewayBusinessMsgType.PTZ_CONTROL);

    }


}
