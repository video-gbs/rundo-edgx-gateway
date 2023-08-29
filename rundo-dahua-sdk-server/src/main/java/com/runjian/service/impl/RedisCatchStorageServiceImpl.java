package com.runjian.service.impl;

import com.runjian.common.constant.VideoManagerConstants;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.UserSetting;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

@Service
@Slf4j
public class RedisCatchStorageServiceImpl implements IRedisCatchStorageService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${gateway-info.serialNum}")
    private String serialNum;




    @Autowired
    UserSetting userSetting;
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


//    @Override
//    public void editBusinessSceneKey(String businessSceneKey,GatewayMsgType gatewayMsgType, BusinessErrorEnums businessErrorEnums,Object data) {
//
//        String businessSceneString = (String) RedisCommonUtil.hget(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey);
//        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"业务消息修改修改",businessSceneKey);
//        if(ObjectUtils.isEmpty(businessSceneString)){
//            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"处理网关业务状态","处理失败,对应的业务缓存不存在",businessSceneKey);
//            return;
//        }
//        //其中data的数据格式为arraylist
//        List<BusinessSceneResp> businessSceneRespList = JSONObject.parseArray(businessSceneString, BusinessSceneResp.class);
//        ArrayList<BusinessSceneResp> businessSceneRespArrayListNew = new ArrayList<>();
//        for (BusinessSceneResp businessSceneResp : businessSceneRespList) {
//            //把其中全部的请求状态修改成一致
//            BusinessSceneResp objectBusinessSceneResp = businessSceneResp.addThisSceneEnd(gatewayMsgType,businessErrorEnums, businessSceneResp,data);
//            businessSceneRespArrayListNew.add(objectBusinessSceneResp);
//        }
//        RedisCommonUtil.hset(redisTemplate,BusinessSceneConstants.ALL_SCENE_HASH_KEY,businessSceneKey,businessSceneRespArrayListNew);
//    }
//
//    @Override
//    public void addBusinessSceneKey(String businessSceneKey, GatewayMsgType gatewayMsgType, String msgId) {
//        BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(gatewayMsgType,msgId,userSetting.getBusinessSceneTimeout(),null);
//        ArrayList<BusinessSceneResp> businessSceneRespArrayList = new ArrayList<>();
//        businessSceneRespArrayList.add(objectBusinessSceneResp);
//        RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, businessSceneRespArrayList);
//    }
//
//    @Override
//    public BusinessSceneResp getOneBusinessSceneKey(String businessSceneKey) {
//        String businessSceneString = (String) RedisCommonUtil.hget(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey);
//        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"业务消息获取",businessSceneKey);
//        if(ObjectUtils.isEmpty(businessSceneString)){
//            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"业务消息获取","处理失败,对应的业务缓存不存在",businessSceneKey);
//            return null;
//        }
//        //其中data的数据格式为arraylist
//        List<BusinessSceneResp> businessSceneRespList = JSONObject.parseArray(businessSceneString, BusinessSceneResp.class);
//        return businessSceneRespList.get(0);
//    }
}
