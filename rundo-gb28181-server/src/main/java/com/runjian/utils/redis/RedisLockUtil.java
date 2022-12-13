package com.runjian.utils.redis;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * redis分布式锁
 * @author Miracle
 * @date 2022/03/15 14:42
 */
@Component
public class RedisLockUtil {


    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 上锁
     * @param key 上锁key
     * @param value 上锁value，建议采用唯一性，用于解锁
     * @param expire 过期时间
     * @param timeUnit 过期时间单位
     * @param cycleNum 循环拿锁次数
     * @return 上锁是否成功
     */
    public boolean lock(String key, String value, long expire, TimeUnit timeUnit, int cycleNum){
        while (true){
            try{
                boolean lock = redisTemplate.opsForValue().setIfAbsent(key, value, expire, timeUnit);
                cycleNum--;
                if (lock){
                    return true;
                }else if (cycleNum > 1){
                    // 主动让出CPU
                    // Thread.yield();
                    // 线程睡眠
                    Thread.sleep(new Random().nextInt(100));
                }else if (cycleNum < 1){
                    return false;
                }
            }catch (Exception ex){
                ex.printStackTrace();
                return false;
            }
        }
    }

    public boolean lockInMap(String redisKey, String key, Object value){
        return redisTemplate.opsForHash().putIfAbsent(redisKey, key ,value);
    }

    public boolean unlockInMap(String redisKey, String key, Object value){
        try{
            // 获取分布式锁
            Object currentValue = redisTemplate.opsForHash().get(redisKey, key);
            // 这里避免解了其他人的锁，要匹配value值
            if (Objects.nonNull(currentValue) && currentValue.equals(value)){
                redisTemplate.opsForHash().delete(redisKey, key);
                return true;
            }
            return false;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    public void unlockInMapMandatory(String redisKey, String key){
        redisTemplate.opsForHash().delete(redisKey, key);
    }

    /**
     * 解锁
     * @param key 上锁时的key
     * @param value 上锁时的value
     * @return
     */
    public boolean unLock(String key, String value){
        try{
            if (checkValue(key, value)){
                redisTemplate.opsForValue().getOperations().delete(key);
                return true;
            }
            return false;
        }catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    /**
     * 检测值
     * @param key
     * @param value
     * @return
     */
    public boolean checkValue(String key, String value){
        try {
            // 获取分布式锁
            String currentValue = redisTemplate.opsForValue().get(key);
            // 这里避免解了其他人的锁，要匹配value值
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)){
                return true;
            }
            return false;
        }catch (Exception ex){
            return false;
        }

    }
}
