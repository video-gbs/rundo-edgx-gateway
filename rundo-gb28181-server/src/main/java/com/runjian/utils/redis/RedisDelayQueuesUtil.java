package com.runjian.utils.redis;

import com.runjian.common.constant.LogTemplate;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjialing
 */
@Slf4j
@Order(0)
@Configuration(value = "redisDelayQueuesUtil")
//@ConditionalOnBean({RedissonClient.class})
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
        } catch (Exception e) {
            log.error("(添加延时队列失败) {}", e.getMessage());
            throw new RuntimeException("(添加延时队列失败)");
        }
    }

    public boolean checkDelayQueueExist(String queueCode) {
        synchronized (queueCode){
            RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            boolean empty = delayedQueue.isEmpty();
            return empty;
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
    public synchronized  <T> void addQueueList(String queueCode,T value)  {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        blockingDeque.addLast(value);
    }

    public <T> T getLastQueue(String queueCode)  {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        T value = (T) blockingDeque.pollLast();
        return value;
    }

    public void clearQueue(String queueCode)  {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        blockingDeque.clear();
    }

    public <T> T getDelayQueueHold(String queueCode) throws InterruptedException {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        boolean empty1 = delayedQueue.isEmpty();
        T value = (T) blockingDeque.takeLast();
        return value;
    }
    /**
     * 获取延迟队列
     *
     * @param queueCode
     * @param <T>
     * @return
     * @throws InterruptedException
     */
    public <T> T getDelayQueue(String queueCode)  {
        T value = null;
        try {
            RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
            RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
            value = (T) blockingDeque.poll();
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE,"延迟队列获取异常",queueCode,e);
        }

        return value;
    }
    /**
     * 删除延时队列
     *
     * @param queueCode
     * @return
     * @throws InterruptedException
     */
    public synchronized <T> T remove(String queueCode) {
        RBlockingDeque<Object> blockingDeque = redissonClient.getBlockingDeque(queueCode);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        T value = null;
        if(!ObjectUtils.isEmpty(delayedQueue)){
            value = (T) delayedQueue.remove();

        }else {
            log.info("队列移除失败:"+queueCode);
        }
        return value;
    }
}
