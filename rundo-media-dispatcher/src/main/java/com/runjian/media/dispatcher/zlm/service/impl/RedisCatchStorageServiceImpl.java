package com.runjian.media.dispatcher.zlm.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.dispatcher.zlm.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class RedisCatchStorageServiceImpl implements IRedisCatchStorageService {

    @Autowired
    private RedisTemplate redisTemplate;




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
    public GatewayMqDto getMqInfo(String msgType, String snIncr, String snPrefix,String msgId,String gatewayNum) {
        GatewayMqDto gatewayMqDto = new GatewayMqDto();
        gatewayMqDto.setMsgType(msgType);
        gatewayMqDto.setTime(LocalDateTime.now());
        gatewayMqDto.setSerialNum(gatewayNum);

        String sn = getSn(snIncr);
        if(ObjectUtils.isEmpty(msgId)){
            gatewayMqDto.setMsgId(snPrefix+sn);

        }else {
            gatewayMqDto.setMsgId(msgId);

        }
        return gatewayMqDto;
    }

}
