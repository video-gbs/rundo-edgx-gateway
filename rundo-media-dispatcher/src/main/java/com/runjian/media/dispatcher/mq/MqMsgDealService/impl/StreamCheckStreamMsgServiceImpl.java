package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StreamCheckStreamMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    ImediaServerService imediaServerService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.STREAM_CHECK_STREAM.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");

        JSONArray streamArray = dataMapJson.getJSONArray("streamIdList");
        List<String> streamIdList = JSONArray.parseArray(streamArray.toJSONString(), String.class);
        imediaServerService.streamListByStreamIds(streamIdList,commonMqDto.getMsgId());
    }


}
