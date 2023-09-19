package com.runjian.gb28181.session;
import com.alibaba.fastjson.JSON;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.gb28181.bean.*;
import com.runjian.service.IDeviceAlarmService;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author chenjialing
 */
@Component
@Slf4j
public class DeviceAlarmCatch {

    public static Map<String, ConcurrentLinkedQueue<DeviceAlarm>> data2 = new ConcurrentHashMap<>();

    @Autowired
    IDeviceAlarmService deviceAlarmService;

    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;
    /**
     * 聚合过期时间
     */
    @Value("${AlarmConfig.polymerization-expire:1}")
    int polymerizationExpire;


    public synchronized void addReady(DeviceAlarm deviceAlarm) {
        String alarmType = deviceAlarm.getAlarmType();
        String alarmKey = deviceAlarm.getDeviceId()+ BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY +deviceAlarm.getChannelId()+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+deviceAlarm.getAlarmMethod();
        if(ObjectUtils.isEmpty(alarmType)){
            alarmKey = alarmKey+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+alarmType;
        }
        final String alarmDelayKey = BusinessSceneConstants.ALARM_BUSINESS+alarmKey;
        final String alarmListKey = BusinessSceneConstants.ALARM_BUSINESS_LIST+alarmKey;

        if(redisDelayQueuesUtil.checkDelayQueueExist(alarmDelayKey)){
            redisDelayQueuesUtil.addDelayQueue(deviceAlarm, polymerizationExpire, TimeUnit.SECONDS,alarmDelayKey);
            Thread thread = new Thread(() -> {
                while (true){
                    DeviceAlarm delayQueueOne = redisDelayQueuesUtil.getDelayQueue(alarmDelayKey);
                    if(ObjectUtils.isEmpty(delayQueueOne)){
                        continue;
                    }else {
                        DeviceAlarm alarmListOne = redisDelayQueuesUtil.getLastQueue(alarmListKey);
                        if(ObjectUtils.isEmpty(alarmListOne)){
                            //只有一条告警数据
                            log.info("开始:"+ JSON.toJSONString(delayQueueOne)+",结束："+JSON.toJSONString(delayQueueOne));
                            break;
                        }else {
                            //清理已有的告警数据
                            redisDelayQueuesUtil.clearQueue(alarmListKey);
                            //聚合最后的告警信息，其他的丢掉
                            log.info("开始:"+ JSON.toJSONString(delayQueueOne)+",结束："+JSON.toJSONString(alarmListOne));
                            break;
                        }

                    }
                }
            });
            thread.start();
        }else {
            redisDelayQueuesUtil.addQueueList(alarmListKey,deviceAlarm);
        }




    }

    public synchronized void push(DeviceAlarm deviceAlarm) {

    }

}
