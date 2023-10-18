package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.WebRTCTalkReq;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamWebrtcTalkMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IMediaPlayService iMediaPlayService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(StreamBusinessMsgType.STREAM_WEBRTC_TALK.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");

        WebRTCTalkReq playReq = JSONObject.toJavaObject(dataMapJson, WebRTCTalkReq.class);
        playReq.setStreamId(dataJson.getString("streamId"));
        playReq.setMsgId(commonMqDto.getMsgId());
        playReq.setGatewayMqExchange(dataMapJson.getString("exchangeName"));
        playReq.setGatewayMqRouteKey(dataMapJson.getString("gatewayMq"));
        playReq.setMsgId(commonMqDto.getMsgId());
        playReq.setDispatchUrl(dataMapJson.getString("mediaUrl"));
        iMediaPlayService.webRtcTalk(playReq);


    }


}
