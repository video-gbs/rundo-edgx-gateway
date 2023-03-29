package com.runjian.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.SipConfig;
import com.runjian.conf.SsrcConfig;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public void editBusinessSceneKey(String businessSceneKey,GatewayMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums,Object data) {

        String businessSceneString = (String) RedisCommonUtil.hget(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey);
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"业务消息修改修改",businessSceneKey);
        if(ObjectUtils.isEmpty(businessSceneString)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","处理失败,对应的业务缓存不存在",businessSceneKey);
            return;
        }
        //其中data的数据格式为arraylist
        List<BusinessSceneResp> businessSceneRespList = JSONObject.parseArray(businessSceneString, BusinessSceneResp.class);
        ArrayList<BusinessSceneResp> businessSceneRespArrayListNew = new ArrayList<>();
        for (BusinessSceneResp businessSceneResp : businessSceneRespList) {
            //把其中全部的请求状态修改成一致
            BusinessSceneResp objectBusinessSceneResp = businessSceneResp.addThisSceneEnd(GatewayMsgType.DEVICE_TOTAL_SYNC,BusinessErrorEnums.SIP_SEND_EXCEPTION, businessSceneResp,data);
            businessSceneRespArrayListNew.add(objectBusinessSceneResp);
        }
        RedisCommonUtil.hset(redisTemplate,BusinessSceneConstants.ALL_SCENE_HASH_KEY,businessSceneKey,businessSceneRespArrayListNew);
    }
}
