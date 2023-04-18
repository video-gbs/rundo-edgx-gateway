package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamCheckListResp;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
public class StreamLivePlayStartMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IMediaPlayService iMediaPlayService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.STREAM_LIVE_PLAY_START.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");

        MediaPlayReq playReq = JSONObject.toJavaObject(dataMapJson, MediaPlayReq.class);
        playReq.setStreamId(dataJson.getString("streamId"));
        playReq.setMsgId(commonMqDto.getMsgId());
        playReq.setGatewayMqExchange(dataMapJson.getString("exchangeName"));
        playReq.setGatewayMqRouteKey(dataMapJson.getString("gatewayMq"));
        playReq.setMsgId(commonMqDto.getMsgId());
        playReq.setDispatchUrl(dataMapJson.getString("mediaUrl"));
        playReq.setStreamMode(dataMapJson.getInteger("streamMode")==1?"UDP":"TCP");
        iMediaPlayService.play(playReq);


    }


}
