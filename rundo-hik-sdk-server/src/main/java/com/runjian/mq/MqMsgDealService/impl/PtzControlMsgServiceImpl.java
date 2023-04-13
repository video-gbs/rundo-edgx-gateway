package com.runjian.mq.MqMsgDealService.impl;//package com.runjian.mq.MqMsgDealService.impl;
//
//import com.alibaba.fastjson.JSONObject;
//import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;
//import com.runjian.common.constant.GatewayMsgType;
//import com.runjian.common.mq.domain.CommonMqDto;
//import com.runjian.gb28181.bean.Device;
//import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
//import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
//import com.runjian.service.IDeviceService;
//import com.runjian.service.IPtzService;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class PtzControlMsgServiceImpl implements InitializingBean, IMsgProcessorService {
//
////    @Autowired
////    IMqMsgDealServer iMqMsgDealServer;
////
////    @Autowired
////    IPtzService ptzService;
////
////    @Override
////    public void afterPropertiesSet() throws Exception {
////        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.PTZ_CONTROL.getTypeName(),this);
////    }
////
////    @Override
////    public void process(CommonMqDto commonMqDto) {
////        JSONObject dataJson = (JSONObject) commonMqDto.getData();
////        //实际的请求参数
////        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
////        //设备信息同步  获取设备信息
////        String deviceId = dataJson.getString("deviceId");
////        String channelId = dataJson.getString("channelId");
////        DeviceControlReq deviceControlReq = JSONObject.toJavaObject(dataMapJson, DeviceControlReq.class);
////        deviceControlReq.setDeviceId(deviceId);
////        deviceControlReq.setChannelId(channelId);
////        deviceControlReq.setMsgId(commonMqDto.getMsgId());
////        ptzService.deviceControl(deviceControlReq);
////    }
//
//
//}
