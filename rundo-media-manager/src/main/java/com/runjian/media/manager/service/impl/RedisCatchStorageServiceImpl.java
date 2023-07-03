package com.runjian.media.manager.service.impl;


import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.conf.UserSetting;
import com.runjian.media.manager.event.mqEvent.MqSendSceneDto;
import com.runjian.media.manager.event.mqEvent.MqSendSceneEvent;
import com.runjian.media.manager.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
import com.runjian.media.manager.service.IRedisCatchStorageService;
import com.runjian.media.manager.utils.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
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
    RedisDelayQueuesUtil redisDelayQueuesUtil;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
//    @Scheduled(fixedRate = 10000)
    public void msgExpireRoutine() {
//        LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(userSetting.getBusinessSceneTimeout() / 1000);
//        List<GatewayTask> listByBusinessKey = gatewayTaskMapper.getExpireListByBusinessKey(localDateTime);
//        if(!ObjectUtils.isEmpty(listByBusinessKey)){
//            ConcurrentLinkedQueue<StreamBusinessSceneResp> taskQueue = InfoConf.getTaskQueue();
//            for (GatewayTask gatewayTask : listByBusinessKey) {
//                if(gatewayTask.getSourceType() == 1){
//                    //直接进行超时数据库的修改
//                    gatewayTask.setCode(BusinessErrorEnums.MSG_OPERATION_TIMEOUT.getErrCode());
//                    gatewayTask.setMsg(BusinessErrorEnums.MSG_OPERATION_TIMEOUT.getErrMsg());
//                    gatewayTask.setStatus(BusinessSceneStatusEnum.TimeOut.getCode());
//                    gatewayTaskMapper.updateById(gatewayTask);
//                    continue;
//                }
//
//                StreamBusinessMsgType typeName = StreamBusinessMsgType.getTypeName(gatewayTask.getMsgType());
//                StreamBusinessSceneResp<Object> objectGatewayBusinessSceneResp = StreamBusinessSceneResp.addSceneTimeout(typeName, BusinessErrorEnums.MSG_OPERATION_TIMEOUT,gatewayTask.getBusinessKey(), null,gatewayTask.getMsgId(),gatewayTask.getThreadId());
//
//                taskQueue.offer(objectGatewayBusinessSceneResp);
//            }
//        }
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
    public synchronized   void editBusinessSceneKey(String businessSceneKey,BusinessErrorEnums businessErrorEnums,Object data) {
        try {
            //超时时间内返回，处理全部的消息聚合的消息
            Object removeObj = redisDelayQueuesUtil.remove(businessSceneKey);
            if(removeObj.equals(false)){
                //队列删除失败
            }else {
                //异步进行全局锁解锁
                StreamBusinessSceneResp streamBusinessSceneResp = (StreamBusinessSceneResp) removeObj;
                MqSendSceneEvent mqSendSceneEvent = new MqSendSceneEvent(this);
                mqSendSceneEvent.setMqSendSceneDto(new MqSendSceneDto(streamBusinessSceneResp,businessErrorEnums,data));
                applicationEventPublisher.publishEvent(mqSendSceneEvent);
                RLock lock = redissonClient.getLock( BusinessSceneConstants.SELF_BUSINESS_LOCK_KEY+businessSceneKey);
                if(!ObjectUtils.isEmpty(lock)){
                    lock.unlockAsync(streamBusinessSceneResp.getThreadId());
                }
            }

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","数据执行失败",businessSceneKey,e);
        }

    }

    @Override
    public  Boolean addBusinessSceneKey(String businessSceneKey, StreamBusinessMsgType msgType, String msgId) {
        String redisLockKey = BusinessSceneConstants.SELF_BUSINESS_LOCK_KEY+businessSceneKey;
        RLock lock = redissonClient.getLock(redisLockKey);
        Boolean  aBoolean = false;
        try {
            //分布式锁 进行
            aBoolean = lock.tryLock(10, 60, TimeUnit.MILLISECONDS);
            if(ObjectUtils.isEmpty(msgId)){
                String sn = getSn(GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR);
                msgId = GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix + sn;
            }
            StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneReady(msgType, msgId, businessSceneKey,null);
            RedisCommonUtil.leftPush(redisTemplate,BusinessSceneConstants.SELF_STREAM_BUSINESS_LISTS+businessSceneKey,objectStreamBusinessSceneResp);
            redisDelayQueuesUtil.addDelayQueue(objectStreamBusinessSceneResp, 30, TimeUnit.SECONDS,businessSceneKey);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","缓存添加执行失败",businessSceneKey,e);
        }
        return aBoolean;

    }

}
