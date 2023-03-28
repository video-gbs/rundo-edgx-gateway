package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IplayService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RecordInfoServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceChannelService deviceChannelService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.RECORD_INFO.getTypeName(),this);
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
        deviceChannelService.recordInfo(recordInfoReq);
    }


}
