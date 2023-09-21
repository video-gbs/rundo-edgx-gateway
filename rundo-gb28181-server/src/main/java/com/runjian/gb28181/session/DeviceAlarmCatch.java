package com.runjian.gb28181.session;
import com.alibaba.fastjson.JSON;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.utils.DateUtils;
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


    public  void addReady(DeviceAlarm deviceAlarm) {
        String alarmType = deviceAlarm.getAlarmType();
        String alarmKey = deviceAlarm.getDeviceId()+ BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY +deviceAlarm.getChannelId()+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+deviceAlarm.getAlarmMethod();
        if(!ObjectUtils.isEmpty(alarmType)){
            alarmKey = alarmKey+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+alarmType;
        }

        final String alarmDelayKey = BusinessSceneConstants.ALARM_BUSINESS+alarmKey;
        final String alarmHeartKey = BusinessSceneConstants.ALARM_HEART_BUSINESS+alarmKey;
        synchronized (alarmDelayKey){
            if(redisDelayQueuesUtil.checkDelayQueueExist(alarmDelayKey)){
                //首次开始
                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, polymerizationExpire, TimeUnit.SECONDS,alarmDelayKey);
                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, 15, TimeUnit.SECONDS,alarmHeartKey);
                Thread thread = new Thread(() -> {
                    while (true){
                        DeviceAlarm delayQueueOne = redisDelayQueuesUtil.getDelayQueue(alarmDelayKey);
                        if(ObjectUtils.isEmpty(delayQueueOne)){
                            DeviceAlarm heartQueueOne = redisDelayQueuesUtil.getDelayQueue(alarmHeartKey);
                            if(!ObjectUtils.isEmpty(heartQueueOne)){
                                //发送告警的心跳
                                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, 15, TimeUnit.SECONDS,alarmHeartKey);
                                log.info("心跳："+alarmHeartKey);

                            }
                        }else {
                            log.info("结束："+JSON.toJSONString(delayQueueOne));
                            //心跳队列移除
                            redisDelayQueuesUtil.remove(alarmHeartKey);
                            break;

                        }


                    }
                });
                thread.start();
            }else {
                //首次结束数据周期内到达
                redisDelayQueuesUtil.remove(alarmDelayKey);
                //比较时间范围的
                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, polymerizationExpire, TimeUnit.SECONDS,alarmDelayKey);
            }
        }





    }

    public synchronized void push(DeviceAlarm deviceAlarm) {

    }

}
