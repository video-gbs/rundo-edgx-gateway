package com.runjian.gb28181.session;
import com.runjian.gb28181.bean.*;
import com.runjian.service.IDeviceAlarmService;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class DeviceAlarmCatch {

    public static Map<String, DeviceAlarm> data = new ConcurrentHashMap<>();

    @Autowired
    IDeviceAlarmService deviceAlarmService;

    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;
    /**
     * 聚合过期时间
     */
    @Value("${AlarmConfig.polymerization-expire:2}")
    int polymerizationExpire;


    public synchronized void addReady(DeviceAlarm deviceAlarm) {
        String alarmType = deviceAlarm.getAlarmType();
        String alarmKey = deviceAlarm.getDeviceId()+":"+deviceAlarm.getChannelId()+":"+deviceAlarm.getAlarmMethod();
        if(ObjectUtils.isEmpty(alarmType)){
            alarmKey = alarmKey+":"+alarmType;
        }
        DeviceAlarm deviceAlarmOld = data.get(alarmKey);
        if(ObjectUtils.isEmpty(deviceAlarmOld)){
            //首次类型的告警信息进入
            deviceAlarmService.add(deviceAlarm);
            data.put(alarmKey,deviceAlarm);
            redisDelayQueuesUtil.addDelayQueue(deviceAlarm, polymerizationExpire, TimeUnit.SECONDS,alarmKey);

        }else {

        }
    }

}
