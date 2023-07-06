package com.runjian.media.manager.utils;

import com.runjian.common.config.response.StreamBusinessSceneResp;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjialing
 */
@Slf4j
@Configuration
@ConditionalOnBean({RedissonClient.class})
public class RedisDelayQueuesUtil {

    @Resource
    private RedissonClient redissonClient;


    /**
     * 添加延迟队列
     *
     * @param value     队列值
     * @param delay     延迟时间
     * @param timeUnit  时间单位
     * @param queueCode 队列键
     * @param <T>
     */
    public synchronized  <T> void addDelayQueue(T value, long delay, TimeUnit timeUnit, String queueCode) {
        try {
            RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            delayedQueue.offer(value, delay, timeUnit);

            long l = timeUnit.toSeconds(delay);
            log.info("(添加延时队列成功) 队列键：{}，队列值：{}，延迟时间：{}", queueCode, value,l + "秒");
        } catch (Exception e) {
            log.error("(添加延时队列失败) {}", e.getMessage());
            throw new RuntimeException("(添加延时队列失败)");
        }
    }

//    public synchronized  <T> void changeDelayQueue(T value, long delay, TimeUnit timeUnit, String queueCode) {
//        try {
//            RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
//            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
//
//            long l = timeUnit.toSeconds(delay);
//            log.info("(添加延时队列成功) 队列键：{}，队列值：{}，延迟时间：{}", queueCode, value,l + "秒");
//        } catch (Exception e) {
//            log.error("(添加延时队列失败) {}", e.getMessage());
//            throw new RuntimeException("(添加延时队列失败)");
//        }
//    }

    /**
     * 获取延迟队列
     *
     * @param queueCode
     * @param <T>
     * @return
     * @throws InterruptedException
     */
    public <T> T getDelayQueue(String queueCode) throws InterruptedException {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        T value = (T) blockingDeque.poll();
        return value;
    }
    /**
     * 删除延时队列
     *
     * @param queueCode
     * @return
     * @throws InterruptedException
     */
    public Object remove(String queueCode) throws InterruptedException {
        log.info("移除延时队列数据，req={}", queueCode);
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);

        return delayedQueue.remove();
    }
}
