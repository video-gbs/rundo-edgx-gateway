package com.runjian.media.dispatcher.service.impl;

import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.SsrcInfo;
import com.runjian.common.commonDto.StreamPlayDto;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.dispatcher.mapper.GatewayTaskMapper;
import com.runjian.media.dispatcher.service.IGatewayDealMsgService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public void sendGatewayPlayMsg(SsrcInfo playCommonSsrcInfo, MediaPlayReq playReq) {
        PlayReq gatewayPlayReq = new PlayReq();
        gatewayPlayReq.setSsrcInfo(playCommonSsrcInfo);
        gatewayPlayReq.setDeviceId(playReq.getDeviceId());
        gatewayPlayReq.setChannelId(playReq.getChannelId());
        gatewayPlayReq.setStreamMode(playReq.getStreamMode());
        gatewayPlayReq.setDispatchUrl(playReq.getDispatchUrl());
        gatewayPlayReq.setStreamId(playReq.getStreamId());
        //将ssrcinfo通知网关
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.PLAY.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,playReq.getMsgId());
        businessMqInfo.setData(gatewayPlayReq);
        rabbitMqSender.sendMsgByExchange(playReq.getGatewayMqExchange(), playReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);
        //存储网关点播请求
        //消息链路的数据库记录
        GatewayTask gatewayTask = new GatewayTask();
        gatewayTask.setMsgId(playReq.getMsgId());
        gatewayTask.setBusinessKey(GatewayBusinessMsgType.PLAY+playReq.getStreamId());
        gatewayTask.setCode(BusinessErrorEnums.BUSINESS_SCENE_RUNNING.getErrCode());

        gatewayTask.setMsg(BusinessErrorEnums.BUSINESS_SCENE_RUNNING.getErrMsg());
        gatewayTask.setMsgType(GatewayBusinessMsgType.PLAY.getTypeName());
        gatewayTask.setStatus(0);
        gatewayTask.setSourceType(1);
        gatewayTask.setThreadId(Thread.currentThread().getId());
        gatewayTaskMapper.add(gatewayTask);

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
        gatewayPlayBackReq.setDispatchUrl(mediaPlayBackReq.getDispatchUrl());
        gatewayPlayBackReq.setStreamId(mediaPlayBackReq.getStreamId());
        gatewayPlayBackReq.setStartTime(mediaPlayBackReq.getStartTime());
        gatewayPlayBackReq.setEndTime(mediaPlayBackReq.getEndTime());
        rabbitMqSender.sendMsgByExchange(mediaPlayBackReq.getGatewayMqExchange(), mediaPlayBackReq.getGatewayMqRouteKey(), UuidUtil.toUuid(),businessMqInfo,true);
//消息链路的数据库记录
        GatewayTask gatewayTask = new GatewayTask();
        gatewayTask.setMsgId(mediaPlayBackReq.getMsgId());
        gatewayTask.setBusinessKey(GatewayBusinessMsgType.PLAY_BACK+mediaPlayBackReq.getStreamId());
        gatewayTask.setCode(BusinessErrorEnums.BUSINESS_SCENE_RUNNING.getErrCode());

        gatewayTask.setMsg(BusinessErrorEnums.BUSINESS_SCENE_RUNNING.getErrMsg());
        gatewayTask.setMsgType(GatewayBusinessMsgType.PLAY_BACK.getTypeName());
        gatewayTask.setStatus(0);
        gatewayTask.setSourceType(1);
        gatewayTask.setThreadId(Thread.currentThread().getId());
        gatewayTaskMapper.add(gatewayTask);
    }

    @Override
    public void sendGatewayStreamBye(String streamId, String msgId, BaseRtpServerDto baseRtpServerDto) {
        //通知网关
        CommonMqDto byeMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.STOP_PLAY.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
        StreamPlayDto streamPlayDto = new StreamPlayDto();
        streamPlayDto.setStreamId(streamId);
        byeMqInfo.setData(streamPlayDto);
        GatewayBindReq gatewayBindReq = baseRtpServerDto.getGatewayBindReq();
        rabbitMqSender.sendMsgByExchange(gatewayBindReq.getMqExchange(), gatewayBindReq.getMqRouteKey(), UuidUtil.toUuid(),byeMqInfo,true);
        //消息链路的数据库记录
        GatewayTask gatewayTask = new GatewayTask();
        gatewayTask.setMsgId(msgId);
        gatewayTask.setBusinessKey(GatewayBusinessMsgType.STOP_PLAY+streamId);
        gatewayTask.setCode(BusinessErrorEnums.BUSINESS_SCENE_RUNNING.getErrCode());
        gatewayTask.setMsg(BusinessErrorEnums.BUSINESS_SCENE_RUNNING.getErrMsg());
        gatewayTask.setMsgType(GatewayBusinessMsgType.STOP_PLAY.getTypeName());
        gatewayTask.setStatus(0);
        gatewayTask.setSourceType(1);
        gatewayTask.setThreadId(Thread.currentThread().getId());
        gatewayTaskMapper.add(gatewayTask);
    }
}
