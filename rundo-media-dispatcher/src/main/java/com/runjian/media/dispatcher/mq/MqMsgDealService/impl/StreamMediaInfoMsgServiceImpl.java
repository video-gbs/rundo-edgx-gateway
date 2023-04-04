package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import com.runjian.media.dispatcher.zlm.ZLMRESTfulUtils;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.service.ImediaServerService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Component
public class StreamMediaInfoMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;

    @Autowired
    ZLMRESTfulUtils zlmresTfulUtils;

    @Autowired
    IOnlineStreamsService onlineStreamsService;
    @Autowired
    ImediaServerService imediaServerService;


    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.STREAM_MEDIA_INFO.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数
        String streamId = dataJson.getString("streamId");
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        String app = VideoManagerConstants.GB28181_APP;
        String schema = VideoManagerConstants.GB28181_SCHEAM;
        if(!ObjectUtils.isEmpty(dataMapJson)){
            String app1 = dataJson.getString("app");
            String schema1 = dataJson.getString("schema");
            if(!ObjectUtils.isEmpty(app1)){
                app = app1;
            }
            if(!ObjectUtils.isEmpty(schema1)){
                schema = schema1;
            }
        }
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STREAM_MEDIA_INFO.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,commonMqDto.getMsgId());
        String mqGetQueue = dispatcherSignInConf.getMqSetQueue();

        //判断流属于哪个流媒体
        OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
        if(ObjectUtils.isEmpty(oneBystreamId)){
            //流信息不存在
            businessMqInfo.setCode(BusinessErrorEnums.STREAM_NOT_FOUND.getErrCode());
            businessMqInfo.setMsg(BusinessErrorEnums.STREAM_NOT_FOUND.getErrMsg());
            businessMqInfo.setData(false);

            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
            return;
        }
        String mediaServerId = oneBystreamId.getMediaServerId();

        MediaServerItem mediaServerItemOne = imediaServerService.getOne(mediaServerId);


        JSONObject mediaInfo = zlmresTfulUtils.getMediaInfo(mediaServerItemOne, app, schema, streamId);
        businessMqInfo.setData(mediaInfo);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
    }


}
