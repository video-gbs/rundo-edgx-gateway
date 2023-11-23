package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPictureDownloadReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaRecordDownloadReq;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamPictureDownloadStartMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IMediaPlayService iMediaPlayService;

    @Autowired
    IMediaPlayService mediaPlayService;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(StreamBusinessMsgType.STREAM_PICTURE_DOWNLOAD.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");

        MediaPictureDownloadReq playReq = JSONObject.toJavaObject(dataMapJson, MediaPictureDownloadReq.class);
        playReq.setStreamId(dataJson.getString("streamId"));
        playReq.setMsgId(commonMqDto.getMsgId());
        playReq.setGatewayMqExchange(dataMapJson.getString("exchangeName"));
        playReq.setGatewayMqRouteKey(dataMapJson.getString("gatewayMq"));
        playReq.setMsgId(commonMqDto.getMsgId());
        playReq.setDispatchUrl(dataMapJson.getString("mediaUrl"));
        playReq.setStreamMode(dataMapJson.getInteger("streamMode"));

        mediaPlayService.playPictureDownload(playReq);

    }


}
