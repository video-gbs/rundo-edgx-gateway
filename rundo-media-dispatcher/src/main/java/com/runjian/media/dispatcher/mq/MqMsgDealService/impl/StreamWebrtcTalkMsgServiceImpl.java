package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.req.MediaPlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.WebRTCTalkReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.service.IMediaPlayService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StreamWebrtcTalkMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IMediaPlayService iMediaPlayService;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;
    @Autowired
    RabbitMqSender rabbitMqSender;
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
        StreamInfo webRtcTalkUrl = null;
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_WEBRTC_TALK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,commonMqDto.getMsgId());
        try {
            webRtcTalkUrl = iMediaPlayService.webRtcTalk(playReq);
        }catch (Exception e){
            businessMqInfo.setCode(BusinessErrorEnums.UNKNOWN_ERROR.getErrCode());
            businessMqInfo.setMsg(e.getMessage());

            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm的webrtc注册失败", "失败信息", e);
        }
        //进行消息返回
        String mqGetQueue = dispatcherSignInConf.getMqSetQueue();
        //通知调度中心成功
        businessMqInfo.setData(webRtcTalkUrl);
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "zlm的webrtc注册发送", "发送信息为:", businessMqInfo);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);

    }


}
