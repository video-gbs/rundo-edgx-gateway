package com.runjian.mq.gatewayBusiness.asyncSender;

import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.common.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

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

    //全消息处理
    public void sendforAllScene(BusinessSceneResp businessSceneResp){
        //先进先出，处理消息队列未能发送失败的场景
        taskQueue.offer(businessSceneResp);
        String mqGetQueue = gatewaySignInConf.getMqSetQueue();
        if(ObjectUtils.isEmpty(mqGetQueue)){
            //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送失败，业务队列暂时未初始化", businessSceneResp);
            return;
        }
        taskExecutor.execute(()->{
            while (!taskQueue.isEmpty()){
                BusinessSceneResp businessSceneRespPoll = taskQueue.poll();
                GatewayMsgType gatewayMsgType = businessSceneRespPoll.getGatewayMsgType();
                String msgId = businessSceneRespPoll.getMsgId();
                CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(gatewayMsgType.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
                mqInfo.setData(businessSceneRespPoll.getData());
                mqInfo.setCode(businessSceneRespPoll.getCode());
                mqInfo.setMsg(businessSceneResp.getMsg());
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送处理", businessSceneResp);
                rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),mqInfo,true);
            }
        });
    }




}

