package com.runjian.mq.gatewayBusiness.asyncSender;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.common.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author chenjialing
 */
@Component
@Slf4j
public class GatewayBusinessAsyncSender {

    @Autowired
    RabbitMqSender rabbitMqSender;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    @Autowired
    CatalogDataCatch catalogDataCatch;

    @Autowired
    RedisTemplate redisTemplate;

    private ConcurrentLinkedQueue<BusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Value("${mdeia-api-uri-list.stream-notify}")
    private String streamNotifyApi;
    @Autowired
    RestTemplate restTemplate;
    //全消息处理
    public void sendforAllScene(BusinessSceneResp businessSceneResp,String businessSceneKey){
        //先进先出，处理消息队列未能发送失败的场景
        taskQueue.offer(businessSceneResp);
        String mqGetQueue = gatewaySignInConf.getMqSetQueue();
        if(ObjectUtils.isEmpty(mqGetQueue)){
            //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送失败，业务队列暂时未初始化", businessSceneResp);
            return;
        }
        taskExecutor.execute(()->{
                BusinessSceneResp businessSceneRespPoll = taskQueue.poll();
                if(ObjectUtils.isEmpty(businessSceneRespPoll)){
                    return;
                }
                GatewayMsgType gatewayMsgType = businessSceneRespPoll.getGatewayMsgType();
                String msgId = businessSceneRespPoll.getMsgId();
                CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(gatewayMsgType.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
                mqInfo.setData(businessSceneRespPoll.getData());
                mqInfo.setCode(businessSceneRespPoll.getCode());
                mqInfo.setMsg(businessSceneResp.getMsg());
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送处理", mqInfo);
                //针对点播和回放的通知 仅通知调度服务
                if(gatewayMsgType.equals(GatewayMsgType.PLAY_BACK) || gatewayMsgType.equals(GatewayMsgType.PLAY)){
                    //restfulapi请求 分离请求中的streamId
                    String streamId = businessSceneKey.substring(businessSceneKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY)+1);
                    GatewayStreamNotify gatewayStreamNotify = new GatewayStreamNotify();
                    gatewayStreamNotify.setStreamId(streamId);
                    gatewayStreamNotify.setBusinessSceneResp(businessSceneResp);
                    //获取实体中的设备数据 转换为playreq
                    //设备信息同步  获取设备信息
                    PlayReq playReq = JSONObject.toJavaObject((JSONObject)businessSceneResp.getData(), PlayReq.class);
                    CommonResponse<Boolean> booleanCommonResponse = RestTemplateUtil.postStreamNotifyRespons(playReq.getDispatchUrl()+streamNotifyApi, gatewayStreamNotify, restTemplate);
                    log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-http请求发送", booleanCommonResponse);

                }else {
                    rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),mqInfo,true);
                }
        });
    }




}

