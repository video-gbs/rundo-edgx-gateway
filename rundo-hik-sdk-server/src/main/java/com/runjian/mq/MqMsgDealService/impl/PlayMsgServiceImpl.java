package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
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
public class PlayMsgServiceImpl implements InitializingBean, IMsgProcessorService {

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
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.PLAY.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        //设备信息同步  获取设备信息
        PlayReq playReq = JSONObject.toJavaObject(dataJson, PlayReq.class);
        CommonResponse<Integer> play = iplayService.play(playReq);

        //restfulapi请求 分离请求中的streamId
        String streamId = playReq.getStreamId();
        BusinessSceneResp businessSceneResp = new BusinessSceneResp();
        businessSceneResp.setCode(play.getCode());
        businessSceneResp.setMsg(play.getMsg());
        businessSceneResp.setGatewayMsgType(GatewayMsgType.PLAY);

        GatewayStreamNotify gatewayStreamNotify = new GatewayStreamNotify();
        gatewayStreamNotify.setStreamId(streamId);
        gatewayStreamNotify.setBusinessSceneResp(businessSceneResp);
        //获取实体中的设备数据 转换为playreq
        //设备信息同步  获取设备信息
        CommonResponse<Boolean> booleanCommonResponse = RestTemplateUtil.postStreamNotifyRespons(playReq.getDispatchUrl() + streamNotifyApi, gatewayStreamNotify, restTemplate);
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-http请求发送", booleanCommonResponse);

    }

}
