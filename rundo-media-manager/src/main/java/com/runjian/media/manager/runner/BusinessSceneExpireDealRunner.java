package com.runjian.media.manager.runner;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.StreamBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.media.manager.mq.dispatcherBusiness.asyncSender.BusinessAsyncSender;
import com.runjian.media.manager.service.IMediaPlayService;
import com.runjian.media.manager.utils.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author chenjialing
 */
@Component
@Slf4j
@Order(value = 2)
public class BusinessSceneExpireDealRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    BusinessAsyncSender businessAsyncSender;


    @Autowired
    IMediaPlayService mediaPlayService;

    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;

    @Async
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //获取hashmap中的

                while (true) {
                    try {
                        Set<String> keys = RedisCommonUtil.keys(redisTemplate, BusinessSceneConstants.SELF_STREAM_BUSINESS_LISTS + "*");
                        if(!ObjectUtils.isEmpty(keys)){
                            for(String bKey : keys){
                                String businessKey = bKey.substring(bKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY) + 1);
                                Object delayQueue = redisDelayQueuesUtil.getDelayQueue(businessKey);
                                if(!ObjectUtils.isEmpty(delayQueue)){
                                    StreamBusinessSceneResp streamBusinessSceneResp = (StreamBusinessSceneResp) delayQueue;
                                    businessAsyncSender.sendforAllScene(streamBusinessSceneResp, BusinessErrorEnums.MSG_OPERATION_TIMEOUT,null);
                                    RLock lock = redissonClient.getLock( BusinessSceneConstants.SELF_BUSINESS_LOCK_KEY+streamBusinessSceneResp.getBusinessSceneKey());
                                    if(!ObjectUtils.isEmpty(lock)){
                                        lock.unlockAsync(streamBusinessSceneResp.getThreadId());
                                    }
                                }
                            }

                        }
                    } catch (Exception e) {
                        log.error("(Redis延迟队列异常中断) {}", e.getMessage());
                    }
                }

    }

}
