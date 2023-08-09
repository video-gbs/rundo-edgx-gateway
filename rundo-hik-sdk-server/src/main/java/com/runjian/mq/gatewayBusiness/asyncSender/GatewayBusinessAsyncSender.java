package com.runjian.mq.gatewayBusiness.asyncSender;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
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
    RedisTemplate redisTemplate;

    private ConcurrentLinkedQueue<CommonResponse> taskQueue = new ConcurrentLinkedQueue<>();

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    //全消息处理
    @Async("taskExecutor")
    public  void sendforAllScene(CommonResponse businessSceneResp, String msgId,GatewayBusinessMsgType gatewayBusinessMsgType) {
        //判断业务网关是否初始化
        taskQueue.offer(businessSceneResp);
        //业务网关未初始化 阻塞进行等待
        while (!ObjectUtils.isEmpty(gatewaySignInConf.getMqSetQueue())) {
            CommonResponse businessCommonResponsePoll = taskQueue.poll();
            if (!ObjectUtils.isEmpty(businessCommonResponsePoll)) {
                CommonMqDto mqInfo = redisCatchStorageService.getMqInfo(gatewayBusinessMsgType.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix, msgId);
                mqInfo.setData(businessCommonResponsePoll.getData());
                mqInfo.setCode(businessCommonResponsePoll.getCode());
                mqInfo.setMsg(businessCommonResponsePoll.getMsg());
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送处理", mqInfo);
                rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), gatewaySignInConf.getMqSetQueue(), UuidUtil.toUuid(), mqInfo, true);

            } else {
                //退出阻塞
                break;
            }

        }
    }



}

