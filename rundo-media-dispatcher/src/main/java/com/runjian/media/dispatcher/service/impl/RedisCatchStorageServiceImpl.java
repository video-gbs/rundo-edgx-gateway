package com.runjian.media.dispatcher.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.StreamInfoConf;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.mapper.GatewayTaskMapper;
import com.runjian.media.dispatcher.mq.mqEvent.MqSendSceneEvent;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import lombok.Data;
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
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class RedisCatchStorageServiceImpl implements IRedisCatchStorageService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${dispatcher-info.serialNum}")
    private String serialNum;


    @Value("${gb28181-gateway-info.sipDomain}")
    private String sipDomain;

    @Autowired
    private UserSetting userSetting;
    @Autowired
    GatewayTaskMapper gatewayTaskMapper;

    @Autowired
    StreamInfoConf InfoConf;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    private static HashedWheelTimer timer;
    /**
     * 延迟执行时间，可配置在配置文件中，这里为了方便测试设置为20s
     */
    private static long delay = 20L;
    /**
     * 存储Timeout对象，建立订单id与Timeout关系，用于通过订单id找到Timeout从队列中移除
     */
    private static Map<String, Timeout> timeoutMap = new HashMap<>();

    static {
        // 创建延迟队列实例，可以设置时间轮长度及刻度等，这里直接使用默认的
        timer = new HashedWheelTimer();
    }


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
    public CommonMqDto  getMqInfo(String msgType, String snIncr, String snPrefix, String msgId){
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
        Object o = RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipDomain);
        if(ObjectUtils.isEmpty(o)){
            SsrcConfig ssrcConfig = new SsrcConfig(null, sipDomain);
            RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipDomain,ssrcConfig);

        }
        return true;
    }

    @Override
    public synchronized Boolean ssrcRelease(String ssrc) {
        Object o = RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipDomain);
        if(ObjectUtils.isEmpty(o)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"释放ssrc","释放失败,对应的ssrc缓存不存在",ssrc);
            return false;
        }
        SsrcConfig ssrcConfig = (SsrcConfig)o;
        Boolean aBoolean = ssrcConfig.releaseSsrc(ssrc);
        if(aBoolean){
            RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipDomain,ssrcConfig);
        }
        return aBoolean;

    }

    @Override
    public SsrcConfig getSsrcConfig() {
        return  (SsrcConfig)RedisCommonUtil.get(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipDomain);
    }

    @Override
    public Boolean setSsrcConfig(SsrcConfig ssrcConfig) {
        return RedisCommonUtil.set(redisTemplate, VideoManagerConstants.SSRC_CACHE_KEY+sipDomain,ssrcConfig);
    }

    @Override
    public    void editBusinessSceneKey(String businessSceneKey,StreamBusinessMsgType msgType, BusinessErrorEnums businessErrorEnums,Object data) {
        try {
            Timeout timeout = timeoutMap.remove(businessSceneKey);
            if(ObjectUtils.isEmpty(timeout)){
                log.error(LogTemplate.PROCESS_LOG_TEMPLATE,"处理网关业务状态--数据已被处理",businessSceneKey);
                return;
            }
            //取消延时任务
            boolean cancel = timeout.cancel();
            if(!cancel){
                log.error(LogTemplate.PROCESS_LOG_TEMPLATE,"处理网关业务状态--数据其实已经过期了,可能是过期以后才通知，不用处理",businessSceneKey);
                return;
            }
            MsgTimerTask task = (MsgTimerTask)timeout.task();
            StreamBusinessSceneResp businessSceneRespOld = task.getStreamBusinessSceneResp();
            StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneEnd(msgType, businessErrorEnums,businessSceneKey, data,businessSceneRespOld.getMsgId(), businessSceneRespOld.getThreadId());
            MqSendSceneEvent mqSendSceneEvent = new MqSendSceneEvent(this);
            mqSendSceneEvent.setMqSendSceneDto(objectStreamBusinessSceneResp);

            applicationEventPublisher.publishEvent(mqSendSceneEvent);
            RLock lock = redissonClient.getLock( BusinessSceneConstants.BUSINESS_LOCK_KEY+businessSceneKey);
            if(!ObjectUtils.isEmpty(lock)){
                lock.unlockAsync(businessSceneRespOld.getThreadId());
            }
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","数据执行失败",businessSceneKey,e);
        }

    }

    @Override
    public  Boolean  addBusinessSceneKey(String businessSceneKey, StreamBusinessMsgType msgType, String msgId) {
        String redisLockKey = BusinessSceneConstants.BUSINESS_LOCK_KEY+businessSceneKey;
        RLock lock = redissonClient.getLock(redisLockKey);
        Boolean  aBoolean = false;
        try {
            aBoolean = lock.tryLock(10, userSetting.getBusinessSceneTimeout(), TimeUnit.MILLISECONDS);
            if(ObjectUtils.isEmpty(msgId)){
                String sn = getSn(GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR);
                msgId = GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix + sn;
            }
            StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneReady(msgType, msgId, businessSceneKey,null);
            //进行时间轮的插入
            Timeout timeout = timer.newTimeout(new MsgTimerTask(businessSceneKey, objectStreamBusinessSceneResp), userSetting.getBusinessSceneTimeout(), TimeUnit.MILLISECONDS);
            timeoutMap.put(businessSceneKey,timeout);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","缓存添加执行失败",businessSceneKey,e);
        }
        return aBoolean;

    }


    @Data
    public  class MsgTimerTask implements TimerTask {

        private String businessSceneKey;
        private StreamBusinessSceneResp streamBusinessSceneResp;
        public MsgTimerTask(String businessSceneKey,StreamBusinessSceneResp streamBusinessSceneResp){
            this.businessSceneKey = businessSceneKey;
            this.streamBusinessSceneResp = streamBusinessSceneResp;
        }
        @Override
        public void run(Timeout timeout) throws Exception {

            timeoutMap.remove(businessSceneKey);
            MsgTimerTask task = (MsgTimerTask)timeout.task();
            StreamBusinessSceneResp businessSceneRespExpire = task.getStreamBusinessSceneResp();
            businessSceneRespExpire.setCode(BusinessErrorEnums.MSG_OPERATION_TIMEOUT.getErrCode());
            businessSceneRespExpire.setMsg(BusinessErrorEnums.MSG_OPERATION_TIMEOUT.getErrMsg());
            log.info("执行延时任务,延时过期的任务,businessSceneRespExpire={}",businessSceneRespExpire);
            MqSendSceneEvent mqSendSceneEvent = new MqSendSceneEvent(this);
            mqSendSceneEvent.setMqSendSceneDto(businessSceneRespExpire);

            applicationEventPublisher.publishEvent(mqSendSceneEvent);
            RLock lock = redissonClient.getLock( BusinessSceneConstants.BUSINESS_LOCK_KEY+businessSceneKey);
            if(!ObjectUtils.isEmpty(lock)){
                lock.unlockAsync(businessSceneRespExpire.getThreadId());
            }

        }
    }
}
