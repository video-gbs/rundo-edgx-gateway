package com.runjian.media.dispatcher.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.conf.UserSetting;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
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

    @Value("${dispatcher-info.serialNum}")
    private String serialNum;

    @Autowired
    private UserSetting userSetting;


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
    public void editBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums, Object data) {
        String businessSceneString = (String) RedisCommonUtil.hget(redisTemplate, BusinessSceneConstants.DISPATCHER_ALL_SCENE_HASH_KEY, businessSceneKey);
        if(ObjectUtils.isEmpty(businessSceneString)){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理调度服务业务状态","处理失败,对应的业务缓存不存在",businessSceneKey);
            return;
        }
        BusinessSceneResp businessSceneResp = JSONObject.parseObject(businessSceneString, BusinessSceneResp.class);
        BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneEnd(gatewayMsgType, businessErrorEnums, businessSceneResp.getMsgId(),businessSceneResp.getThreadId(),businessSceneResp.getTime(),data);
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"业务消息修改修改",objectBusinessSceneResp);
        RedisCommonUtil.hset(redisTemplate,BusinessSceneConstants.DISPATCHER_ALL_SCENE_HASH_KEY,businessSceneKey,objectBusinessSceneResp);
    }


    @Override
    public void addBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, String msgId) {
        BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(gatewayMsgType,msgId,userSetting.getBusinessSceneTimeout(),null);
        RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.DISPATCHER_ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
    }
}
