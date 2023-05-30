package com.runjian.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.service.IplayService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjialing
 */
@Component
@Slf4j
@Order(value = 0)
public class BusinessSceneDealRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Autowired
    IplayService iplayService;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    @Autowired
    GatewayInfoConf gatewayInfoConf;
    @Async
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //获取hashmap中的

        while (true){
            try{


                ConcurrentLinkedQueue<GatewayBusinessSceneResp> taskQueue = gatewayInfoConf.getTaskQueue();
                if(!ObjectUtils.isEmpty(taskQueue)){

                    if(ObjectUtils.isEmpty(gatewaySignInConf.getMqExchange())){
                        //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
                        Thread.sleep(10);
                        continue;
                    }

                    GatewayBusinessSceneResp  gatewayBusinessSceneRespEnd = taskQueue.poll();
                    String businessSceneKey = gatewayBusinessSceneRespEnd.getBusinessSceneKey();
                    ArrayList<String> keyStrings = new ArrayList<>();
                    while (!ObjectUtils.isEmpty(RedisCommonUtil.rangListAll(redisTemplate,BusinessSceneConstants.GATEWAY_BUSINESS_LISTS + businessSceneKey))){

                        GatewayBusinessSceneResp oneResp = (GatewayBusinessSceneResp)RedisCommonUtil.leftPop(redisTemplate, BusinessSceneConstants.GATEWAY_BUSINESS_LISTS + businessSceneKey);
                        //消息汇聚聚合
                        keyStrings.add(oneResp.getMsgId());
                        oneResp.setCode(gatewayBusinessSceneRespEnd.getCode());
                        oneResp.setMsg(gatewayBusinessSceneRespEnd.getMsg());
                        oneResp.setData(gatewayBusinessSceneRespEnd.getData());
                        gatewayBusinessAsyncSender.sendforAllScene(oneResp,businessSceneKey);
                    };
                    //消息日志记录 根据消息id进行消息修改
                    redisCatchStorageService.businessSceneLogDb(gatewayBusinessSceneRespEnd,keyStrings);
                    RLock lock = redissonClient.getLock( BusinessSceneConstants.BUSINESS_LOCK_KEY+businessSceneKey);
                    if(!ObjectUtils.isEmpty(lock)){
                        lock.unlockAsync(gatewayBusinessSceneRespEnd.getThreadId());
                    }
                }else {
                    Thread.sleep(10);
                }
            }catch (Exception e){
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景常驻线程处理","异常处理失败",e);
            }
        }

    }

//    private void commonBusinessDeal(GatewayBusinessSceneResp businessSceneResp,String redisKey){
//        //删除状态值的key,修改数据库
//        RedisCommonUtil.del(redisTemplate,redisKey);
//        String redisLockKey = BusinessSceneConstants.BUSINESS_LOCK_KEY+redisKey ;
//        RLock lock = redissonClient.getLock(redisLockKey);
//        try {
//            //分布式锁 进行
//            lock.lock(3,  TimeUnit.SECONDS);
//            //进行list数据的处理
//            String businessSceneKey = redisKey.substring(BusinessSceneConstants.GATEWAY_BUSINESS_KEY.length());
//            //list集合的key值
//            String  redisListKey = BusinessSceneConstants.GATEWAY_BUSINESS_LISTS+ businessSceneKey;
//            ArrayList<String> keyStrings = new ArrayList<>();
//
//            while (!ObjectUtils.isEmpty(RedisCommonUtil.rangListAll(redisTemplate,redisListKey))){
//
//
//
//                GatewayBusinessSceneResp oneResp = (GatewayBusinessSceneResp)RedisCommonUtil.leftPop(redisTemplate, redisListKey);
//                //消息汇聚聚合
//                keyStrings.add(oneResp.getMsgId());
//                gatewayBusinessAsyncSender.sendforAllScene(oneResp,redisKey);
//            };
//            //消息日志记录 根据消息id进行消息修改
//            redisCatchStorageService.businessSceneLogDb(businessSceneResp,keyStrings);
//
//        }catch (Exception e){
//            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE,"处理网关业务状态","缓存编辑执行失败",redisKey,e);
//        }finally {
//            lock.unlock();
//        }
//
//    }
}
