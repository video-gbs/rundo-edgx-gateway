package com.runjian.mq.gatewayBusiness.asyncSender;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.commonDto.Gb28181Media.req.GatewayStreamNotify;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.RestTemplateUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.mq.MqMsgDealService.MqInfoCommonDto;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.common.utils.UuidUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
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

    private ConcurrentLinkedQueue<GatewayBusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Value("${mdeia-api-uri-list.stream-notify}")
    private String streamNotifyApi;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MqInfoCommonDto mqInfoCommonDto;
    @Async
    //全消息处理
    public void sendforAllScene(GatewayBusinessSceneResp businessSceneResp,BusinessErrorEnums businessErrorEnums){
        //判断业务网关是否初始化
        taskQueue.offer(businessSceneResp);
        //业务网关未初始化 阻塞进行等待
        while (!ObjectUtils.isEmpty(gatewaySignInConf.getMqExchange())){
            GatewayBusinessSceneResp businessSceneKeyPoll = taskQueue.poll();
            if(!ObjectUtils.isEmpty(businessSceneKeyPoll)){
                try {

                    //进行消息通知
                    GatewayBusinessMsgType gatewayMsgType = businessSceneKeyPoll.getGatewayMsgType();
                    String msgId = businessSceneKeyPoll.getMsgId();
                    CommonMqDto mqInfo = mqInfoCommonDto.getMqInfo(gatewayMsgType.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix, msgId);
                    mqInfo.setData(businessSceneKeyPoll.getData());
                    mqInfo.setCode(businessErrorEnums.getErrCode());
                    mqInfo.setMsg(businessErrorEnums.getErrMsg());
                    String mqGetQueue = gatewaySignInConf.getMqSetQueue();
                    log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mqN信令发送处理", mqInfo);
                    if(businessSceneKeyPoll.getSendType() == 0){
                        rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(), mqInfo, true);

                    }else {

                        PlayReq objData = (PlayReq)businessSceneKeyPoll.getData();
                        String dispatchUrl = objData.getDispatchUrl();
                        businessSceneKeyPoll.setMsg(businessErrorEnums.getErrMsg());
                        businessSceneKeyPoll.setCode(businessErrorEnums.getErrCode());
                        String resultString = RestTemplateUtil.postString(dispatchUrl, JSON.toJSONString(businessSceneKeyPoll), null, restTemplate);
                        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-http请求发送", resultString);
                    }

                }catch (Exception e){
                    log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理--异常", businessSceneResp, e);
                }


            }else {
                //退出阻塞
                break;
            }

        }
    }




}

