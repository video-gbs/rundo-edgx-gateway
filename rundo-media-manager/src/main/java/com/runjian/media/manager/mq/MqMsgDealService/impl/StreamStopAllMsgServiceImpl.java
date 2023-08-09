package com.runjian.media.manager.mq.MqMsgDealService.impl;

import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.media.manager.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.manager.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.manager.service.IMediaPlayService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StreamStopAllMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    IMediaPlayService mediaPlayService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(StreamBusinessMsgType.STREAM_STOP_ALL.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {


        mediaPlayService.streamStopAll();
    }


}
