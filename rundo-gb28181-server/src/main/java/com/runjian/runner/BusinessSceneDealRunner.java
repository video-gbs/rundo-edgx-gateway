//package com.runjian.runner;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.runjian.common.config.exception.BusinessErrorEnums;
//import com.runjian.common.config.response.BusinessSceneResp;
//import com.runjian.common.config.response.GatewayBusinessSceneResp;
//import com.runjian.common.config.response.StreamBusinessSceneResp;
//import com.runjian.common.constant.*;
//import com.runjian.common.utils.redis.RedisCommonUtil;
//import com.runjian.conf.GatewayInfoConf;
//import com.runjian.conf.mq.GatewaySignInConf;
//import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
//import com.runjian.service.IRedisCatchStorageService;
//import com.runjian.service.IplayService;
//import com.runjian.utils.redis.RedisDelayQueuesUtil;
//import lombok.extern.slf4j.Slf4j;
//import org.redisson.Redisson;
//import org.redisson.api.RLock;
//import org.redisson.api.RReadWriteLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
//import org.springframework.stereotype.Component;
//import org.springframework.util.CollectionUtils;
//import org.springframework.util.ObjectUtils;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.TimeUnit;
//
///**
// * @author chenjialing
// */
//@Component
//@Slf4j
//@Order(value = 0)
//public class BusinessSceneDealRunner implements CommandLineRunner {
//
//    @Autowired
//    RedissonClient redissonClient;
//
//    @Autowired
//    RedisTemplate redisTemplate;
//
//    @Autowired
//    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;
//
//    @Autowired
//    IplayService iplayService;
//
//    @Autowired
//    IRedisCatchStorageService redisCatchStorageService;
//
//    @Autowired
//    GatewaySignInConf gatewaySignInConf;
//
//    @Autowired
//    GatewayInfoConf gatewayInfoConf;
//
//    @Autowired
//    RedisDelayQueuesUtil redisDelayQueuesUtil;
//
//
//    @Qualifier("taskExecutor")
//    @Autowired
//    private ThreadPoolTaskExecutor taskExecutor;
//    @Async
//    @Override
//    public void run(String... args) throws Exception {
//        while (true) {
//                taskExecutor.execute(()->{
//                    try {
//                        Set<String> keys = RedisCommonUtil.keys(redisTemplate, BusinessSceneConstants.GATEWAY_BUSINESS_LISTS + "*");
//                        if(!ObjectUtils.isEmpty(keys)){
//                            for(String bKey : keys){
//                                String businessKey = bKey.substring(bKey.indexOf(BusinessSceneConstants.SCENE_SEM_KEY) + 1);
//        //                        if(!redisDelayQueuesUtil.checkDelayQueueExist(businessKey)){
//        //                            RedisCommonUtil.del(redisTemplate,bKey);
//        //                        }
//                                Object delayQueue = redisDelayQueuesUtil.getDelayQueue(businessKey);
//                                if(!ObjectUtils.isEmpty(delayQueue)){
//                                    GatewayBusinessSceneResp businessSceneResp = (GatewayBusinessSceneResp) delayQueue;
//                                    gatewayBusinessAsyncSender.sendforAllScene(businessSceneResp, BusinessErrorEnums.MSG_OPERATION_TIMEOUT);
//                                    RLock lock = redissonClient.getLock( BusinessSceneConstants.GATEWAY_BUSINESS_LOCK_KEY+businessSceneResp.getBusinessSceneKey());
//                                    if(!ObjectUtils.isEmpty(lock)){
//                                        lock.unlockAsync(businessSceneResp.getThreadId());
//                                    }
//                                }
//                            }
//
//                        }
//                    } catch (Exception e) {
//                        log.error("(Redis延迟队列异常中断) {}", e.getMessage());
//                    }
//            });
//        }
//
//    }
//}
