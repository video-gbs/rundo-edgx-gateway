package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.dto.GatewayBindMedia;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayBindReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.UserSetting;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.service.IGatewayBaseService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class IGatewayBaseServiceImpl implements IGatewayBaseService {
    @Autowired
    private IRedisCatchStorageService iRedisCatchStorageService;
    @Value("${gateway-info.serialNum}")
    private String serialNum;

    @Value("${gateway-info.expire}")
    private int expire;

    @Autowired
    RedissonClient redissonClient;
    @Autowired
    UserSetting userSetting;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;


    @Value("${mdeia-api-uri-list.gateway-bind}")
    private String gatewayBind;

    @Autowired
    GatewaySignInConf gatewaySignInConf;



    @Autowired
    private RestTemplate restTemplate;
    @Override
    public void gatewayBindMedia(GatewayBindMedia gatewayBindMedia) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "绑定调度服务", "开始处理", gatewayBindMedia);
        String businessSceneKey = GatewayMsgType.GATEWAY_BIND_MEDIA.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+gatewayBindMedia.getSerialNum();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.GATEWAY_BIND_MEDIA,gatewayBindMedia.getMsgId(),userSetting.getBusinessSceneTimeout());
            RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);

            if(ObjectUtils.isEmpty(gatewayBindMedia)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.GATEWAY_BIND_MEDIA, BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR,null);

            }else {
                //缓存绑定信息 一个网关只有一个绑定的调度服务
                RedisCommonUtil.set(redisTemplate,BusinessSceneConstants.BIND_GATEWAY_MEDIA+serialNum,gatewayBindMedia);
                //调度服务进行网关服务的mq信息绑定
                GatewayBindReq gatewayBindReq = new GatewayBindReq();
                gatewayBindReq.setGatewayId(gatewaySignInConf.getGatewayId());
                gatewayBindReq.setMqExchange(gatewaySignInConf.getMqExchange());
                gatewayBindReq.setMqRouteKey(gatewaySignInConf.getMqGetQueue());
                gatewayBindReq.setMqQueueName(gatewaySignInConf.getMqGetQueue());
                //进行网关业务队列与流媒体的绑定接口绑定
                CommonResponse commonResponse = RestTemplateUtil.postReturnCommonrespons(gatewayBindMedia.getUrl() + gatewayBind, gatewayBindReq, restTemplate);
                if(commonResponse.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
                    log.error(LogTemplate.ERROR_LOG_TEMPLATE, "媒体调度服务", "创建绑定信息失败，将导致点播服务异常", commonResponse);
                    //绑定失败  调度服务绑定异常
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.GATEWAY_BIND_MEDIA, BusinessErrorEnums.BIND_GATEWAY_ERROR,null);
                }

                //绑定成功
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.GATEWAY_BIND_MEDIA, BusinessErrorEnums.SUCCESS,null);

            }

        }catch (Exception e){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.GATEWAY_BIND_MEDIA, BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION,null);

        }
    }
}
