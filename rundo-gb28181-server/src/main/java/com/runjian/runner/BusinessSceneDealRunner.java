package com.runjian.runner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.BusinessSceneStatusEnum;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * @author chenjialing
 */
@Component
@Order(value = 0)
public class BusinessSceneDealRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Async
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //获取hashmap中的

        while (true){
            Map<String, Object> allBusinessMap = RedisCommonUtil.hmget(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY);
            if(CollectionUtils.isEmpty(allBusinessMap)){

                Thread.sleep(50);
            }
            Set<Map.Entry<String, Object>> entries = allBusinessMap.entrySet();
            for(Map.Entry entry:  entries){
                //获取key与value  value为BusinessSceneResp
                BusinessSceneResp businessSceneResp = JSONObject.parseObject((String) entry.getValue(), BusinessSceneResp.class);
                GatewayMsgType gatewayMsgType = businessSceneResp.getGatewayMsgType();
                //判断状态是否结束，以及是否信令跟踪超时
                LocalDateTime time = businessSceneResp.getTime();
                BusinessSceneStatusEnum statusEnum =businessSceneResp.getStatus();

                LocalDateTime now = LocalDateTime.now();
                String entrykey = (String)entry.getKey();
                if(time.isBefore(now) || statusEnum.equals(BusinessSceneStatusEnum.end)){
                    //消息跟踪完毕 删除指定的键值 异步处理对应的mq消息发送,并释放相应的redisson锁
                    //释放全局redisson锁
                    long threadId = businessSceneResp.getThreadId();
                    RLock lock = redissonClient.getLock(entrykey);
                    lock.unlockAsync(threadId);
                    //删除跟踪完毕的消息
                    RedisCommonUtil.hdel(redisTemplate,BusinessSceneConstants.ALL_SCENE_HASH_KEY,entrykey);
                    //异步处理消息的mq发送
                    gatewayBusinessAsyncSender.sendforAllScene(businessSceneResp);

                }
            }
            Thread.yield();

        }

    }
}
