package com.runjian.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.SipConfig;
import com.runjian.conf.SsrcConfig;
import com.runjian.conf.UserSetting;
import com.runjian.dao.GatewayTaskMapper;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
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
    RedissonClient redissonClient;

    @Autowired
    GatewayInfoConf gatewayInfoConf;
    /**
     * 定时任务-网关心跳处理
     */
    @Override
    @Scheduled(fixedRate = 1000)
    public void msgExpireRoutine() {
        Set<String> businessKeys = msgIdArray.pullAndNext();
        //过期数据处理
        if(!ObjectUtils.isEmpty(businessKeys)){
            List<GatewayTask> listByBusinessKey = gatewayTaskMapper.getListByBusinessKey(businessKeys);
            if(!ObjectUtils.isEmpty(listByBusinessKey)){
                ConcurrentLinkedQueue<GatewayBusinessSceneResp> taskQueue = gatewayInfoConf.getTaskQueue();
                for (GatewayTask gatewayTask : listByBusinessKey) {
                    GatewayBusinessMsgType typeName = GatewayBusinessMsgType.getTypeName(gatewayTask.getMsgType());
                    GatewayBusinessSceneResp<Object> objectGatewayBusinessSceneResp = GatewayBusinessSceneResp.addSceneTimeout(typeName, BusinessErrorEnums.MSG_OPERATION_TIMEOUT,gatewayTask.getBusinessKey(), null);

                    taskQueue.offer(objectGatewayBusinessSceneResp);
                }
            }else {
                log.error(LogTemplate.ERROR_LOG_TEMPLATE,"过期消息数据异常","内存与db数据不一致",businessKeys);
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
    public  void editBusinessSceneKey(String businessSceneKey,GatewayBusinessMsgType gatewayBusinessMsgType, BusinessErrorEnums businessErrorEnums,Object data) {
        try {
            //待过期数据剔除
            msgIdArray.deleteTime(businessSceneKey);
            GatewayBusinessSceneResp<Object> objectGatewayBusinessSceneResp = GatewayBusinessSceneResp.addSceneEnd(gatewayBusinessMsgType, businessErrorEnums,businessSceneKey, data);
            ConcurrentLinkedQueue<GatewayBusinessSceneResp> taskQueue = gatewayInfoConf.getTaskQueue();
            taskQueue.offer(objectGatewayBusinessSceneResp);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","数据执行失败",businessSceneKey,e);
        }

    }

    @Override
    public  Boolean addBusinessSceneKey(String businessSceneKey, GatewayBusinessMsgType gatewayBusinessMsgType, String msgId) {
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
            GatewayBusinessSceneResp<Object> objectGatewayBusinessSceneResp = GatewayBusinessSceneResp.addSceneReady(gatewayBusinessMsgType, msgId, businessSceneKey,null);
            RedisCommonUtil.leftPush(redisTemplate,BusinessSceneConstants.GATEWAY_BUSINESS_LISTS+businessSceneKey,objectGatewayBusinessSceneResp);
            if(aBoolean){
                //待过期数据整理
                msgIdArray.addOrUpdateTime(businessSceneKey,userSetting.getBusinessSceneTimeout().longValue());

                //消息链路的数据库记录
                GatewayTask gatewayTask = new GatewayTask();
                gatewayTask.setMsgId(msgId);
                gatewayTask.setBusinessKey(businessSceneKey);
                gatewayTask.setCode(objectGatewayBusinessSceneResp.getCode());

                gatewayTask.setMsg(objectGatewayBusinessSceneResp.getMsg());
                gatewayTask.setMsgType(gatewayBusinessMsgType.getTypeName());
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
