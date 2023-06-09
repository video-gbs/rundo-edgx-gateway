package com.runjian.media.manager.mq.MqMsgDealService.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gb28181Media.BaseRtpServerDto;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.conf.mq.DispatcherSignInConf;
import com.runjian.media.manager.mq.MqMsgDealService.IMqMsgDealServer;
import com.runjian.media.manager.mq.MqMsgDealService.IMsgProcessorService;
import com.runjian.media.manager.service.IRedisCatchStorageService;
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
public class StreamRecordPauseMsgServiceImpl implements InitializingBean, IMsgProcessorService {

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

    @Override
    public void afterPropertiesSet() throws Exception {
        iMqMsgDealServer.addRequestProcessor(GatewayMsgType.STREAM_RECORD_PAUSE.getTypeName(),this);
    }

    @Override
    public void process(CommonMqDto commonMqDto) {
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        //实际的请求参数

        //设备信息同步  获取设备信息 String streamId,Double speed,String msgId
        String streamId = dataJson.getString("streamId");
        //通知网关操作
        BaseRtpServerDto baseRtpServerDto = (BaseRtpServerDto) RedisCommonUtil.get(redisTemplate, VideoManagerConstants.MEDIA_RTP_SERVER_REQ + BusinessSceneConstants.SCENE_SEM_KEY + streamId);
        CommonMqDto businessMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.STREAM_RECORD_PAUSE.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,commonMqDto.getMsgId());
        String mqGetQueue = dispatcherSignInConf.getMqSetQueue();

        if(ObjectUtils.isEmpty(baseRtpServerDto)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"流暂停操作","操作失败,流的缓存信息不存在",commonMqDto);
            businessMqInfo.setCode(BusinessErrorEnums.STREAM_NOT_FOUND.getErrCode());
            businessMqInfo.setMsg(BusinessErrorEnums.STREAM_NOT_FOUND.getErrMsg());
            businessMqInfo.setData(false);
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);
            return;
        }

        RedisCommonUtil.set(redisTemplate,VideoManagerConstants.MEDIA_STREAM_PAUSE+ BusinessSceneConstants.SCENE_SEM_KEY+streamId,streamId,60);
        //通知网关进行bye请求的发送

        //通知网关进行设备操作  todo 暂时不考虑网关操作结果的返回
        CommonMqDto gatewayMqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.DEVICE_RECORD_PAUSE.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);

        gatewayMqInfo.setData(dataJson);
        GatewayBindReq gatewayBindReq = baseRtpServerDto.getGatewayBindReq();
        rabbitMqSender.sendMsgByExchange(gatewayBindReq.getMqExchange(), gatewayBindReq.getMqRouteKey(), UuidUtil.toUuid(),gatewayMqInfo,true);
        //通知调度中心成功
        businessMqInfo.setData(true);
        rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),businessMqInfo,true);

    }


}
