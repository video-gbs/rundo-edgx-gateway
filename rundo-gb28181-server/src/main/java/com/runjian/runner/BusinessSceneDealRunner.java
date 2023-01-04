package com.runjian.runner;

import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.utils.redis.RedisCommonUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

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
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //获取hashmap中的
        Map<Object, Object> hmget = RedisCommonUtil.hmget(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY);


    }
}
