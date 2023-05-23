package com.runjian.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.SipConfig;
import com.runjian.conf.SsrcConfig;
import com.runjian.conf.UserSetting;
import com.runjian.dao.GatewayTaskMapper;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    RedissonClient redissonClient;

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
    public  void editBusinessSceneKey(String businessSceneKey,GatewayBusinessMsgType GatewayBusinessMsgType, BusinessErrorEnums businessErrorEnums,Object data) {
        String redisKey =  BusinessSceneConstants.GATEWAY_BUSINESS_KEY + businessSceneKey;
        String redisLockKey =  BusinessSceneConstants.BUSINESS_LOCK_KEY+redisKey;

        RLock lock = redissonClient.getLock(redisLockKey);
        try {
            //分布式锁 进行
            lock.lock(3,  TimeUnit.SECONDS);
            GatewayBusinessSceneResp gatewayBusinessSceneResp = (GatewayBusinessSceneResp) RedisCommonUtil.get(redisTemplate, redisKey);
            if(ObjectUtils.isEmpty(gatewayBusinessSceneResp)){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","处理失败,对应的业务缓存不存在",businessSceneKey);
                return;
            }
            GatewayBusinessSceneResp<Object> objectGatewayBusinessSceneResp = GatewayBusinessSceneResp.addSceneEnd(GatewayBusinessMsgType,businessErrorEnums, gatewayBusinessSceneResp, data);
            RedisCommonUtil.set(redisTemplate,redisKey,objectGatewayBusinessSceneResp);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","缓存编辑执行失败",businessSceneKey);
        }finally {
            lock.unlock();
        }

    }

    @Override
    public  Boolean addBusinessSceneKey(String businessSceneKey, GatewayBusinessMsgType GatewayBusinessMsgType, String msgId) {
        String redisKey =  BusinessSceneConstants.GATEWAY_BUSINESS_KEY + businessSceneKey;
        String redisLockKey = BusinessSceneConstants.BUSINESS_LOCK_KEY+redisKey;
        RLock lock = redissonClient.getLock(redisLockKey);
        Boolean  aBoolean = false;
        try {
            //分布式锁 进行
            lock.lock(3,  TimeUnit.SECONDS);

            if(ObjectUtils.isEmpty(msgId)){
                String sn = getSn(GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR);
                msgId = GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix + sn;

            }
            GatewayBusinessSceneResp<Object> objectGatewayBusinessSceneResp = GatewayBusinessSceneResp.addSceneReady(GatewayBusinessMsgType, msgId, userSetting.getBusinessSceneTimeout(), null);
            aBoolean = RedisCommonUtil.setIfAbsent(redisTemplate,redisKey,objectGatewayBusinessSceneResp);
            RedisCommonUtil.leftPush(redisTemplate,BusinessSceneConstants.GATEWAY_BUSINESS_LISTS+businessSceneKey,objectGatewayBusinessSceneResp);
            if(aBoolean){
                //消息链路的数据库记录
                GatewayTask gatewayTask = new GatewayTask();
                gatewayTask.setMsgId(msgId);
                gatewayTask.setBusinessKey(businessSceneKey);
                gatewayTask.setCode(objectGatewayBusinessSceneResp.getCode());

                gatewayTask.setMsg(objectGatewayBusinessSceneResp.getMsg());
                gatewayTask.setMsgType(GatewayBusinessMsgType.getTypeName());
                gatewayTask.setStatus(0);
                gatewayTaskMapper.add(gatewayTask);
            }
            return aBoolean;
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","缓存添加执行失败",businessSceneKey,e);
        }finally {
            lock.unlock();
        }
        return aBoolean;

    }

    @Override
    public Boolean businessSceneLogDb(GatewayBusinessSceneResp businessSceneResp, List<String> msgStrings) {

        GatewayTask gatewayTask = gatewayTaskMapper.getOne(businessSceneResp.getMsgId());
        if(ObjectUtils.isEmpty(gatewayTask)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","数据库信息查询失败",businessSceneResp);
            return null;
        }
        gatewayTask.setCode(businessSceneResp.getCode());
        gatewayTask.setMsg(businessSceneResp.getMsg());
        gatewayTask.setStatus(1);
        if(!ObjectUtils.isEmpty(msgStrings)){
            gatewayTask.setMsgIdList(String.join(",", msgStrings));
        }
        gatewayTaskMapper.updateById(gatewayTask);

        return null;
    }
}
