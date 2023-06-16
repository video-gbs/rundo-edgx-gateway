package com.runjian.media.manager.mq.dispatcherBusiness.asyncSender;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.UuidUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.conf.mq.DispatcherSignInConf;
import com.runjian.media.manager.mq.MqInfoCommonDto;
import com.runjian.media.manager.service.IMediaPlayService;
import com.runjian.media.manager.service.IRedisCatchStorageService;
import com.runjian.media.manager.utils.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
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
    DispatcherSignInConf dispatcherSignInConf;

    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;

    @Autowired
    RedisTemplate redisTemplate;

    private ConcurrentLinkedQueue<StreamBusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    IMediaPlayService mediaPlayService;

    @Autowired
    MqInfoCommonDto mqInfoCommonDto;
    //全消息处理
    @Async("taskExecutor")
    public  void sendforAllScene(StreamBusinessSceneResp businessSceneResp,BusinessErrorEnums businessErrorEnums,Object data){
        //判断业务网关是否初始化
        taskQueue.offer(businessSceneResp);
        //业务网关未初始化 阻塞进行等待
        while (!ObjectUtils.isEmpty(dispatcherSignInConf.getMqExchange())){
            StreamBusinessSceneResp businessSceneKeyPoll = taskQueue.poll();
            if(!ObjectUtils.isEmpty(businessSceneKeyPoll)){

                //针对点播异常请求通知网关进行bye指令的发送
                if(businessSceneResp.getMsgType().equals(StreamBusinessMsgType.STREAM_LIVE_PLAY_START) || businessSceneResp.getMsgType().equals(StreamBusinessMsgType.STREAM_RECORD_PLAY_START)){
                    if(businessSceneResp.getCode() != BusinessErrorEnums.SUCCESS.getErrCode()){
                        //异常点播处理
                        mediaPlayService.playBusinessErrorScene(businessSceneResp);

                    }
                }
                String businessSceneKey = businessSceneResp.getBusinessSceneKey();
                while (!ObjectUtils.isEmpty(RedisCommonUtil.rangListAll(redisTemplate,BusinessSceneConstants.SELF_STREAM_BUSINESS_LISTS + businessSceneKey))){

                    StreamBusinessSceneResp oneResp = (StreamBusinessSceneResp)RedisCommonUtil.leftPop(redisTemplate, BusinessSceneConstants.SELF_STREAM_BUSINESS_LISTS + businessSceneKey);
                    //进行消息通知
                    StreamBusinessMsgType gatewayMsgType = oneResp.getMsgType();
                    String msgId = oneResp.getMsgId();
                    CommonMqDto mqInfo = mqInfoCommonDto.getMqInfo(gatewayMsgType.getTypeName(), GatewayCacheConstants.DISPATCHER_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix, msgId);
                    mqInfo.setData(data);
                    mqInfo.setCode(businessErrorEnums.getErrCode());
                    mqInfo.setMsg(businessErrorEnums.getErrMsg());
                    String mqGetQueue = dispatcherSignInConf.getMqSetQueue();
                    log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送处理", businessSceneResp);
                    rabbitMqSender.sendMsgByExchange(dispatcherSignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(), mqInfo, true);
                };


            }else {
                //退出阻塞
                break;
            }

        }



    }




}

