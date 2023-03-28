package com.runjian.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.NoneStreamReaderReq;
import com.runjian.common.commonDto.StreamInfo;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.service.IplayService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayNoneStreamReaderCallbackServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IplayService iplayService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.PLAY_NONE_STREAM_READER_CALLBACK.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        NoneStreamReaderReq noneStreamReaderReq = JSONObject.toJavaObject((JSONObject) commonMqDto.getData(),NoneStreamReaderReq.class);
        iplayService.onStreamNoneReader(noneStreamReaderReq);
    }


}
