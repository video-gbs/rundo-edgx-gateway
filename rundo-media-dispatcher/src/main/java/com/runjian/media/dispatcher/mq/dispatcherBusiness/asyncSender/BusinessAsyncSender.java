package com.runjian.media.dispatcher.mq.dispatcherBusiness.asyncSender;

import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.GatewayCacheConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.StreamBusinessMsgType;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.media.dispatcher.conf.mq.DispatcherSignInConf;
import com.runjian.media.dispatcher.service.IRedisCatchStorageService;
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
public class BusinessAsyncSender {

    @Autowired
    RabbitMqSender rabbitMqSender;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    DispatcherSignInConf dispatcherSignInConf;



    @Autowired
    RedisTemplate redisTemplate;


    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    //全消息处理
    public void sendforAllScene(StreamBusinessSceneResp businessSceneResp){
        //先进先出，处理消息队列未能发送失败的场景
        taskExecutor.execute(()->{
            StreamBusinessMsgType gatewayMsgType = businessSceneResp.getMsgType();
            String msgId = businessSceneResp.getMsgId();
            CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(gatewayMsgType.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix, msgId);
            mqInfo.setData(businessSceneResp.getData());
            mqInfo.setCode(businessSceneResp.getCode());
            mqInfo.setMsg(businessSceneResp.getMsg());
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送处理", businessSceneResp);
             String mqGetQueue = dispatcherSignInConf.getMqSetQueue();
            rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(), mqInfo, true);
        });
    }




}

