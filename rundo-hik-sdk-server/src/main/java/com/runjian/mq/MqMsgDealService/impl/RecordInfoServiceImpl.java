package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.domain.dto.commder.RecordAllItem;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RecordInfoServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.RECORD_INFO.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        String deviceId = dataJson.getString("deviceId");
        String channelId = dataJson.getString("channelId");
        RecordInfoReq recordInfoReq = JSONObject.toJavaObject(dataMapJson, RecordInfoReq.class);
        recordInfoReq.setChannelId(channelId);
        recordInfoReq.setDeviceId(deviceId);
        recordInfoReq.setMsgId(commonMqDto.getMsgId());
        recordInfoReq.setMsgId(commonMqDto.getMsgId());
        RecordAllItem recordAllItem = new RecordAllItem();
        CommonResponse<RecordAllItem> commonResponse = CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR);
        try{
            recordAllItem = deviceChannelService.recordInfo(recordInfoReq);
            commonResponse = CommonResponse.success(recordAllItem);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景处理", "录像文件获取失败", e.getMessage());
            commonResponse.setMsg(e.getMessage());

        }


        //mq消息发送
        gatewayBusinessAsyncSender.sendforAllScene(commonResponse, commonMqDto.getMsgId(), GatewayBusinessMsgType.RECORD_INFO);
    }


}
