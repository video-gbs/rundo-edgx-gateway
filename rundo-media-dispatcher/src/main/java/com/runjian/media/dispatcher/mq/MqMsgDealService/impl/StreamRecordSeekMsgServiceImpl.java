package com.runjian.media.dispatcher.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.dispatcher.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.dispatcher.service.IOnlineStreamsService;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @author chenjialing
 */
@Component
@Slf4j
public class StreamRecordSeekMsgServiceImpl implements InitializingBean, IMsgProcessorService {

    @Autowired
    IMqMsgDealServer iMqMsgDealServer;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RabbitMqSender rabbitMqSender;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;
    @Autowired
    IOnlineStreamsService onlineStreamsService;

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(StreamBusinessMsgType.STREAM_RECORD_SEEK.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();

        //实际的请求参数
        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        String streamId = dataJson.getString("streamId");
        OnlineStreamsEntity oneBystreamId = onlineStreamsService.getOneBystreamId(streamId);
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(StreamBusinessMsgType.STREAM_RECORD_SEEK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,commonMqDto.getMsgId());
        String mqGetQueue = dispatcherSignInConf.getMqSetQueue();

        if(ObjectUtils.isEmpty(oneBystreamId)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流拖拉操作","操作失败,流的缓存信息不存在",commonMqDto);
            businessMqInfo.setCode(BusinessErrorEnums.STREAM_NOT_FOUND.getErrCode());
            businessMqInfo.setMsg(BusinessErrorEnums.STREAM_NOT_FOUND.getErrMsg());
            businessMqInfo.setData(false);
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
            return;
        }
        //通知网关进行设备操作  todo 暂时不考虑网关操作结果的返回
        CommonMqDto gatewayMqInfo = redisCatchStorageService.getMqInfo(GatewayBusinessMsgType.DEVICE_RECORD_SEEK.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);


        gatewayMqInfo.setData(dataJson);
        rabbitMqSender.sendMsgByExchange(oneBystreamId.getMqExchange(), oneBystreamId.getMqRouteKey(), UuidUtil.toUuid(),gatewayMqInfo,true);
        //通知调度中心成功
        businessMqInfo.setData(true);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
    }


}
