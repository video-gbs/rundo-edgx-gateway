package com.runjian.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.SipConfig;
import com.runjian.conf.SsrcConfig;
import com.runjian.conf.UserSetting;
import com.runjian.dao.GatewayTaskMapper;
import com.runjian.mq.event.mqEvent.MqSendSceneDto;
import com.runjian.mq.event.mqEvent.MqSendSceneEvent;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class RedisCatchStorageServiceImpl implements IRedisCatchStorageService {

    @Autowired
    private RedisTemplate redisTemplate;



    @Value("${gateway-info.serialNum}")
    private String serialNum;

    @Autowired
    SipConfig sipConfig;


    @Autowired
    GatewayTaskMapper gatewayTaskMapper;

    @Autowired
    UserSetting userSetting;


    @Autowired
    GatewayInfoConf gatewayInfoConf;
    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @Override
    public Long getCSEQ() {
        String key = VideoManagerConstants.SIP_CSEQ_PREFIX;

        long result =  RedisCommonUtil.incr(key, 1L,redisTemplate);
        if (result > Long.MAX_VALUE) {
            RedisCommonUtil.set(redisTemplate,key, 1);
            result = 1;
        }
        return result;
    }

    @Override
    public String getSn(String key) {
        long result =  RedisCommonUtil.incr(key, 1L,redisTemplate);
        if (result > Long.MAX_VALUE) {
            RedisCommonUtil.set(redisTemplate,key, 1);
            result = 1;
        }
        return Long.toString(result);
    }

    @Override
    public CommonMqDto getMqInfo(String msgType, String snIncr, String snPrefix, String msgId) {
        CommonMqDto commonMqDto = new CommonMqDto();
        commonMqDto.setMsgType(msgType);
        commonMqDto.setTime(LocalDateTime.now());
        commonMqDto.setSerialNum(serialNum);

        String sn = getSn(snIncr);
        if(ObjectUtils.isEmpty(msgId)){
            commonMqDto.setMsgId(snPrefix+sn);

        }else {
            commonMqDto.setMsgId(msgId);

        }
        return commonMqDto;
    }

    @Override
    public Boolean ssrcInit() {

        Object o = RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipConfig.getDomain());
        if(ObjectUtils.isEmpty(o)){
            SsrcConfig ssrcConfig = new SsrcConfig(null, sipConfig.getDomain());
            RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipConfig.getDomain(),ssrcConfig);

        }
        return true;
    }

    @Override
    public Boolean ssrcRelease(String ssrc) {
        Object o = RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipConfig.getDomain());
        if(ObjectUtils.isEmpty(o)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"释放ssrc","释放失败,对应的ssrc缓存不存在",ssrc);
            return false;
        }
        SsrcConfig ssrcConfig = (SsrcConfig)o;
        Boolean aBoolean = ssrcConfig.releaseSsrc(ssrc);
        if(aBoolean){
            RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipConfig.getDomain(),ssrcConfig);
        }
        return aBoolean;

    }

    @Override
    public SsrcConfig getSsrcConfig() {
        return  (SsrcConfig)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipConfig.getDomain());
    }

    @Override
    public Boolean setSsrcConfig(SsrcConfig ssrcConfig) {
        return RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipConfig.getDomain(),ssrcConfig);
    }

    @Override
    public synchronized   void editBusinessSceneKey(String businessSceneKey,BusinessErrorEnums businessErrorEnums,Object data) {
        try {
            //超时时间内返回，处理全部的消息聚合的消息
            Object removeObj = redisDelayQueuesUtil.remove(businessSceneKey);
            if(ObjectUtils.isEmpty(removeObj)){
                //队列删除失败
            }else {
                //异步进行全局锁解锁
                GatewayBusinessSceneResp businessSceneResp = (GatewayBusinessSceneResp) removeObj;
                MqSendSceneEvent mqSendSceneEvent = new MqSendSceneEvent(this);
                businessSceneResp.setData(data);
                mqSendSceneEvent.setMqSendSceneDto(new MqSendSceneDto(businessSceneResp,businessErrorEnums));
                applicationEventPublisher.publishEvent(mqSendSceneEvent);
                RLock lock = redissonClient.getLock( BusinessSceneConstants.GATEWAY_BUSINESS_LOCK_KEY+businessSceneKey);
                if(!ObjectUtils.isEmpty(lock)){
                    lock.unlockAsync(businessSceneResp.getThreadId());
                }
            }

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","数据执行失败",businessSceneKey,e);
        }

    }

    @Override
    public  Boolean addBusinessSceneKey(String businessSceneKey, GatewayBusinessMsgType msgType, String msgId,Integer sendType) {
        String redisLockKey = BusinessSceneConstants.GATEWAY_BUSINESS_LOCK_KEY+businessSceneKey;
        RLock lock = redissonClient.getLock(redisLockKey);
        Boolean  aBoolean = false;
        try {
            //分布式锁 进行
            aBoolean = lock.tryLock(10, userSetting.getBusinessSceneTimeout()-100, TimeUnit.MILLISECONDS);
            if(ObjectUtils.isEmpty(msgId)){
                String sn = getSn(GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR);
                msgId = GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix + sn;
            }
            GatewayBusinessSceneResp<Object> objectStreamBusinessSceneResp = GatewayBusinessSceneResp.addSceneReady(msgType, msgId, businessSceneKey,null,sendType);
            RedisCommonUtil.leftPush(redisTemplate,BusinessSceneConstants.GATEWAY_BUSINESS_LISTS+businessSceneKey,objectStreamBusinessSceneResp);
            if(aBoolean){
                redisDelayQueuesUtil.addDelayQueue(objectStreamBusinessSceneResp, 20, TimeUnit.SECONDS,businessSceneKey);
            }

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","缓存添加执行失败",businessSceneKey,e);
        }
        return aBoolean;

    }
}
