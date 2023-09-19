package com.runjian.device;

import com.alibaba.fastjson.JSON;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.utils.DateUtils;
import com.runjian.gb28181.bean.DeviceAlarm;
import com.runjian.gb28181.session.DeviceAlarmCatch;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class deviceControlTest {
    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;
    @Autowired
    DeviceAlarmCatch deviceAlarmCatch;
    @Test
    public void testRecordInfo() throws InterruptedException {


            DeviceAlarm deviceAlarm = new DeviceAlarm();
            deviceAlarm.setId(0L);
            redisDelayQueuesUtil.addDelayQueue(deviceAlarm, 20, TimeUnit.SECONDS,"test");
//            for (int i = 1; i < 5; i++) {
//                deviceAlarm.setId((long) i);
//                deviceAlarm.setAlarmTime(DateUtils.getNow());
//                redisDelayQueuesUtil.addQueue("test",deviceAlarm);
//            }
            while (true){
                Object test = redisDelayQueuesUtil.getDelayQueue("test");
                log.info("告警测试"+JSON.toJSONString(test));
            }

    }

    @Test
    public void testAlarm() throws InterruptedException {
        for (int i = 0; i < 30; i++) {
            DeviceAlarm deviceAlarm = new DeviceAlarm();
            deviceAlarm.setId((long) i);
            deviceAlarm.setDeviceId("123");
            deviceAlarm.setChannelId("321");
            deviceAlarm.setAlarmMethod("5");
            deviceAlarm.setAlarmType("2");
            deviceAlarm.setAlarmTime(DateUtils.getNow());
            deviceAlarmCatch.addReady(deviceAlarm);

        }
        while (true){
            System.out.println(DateUtils.getNow());
            Thread.sleep(3000);
        }


    }


}
