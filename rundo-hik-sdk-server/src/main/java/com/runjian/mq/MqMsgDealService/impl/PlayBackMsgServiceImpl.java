package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayBackReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class PlayBackMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IplayService iplayService;
    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Value("${mdeia-api-uri-list.stream-notify}")
    private String streamNotifyApi;

    @Autowired
    RestTemplate restTemplate;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayBusinessMsgType.PLAY_BACK.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        //设备点播同步
        String deviceId = dataJson.getString("deviceId");
        String channelId = dataJson.getString("channelId");

        CommonResponse<Integer> play = CommonResponse.failure(BusinessErrorEnums.UNKNOWN_ERROR);
        PlayBackReq playBackReq = JSONObject.toJavaObject(dataMapJson, PlayBackReq.class);
        playBackReq.setDeviceId(deviceId);
        playBackReq.setChannelId(channelId);
        playBackReq.setMsgId(commonMqDto.getMsgId());
        try{

            play = iplayService.playBack(playBackReq);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景处理", "业务场景处理-http请求发送", e.getMessage());
        }

        //restfulapi请求 分离请求中的streamId
        String streamId = playBackReq.getStreamId();
        GatewayBusinessSceneResp businessSceneResp = new GatewayBusinessSceneResp();
        businessSceneResp.setCode(play.getCode());
        businessSceneResp.setMsg(play.getMsg());
        businessSceneResp.setGatewayMsgType(GatewayBusinessMsgType.PLAY);

        GatewayStreamNotify gatewayStreamNotify = new GatewayStreamNotify();
        gatewayStreamNotify.setStreamId(streamId);
        gatewayStreamNotify.setBusinessSceneResp(businessSceneResp);
        //获取实体中的设备数据 转换为playreq
        //设备信息同步  获取设备信息
        CommonResponse<Boolean> booleanCommonResponse = RestTemplateUtil.postStreamNotifyRespons(playBackReq.getDispatchUrl() + streamNotifyApi, gatewayStreamNotify, restTemplate);
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-http请求发送", booleanCommonResponse);
    }


}
