package com.runjian.media.dispatcher.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.WebRTCTalkReq;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamPlayDto;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.mapper.GatewayTaskMapper;
import com.runjian.media.dispatcher.service.IGatewayDealMsgService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GatewayDealMsgServiceImpl implements IGatewayDealMsgService {

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    GatewayTaskMapper gatewayTaskMapper;

    @Value("${mediaApi.callBackStreamNotify}")
    private String callBackStreamNotify;

    @Value("${mediaApi.streamRtpSendInfo}")
    private String streamRtpSendInfoApi;


    @Override
    public void sendGatewayPlayMsg(SsrcInfo playCommonSsrcInfo, MediaPlayReq playReq) {
        PlayReq gatewayPlayReq = new PlayReq();
        gatewayPlayReq.setSsrcInfo(playCommonSsrcInfo);
        gatewayPlayReq.setDeviceId(playReq.getDeviceId());
        gatewayPlayReq.setChannelId(playReq.getChannelId());
        gatewayPlayReq.setStreamMode(playReq.getStreamMode());
        gatewayPlayReq.setDispatchUrl(playReq.getDispatchUrl()+callBackStreamNotify);
        gatewayPlayReq.setStreamId(playReq.getStreamId());
        //将ssrcinfo通知网关
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.PLAY.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,playReq.getMsgId());
        businessMqInfo.setData(gatewayPlayReq);
        rabbitMqSender.sendMsgByExchange(playReq.getGatewayMqExchange(), playReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);
        //存储网关点播请求
    }

    @Override
    public void sendGatewayPlayBackMsg(SsrcInfo playCommonSsrcInfo, MediaPlayBackReq mediaPlayBackReq) {
        //将ssrcinfo通知网关
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.PLAY_BACK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,mediaPlayBackReq.getMsgId());
        PlayBackReq gatewayPlayBackReq = new PlayBackReq();
        gatewayPlayBackReq.setSsrcInfo(playCommonSsrcInfo);
        gatewayPlayBackReq.setDeviceId(mediaPlayBackReq.getDeviceId());
        gatewayPlayBackReq.setChannelId(mediaPlayBackReq.getChannelId());
        gatewayPlayBackReq.setStreamMode(mediaPlayBackReq.getStreamMode());
        gatewayPlayBackReq.setDispatchUrl(mediaPlayBackReq.getDispatchUrl()+callBackStreamNotify);
        gatewayPlayBackReq.setStreamId(mediaPlayBackReq.getStreamId());
        gatewayPlayBackReq.setStartTime(mediaPlayBackReq.getStartTime());
        gatewayPlayBackReq.setEndTime(mediaPlayBackReq.getEndTime());
        businessMqInfo.setData(gatewayPlayBackReq);
        rabbitMqSender.sendMsgByExchange(mediaPlayBackReq.getGatewayMqExchange(), mediaPlayBackReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);
    }

    @Override
    public void sendGatewayWebrtcTalkMsg(WebRTCTalkReq webRtcTalkReq) {
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.CHANNEL_TALK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,webRtcTalkReq.getMsgId());
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("deviceId",webRtcTalkReq.getDeviceId());
        jsonObject.put("channelId",webRtcTalkReq.getChannelId());
        jsonObject.put("dispatchUrl",webRtcTalkReq.getDispatchUrl()+streamRtpSendInfoApi);

        businessMqInfo.setData(jsonObject);
        rabbitMqSender.sendMsgByExchange(webRtcTalkReq.getGatewayMqExchange(), webRtcTalkReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);
    }

    @Override
    public void sendGatewayStreamBye(OnlineStreamsEntity onlineStreamsEntity, String msgId, OnlineStreamsEntity oneBystreamId) {
        //通知网关
        CommonMqDto byeMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.STOP_PLAY.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        StreamPlayDto streamPlayDto = new StreamPlayDto();
        BeanUtil.copyProperties(onlineStreamsEntity,streamPlayDto);
        byeMqInfo.setData(streamPlayDto);
        rabbitMqSender.sendMsgByExchange(oneBystreamId.getMqExchange(), oneBystreamId.getMqRouteKey(), UuidUtil.toUuid(),byeMqInfo,true);
        //消息链路的数据库记录
    }
}
