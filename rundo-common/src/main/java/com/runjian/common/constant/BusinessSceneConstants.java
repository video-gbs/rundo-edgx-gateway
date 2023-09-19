package com.runjian.common.constant;

/**
 * 业务场景中的常量值ss
 * @author chenjialing
 */
public class BusinessSceneConstants {
    /**
     * 网关缓存key的全部场景值
     */
    public static final String  GATEWAY_BUSINESS_KEY= "gateway_business_keys:";
    public static final String  GATEWAY_BUSINESS_LISTS= "gateway_business_lists:";

    /**
     * 调度服务 缓存key的全部场景值
     */
    public static final String  STREAM_BUSINESS_KEY= "stream_business_keys:";
    public static final String  STREAM_BUSINESS_LISTS= "stream_business_lists:";
//    public static final String  DISPATCHER_ALL_SCENE_HASH_KEY= "stream_business_keys:";

    //redisson的lock前缀
    public static final String  BUSINESS_LOCK_KEY = "redisson_lock";
    /**
     * 自研调度服务 缓存key的全部场景值
     */
    public static final String  SELF_BUSINESS_LOCK_KEY = "SELF_BUSINESS_LOCK_KEY:";
    public static final String  SELF_STREAM_BUSINESS_KEY= "SELF_STREAM_BUSINESS_KEY:";
    public static final String  SELF_STREAM_BUSINESS_LISTS= "SELF_STREAM_BUSINESS_LISTS:";

    /**
     * alarm的告警
     */
    public static final String  ALARM_BUSINESS = "ALARM_BUSINESS:";
    public static final String  ALARM_BUSINESS_LIST = "ALARM_BUSINESS_LIST:";
    /*
     * 设备信息缓存键
     */
    public static final String  DEVICE_INFO_SCENE_KEY= "device_info_scene_key:";

    public static final String  SCENE_SEM_KEY= ":";

    public static final String  SCENE_STREAM_KEY= "-";

    public static final String  SCENE_STREAM_SPLICE_KEY= "_";

    /**
     * zset的key的全部场景值
     */
    public static final String  BIND_GATEWAY_MEDIA = "bind_gateway_media:";
}
