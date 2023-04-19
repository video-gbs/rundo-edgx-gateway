package com.runjian.media.manager.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.manager.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.manager.mq.MqMsgDealService.IMsgProcessorService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class StreamCheckStreamMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;


    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.STREAM_CHECK_STREAM.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        if(!ObjectUtils.isEmpty(dataJson)){
            JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
            StreamCheckListResp streamCheckListResp = JSONObject.toJavaObject(dataMapJson, StreamCheckListResp.class);
        }


    }


}
