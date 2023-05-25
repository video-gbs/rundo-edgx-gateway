package com.runjian.media.dispatcher.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.commonDto.Gateway.dto.SsrcConfig;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.StreamInfoConf;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.mapper.GatewayTaskMapper;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.messageinterpolation.parser.ELState;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Override
    @Scheduled(fixedRate = 10000)
    public void msgExpireRoutine() {
        LocalDateTime localDateTime = LocalDateTime.now().minusSeconds(userSetting.getBusinessSceneTimeout() / 1000);
        List<GatewayTask> listByBusinessKey = gatewayTaskMapper.getExpireListByBusinessKey(localDateTime);
        if(!ObjectUtils.isEmpty(listByBusinessKey)){
            ConcurrentLinkedQueue<StreamBusinessSceneResp> taskQueue = InfoConf.getTaskQueue();
            for (GatewayTask gatewayTask : listByBusinessKey) {
                StreamBusinessMsgType typeName = StreamBusinessMsgType.getTypeName(gatewayTask.getMsgType());
                StreamBusinessSceneResp<Object> objectGatewayBusinessSceneResp = StreamBusinessSceneResp.addSceneTimeout(typeName, BusinessErrorEnums.MSG_OPERATION_TIMEOUT,gatewayTask.getBusinessKey(), null,gatewayTask.getMsgId(),gatewayTask.getThreadId());

                taskQueue.offer(objectGatewayBusinessSceneResp);
            }
        }
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
    public Boolean ssrcRelease(String ssrc) {
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
    public synchronized   void editBusinessSceneKey(String businessSceneKey,StreamBusinessMsgType msgType, BusinessErrorEnums businessErrorEnums,Object data) {
        try {
            //待过期数据剔除
            GatewayTask oneByBusiness = gatewayTaskMapper.getOneByBusinessKey(businessSceneKey);
            if(ObjectUtils.isEmpty(oneByBusiness)){
                log.error(LogTemplate.PROCESS_LOG_TEMPLATE,"处理网关业务状态","数据已被处理",businessSceneKey);
                return;
            }
            StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneEnd(msgType, businessErrorEnums,businessSceneKey, data,oneByBusiness.getMsgId(), oneByBusiness.getThreadId());
            ConcurrentLinkedQueue<StreamBusinessSceneResp> taskQueue = InfoConf.getTaskQueue();
            taskQueue.offer(objectStreamBusinessSceneResp);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","数据执行失败",businessSceneKey,e);
        }

    }

    @Override
    public  Boolean addBusinessSceneKey(String businessSceneKey, StreamBusinessMsgType msgType, String msgId) {
        String redisLockKey = BusinessSceneConstants.BUSINESS_LOCK_KEY+businessSceneKey;
        RLock lock = redissonClient.getLock(redisLockKey);
        Boolean  aBoolean = false;
        try {
            //分布式锁 进行
            aBoolean = lock.tryLock(10, userSetting.getBusinessSceneTimeout(), TimeUnit.MILLISECONDS);
            if(ObjectUtils.isEmpty(msgId)){
                String sn = getSn(GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR);
                msgId = GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix + sn;
            }
            StreamBusinessSceneResp<Object> objectStreamBusinessSceneResp = StreamBusinessSceneResp.addSceneReady(msgType, msgId, businessSceneKey,null);
            RedisCommonUtil.leftPush(redisTemplate,BusinessSceneConstants.STREAM_BUSINESS_LISTS+businessSceneKey,objectStreamBusinessSceneResp);
            if(aBoolean){

                //消息链路的数据库记录
                GatewayTask gatewayTask = new GatewayTask();
                gatewayTask.setMsgId(msgId);
                gatewayTask.setBusinessKey(businessSceneKey);
                gatewayTask.setCode(objectStreamBusinessSceneResp.getCode());

                gatewayTask.setMsg(objectStreamBusinessSceneResp.getMsg());
                gatewayTask.setMsgType(msgType.getTypeName());
                gatewayTask.setStatus(0);
                gatewayTask.setThreadId(objectStreamBusinessSceneResp.getThreadId());
                gatewayTaskMapper.add(gatewayTask);
            }
            return aBoolean;
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","缓存添加执行失败",businessSceneKey,e);
        }
        return aBoolean;

    }

    @Override
    public Boolean businessSceneLogDb(StreamBusinessSceneResp businessSceneResp, List<String> msgStrings) {

        GatewayTask gatewayTask = gatewayTaskMapper.getOneByBusinessKey(businessSceneResp.getBusinessSceneKey());
        if(ObjectUtils.isEmpty(gatewayTask)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","数据库信息查询失败",businessSceneResp);
            return null;
        }
        gatewayTask.setCode(businessSceneResp.getCode());
        gatewayTask.setMsg(businessSceneResp.getMsg());
        gatewayTask.setStatus(businessSceneResp.getStatus().getCode());
        if(!ObjectUtils.isEmpty(msgStrings)){
            gatewayTask.setMsgIdList(String.join(",", msgStrings));
        }
        gatewayTaskMapper.updateById(gatewayTask);

        return null;
    }
}
