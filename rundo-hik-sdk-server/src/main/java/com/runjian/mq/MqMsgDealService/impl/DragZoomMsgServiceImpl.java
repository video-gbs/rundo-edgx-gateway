package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.DragZoomControlReq;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IPtzService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DragZoomMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IPtzService ptzService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.CHANNEL_3D_OPERATION.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        String deviceId = dataJson.getString("deviceId");
        String channelId = dataJson.getString("channelId");
        DragZoomControlReq dragZoomControlReq = JSONObject.toJavaObject(dataMapJson, DragZoomControlReq.class);
        dragZoomControlReq.setDeviceId(deviceId);
        dragZoomControlReq.setChannelId(channelId);
        dragZoomControlReq.setMsgId(commonMqDto.getMsgId());
        ptzService.dragZoomControl(dragZoomControlReq);
    }


}
