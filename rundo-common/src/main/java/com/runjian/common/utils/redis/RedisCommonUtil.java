package com.runjian.common.utils.redis;

import com.runjian.common.constant.DownloadStreamInfoDto;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.constant.MarkConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**    
 * Redis工具类
 * @author swwheihei
 * @date 2020年5月6日 下午8:27:29
 */
@Slf4j
@SuppressWarnings(value = {"rawtypes", "unchecked"})
public class RedisCommonUtil {



    /**
     * 递增
     * @param key 键
     * @param delta 递增大小
     * @return
     */
    public static long incr(String key, long delta,RedisTemplate redisTemplate) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

	/**
     * 指定缓存失效时间
     * @param key 键
     * @param time 时间（秒）
     * @return true / false
     */
    public static boolean expire(RedisTemplate redisTemplate,String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "局部异常捕获", "error","redis.error", e);
            return false;
        }
    }


    /**
     * 删除缓存
     * @SuppressWarnings("unchecked") 忽略类型转换警告
     * @param key 键（一个或者多个）
     */
    public static boolean del(RedisTemplate redisTemplate,String... key) {

    	try {
    		if (key != null && key.length > 0) {
                if (key.length == 1) {
                    redisTemplate.delete(key[0]);
                } else {
//                    传入一个 Collection<String> 集合
                    redisTemplate.delete(CollectionUtils.arrayToList(key));
                }
            }
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "局部异常捕获", "error","redis.error", e);
            return false;
        }
    }

//    ============================== String ==============================

    /**
     * 普通缓存获取
     * @param key 键
     * @return 值
     */
    public static Object get(RedisTemplate redisTemplate,String key) {

        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true / false
     */
    public static boolean set(RedisTemplate redisTemplate,String key, Object value) {

        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "局部异常捕获", "error","redis.error", e);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key 键
     * @param value 值
     * @param time 时间（秒），如果 time < 0 则设置无限时间
     * @return true / false
     */
    public static boolean set(RedisTemplate redisTemplate,String key, Object value, long time) {

        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                set(redisTemplate,key, value);
            }
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "局部异常捕获", "error","redis.error", e);
            return false;
        }
    }






    /**
     * 模糊查询
     * @param query 查询参数
     * @return
     */
    public static List<Object> scan(RedisTemplate redisTemplate,String query) {
        Set<String> resultKeys = (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            ScanOptions scanOptions = ScanOptions.scanOptions().match("*" + query + "*").count(1000).build();
            Cursor<byte[]> scan = connection.scan(scanOptions);
            Set<String> keys = new HashSet<>();
            while (scan.hasNext()) {
                byte[] next = scan.next();
                keys.add(new String(next));
            }
            return keys;
        });

        return new ArrayList<>(resultKeys);
    }


}
