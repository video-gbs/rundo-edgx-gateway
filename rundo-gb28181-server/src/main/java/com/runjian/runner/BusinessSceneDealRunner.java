package com.runjian.runner;

import com.runjian.common.utils.redis.RedisCommonUtil;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author chenjialing
 */
@Component
@Order(value = 0)
public class BusinessSceneDealRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;
    @Override
    public void run(String... args) throws Exception {
        //常驻进程检测redis hash中所有的待处理的业务场景消息
        //redisson 异步解锁必须传入线程id
        RLock asd = redissonClient.getFairLock("asd");
        boolean locked = asd.isLocked();

        asd.unlock();
        boolean b = asd.tryLock();
        asd.unlockAsync();

        long id = Thread.currentThread().getId();


    }
}
