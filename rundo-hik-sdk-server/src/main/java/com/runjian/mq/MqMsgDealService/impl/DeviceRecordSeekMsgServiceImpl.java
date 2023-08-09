package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.StreamSeekReq;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.DateUtils;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IplayService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DeviceRecordSeekMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IplayService iplayService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.DEVICE_RECORD_SEEK.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息 String streamId,Double speed,String msgId
        //设备通道信息同步
        String streamId = dataJson.getString("streamId");
        StreamSeekReq streamSeekReq = JSONObject.toJavaObject(dataMapJson, StreamSeekReq.class);
        long seekTime = DateUtils.StringToTimeStamp(streamSeekReq.getTargetTime()) - DateUtils.StringToTimeStamp(streamSeekReq.getCurrentTime());
        iplayService.playSeekControl(streamId, seekTime,commonMqDto.getMsgId());
    }


}
