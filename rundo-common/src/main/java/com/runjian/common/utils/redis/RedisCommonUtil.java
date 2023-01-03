package com.runjian.common.utils.redis;

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
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return true / false
     */
    public static boolean setOverWrite(RedisTemplate redisTemplate,String key, Object value) {

        try {
            redisTemplate.opsForValue().set(key, value,0);
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



    /**
     * 添加一个元素, zset与set最大的区别就是每个元素都有一个score，因此有个排序的辅助功能;  zadd
     *
     * @param key
     * @param value
     * @param score
     */
    public static Boolean zAdd(RedisTemplate redisTemplate, Object key, Object value, double score) {

        return redisTemplate.opsForZSet().add(key, value, score);
    }

    //    ============================== Map ==============================

    /**
     * HashGet
     * @param key 键（no null）
     * @param item 项（no null）
     * @return 值
     */
    public static Object hget(RedisTemplate redisTemplate,String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * 获取 key 对应的 map
     * @param key 键（no null）
     * @return 对应的多个键值
     */
    public static Map<Object, Object> hmget(RedisTemplate redisTemplate,String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * HashSet
     * @param key 键
     * @param map 值
     * @return true / false
     */
    public static boolean hmset(RedisTemplate redisTemplate,String key, Map<Object, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "Redis工具", "未知异常", e);
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 值
     * @param time 时间
     * @return true / false
     */
    public static boolean hmset(RedisTemplate redisTemplate,,String key, Map<Object, Object> map, long time) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(redisTemplate,key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "Redis工具", "未知异常", e);
            return false;
        }
    }

    /**
     * 向一张 Hash表 中放入数据，如不存在则创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true / false
     */
    public static boolean hset(RedisTemplate redisTemplate,,String key, String item, Object value) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "Redis工具", "未知异常", e);
            return false;
        }
    }

    /**
     * 向一张 Hash表 中放入数据，并设置时间，如不存在则创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 时间（如果原来的 Hash表 设置了时间，这里会覆盖）
     * @return true / false
     */
    public static boolean hset(RedisTemplate redisTemplate,,String key, String item, Object value, long time) {
        try {
            redisTemplate.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(redisTemplate,key, time);
            }
            return true;
        } catch (Exception e) {
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "Redis工具", "未知异常", e);
            return false;
        }
    }

    /**
     * 删除 Hash表 中的值
     * @param key 键
     * @param item 项（可以多个，no null）
     */
    public static void hdel(RedisTemplate redisTemplate,String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }

    /**
     * 判断 Hash表 中是否有该键的值
     * @param key 键（no null）
     * @param item 值（no null）
     * @return true / false
     */
    public static boolean hHasKey(RedisTemplate redisTemplate,String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }

    /**
     * Hash递增，如果不存在则创建一个，并把新增的值返回
     * @param key 键
     * @param item 项
     * @param by 递增大小 > 0
     * @return
     */
    public static Double hincr(RedisTemplate redisTemplate,String key, String item, Double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }

    /**
     * Hash递减
     * @param key 键
     * @param item 项
     * @param by 递减大小
     * @return
     */
    public static Double hdecr(RedisTemplate redisTemplate,String key, String item, Double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }
}
