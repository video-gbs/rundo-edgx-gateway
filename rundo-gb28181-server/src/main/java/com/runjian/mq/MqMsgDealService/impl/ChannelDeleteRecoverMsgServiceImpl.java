package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelDeleteRecoverMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IDeviceService deviceService;

    @Autowired
    IDeviceChannelService deviceChannelService;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.CHANNEL_DELETE_RECOVER.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        String channelId = dataJson.getString("channelId");
        String deviceId = dataJson.getString("deviceId");
        deviceChannelService.channelDeleteRecover(deviceId,channelId, commonMqDto.getMsgId());
    }


}
