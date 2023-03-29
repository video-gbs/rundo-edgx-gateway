package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceinfoServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceService deviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.DEVICEINFO.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        //暂不进行对外接口提供
//        JSONObject dataJson = (JSONObject) commonMqDto.getData();
//        //实际的请求参数
//        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
//        //设备信息同步  获取设备信息
//        String deviceId = dataJson.getString("deviceId");
//        Device device = deviceService.getDevice(deviceId);
//        deviceService.deviceInfoQuery(device, commonMqDto.getMsgId());
    }


}
