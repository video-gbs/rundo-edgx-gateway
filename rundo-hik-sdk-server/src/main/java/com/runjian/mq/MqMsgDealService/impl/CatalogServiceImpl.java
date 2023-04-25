package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.domain.dto.Device;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IDeviceService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CatalogServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceService deviceService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.CATALOG.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        //设备通道信息同步
        String deviceId = dataJson.getString("deviceId");
    }


}
