package com.runjian.device;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.PlayReq;
import com.runjian.common.mq.domain.CommonMqDto;
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
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class deviceControlTest {
    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;
    @Autowired
    DeviceAlarmCatch deviceAlarmCatch;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
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
        // 模拟不断接收告警消息

        for (int i = 0; i < 30; i++) {
            DeviceAlarm deviceAlarm = new DeviceAlarm();
            deviceAlarm.setId((long) i);
            deviceAlarm.setDeviceId(String.valueOf(i%3));
            deviceAlarm.setChannelId("321");
            deviceAlarm.setAlarmMethod("5");
            deviceAlarm.setAlarmType("2");
            deviceAlarm.setAlarmTime(DateUtils.getNow());
            deviceAlarmCatch.addReady(deviceAlarm);
            Thread.sleep(3000);

        }

    }

    @Test
    public void testDefenses() throws InterruptedException {
        String jsonMsg = "{\"code\":0,\"data\":{\"dataMap\":{\"34020000001130000245\":[\"34020000001320000211\",\"34020000001320000203\"]}},\"error\":false,\"msgId\":\"63540\",\"msgType\":\"CHANNEL_DEFENSES_DEPLOY\",\"serialNum\":\"eb48104ddf2b4760be541783b3620001\",\"time\":\"2023-10-31 10:01:45\"}";
        CommonMqDto commonMqDto = JSON.parseObject(jsonMsg, CommonMqDto.class);
        JSONObject dataJson = (JSONObject) commonMqDto.getData();
        log.info("布防={}",dataJson);

        JSONObject dataMapJson = dataJson.getJSONObject("dataMap");
        if(!ObjectUtils.isEmpty(dataMapJson)){
            for(String key: dataMapJson.keySet()){
                JSONArray channelArr = dataMapJson.getJSONArray(key);
                if(!ObjectUtils.isEmpty(channelArr)){
                    channelArr.forEach(obj->{
                        log.info("实际通道={}",obj);
                    });
                }

            }

        }

        log.info("转义后={}",dataMapJson);

    }


}
