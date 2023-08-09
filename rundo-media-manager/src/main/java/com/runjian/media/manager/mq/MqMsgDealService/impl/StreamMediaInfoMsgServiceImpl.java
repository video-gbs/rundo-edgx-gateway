package com.runjian.media.manager.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamAudioMediaInfoResp;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamMediaInfoResp;
import com.runjian.common.commonDto.Gb28181Media.resp.StreamVideoMediaInfoResp;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.manager.conf.mq.DispatcherSignInConf;
import com.runjian.media.manager.dto.entity.OnlineStreamsEntity;
import com.runjian.media.manager.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.manager.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.manager.service.IMediaPlayService;
import com.runjian.media.manager.service.IOnlineStreamsService;
import com.runjian.media.manager.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component
public class StreamMediaInfoMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;


    @Autowired
    IOnlineStreamsService onlineStreamsService;


    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;

    @Autowired
    IMediaPlayService iMediaPlayService;
    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(StreamBusinessMsgType.STREAM_MEDIA_INFO.getTypeName(),this);
    }


//    {
//        "code": 0,
//            "msg": "success",
//            "data": [
//        {
//            "key": 801,
//                "app": "gb28181",
//                "streamId": "LIVE_44",
//                "status": false,
//                "sourceURL": "rtp://127.0.0.1:22002/gb28181/LIVE_44",
//                "networkType": 61,
//                "readerCount": 2,
//                "videoCodec": "H264",
//                "width": 1920,
//                "height": 1080,
//                "audioCodec": "",
//                "audioChannels": 0,
//                "audioSampleRate": 0,
//                "url": {
//            "ws-flv": "ws://192.192.192.132:6088/gb28181/LIVE_44.flv",
//                    "http-flv": "http://192.192.192.132:8080/gb28181/LIVE_44.flv"
//        }
//        }
//    ]
//    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        String streamId = dataJson.getString("streamId");

        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_MEDIA_INFO.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,commonMqDto.getMsgId());
        String mqGetQueue = dispatcherSignInConf.getMqSetQueue();
        try {
            StreamMediaInfoResp streamMediaInfoResp = iMediaPlayService.streamMediaInfo(streamId);
            businessMqInfo.setData(streamMediaInfoResp);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "流信息获取", "流信息获取失败", e);
            businessMqInfo.setCode(BusinessErrorEnums.STREAM_NOT_FOUND.getErrCode());
            businessMqInfo.setMsg(e.getMessage());
            businessMqInfo.setData(false);
        }
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);

    }


}
