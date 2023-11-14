package com.runjian.runner;

import com.runjian.common.constant.AlarmEventTypeEnum;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.bean.DeviceAlarm;
import com.runjian.gb28181.session.DeviceAlarmCatch;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.service.IplayService;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Set;

/**
 * @author chenjialing
 */
@Component
@Slf4j
@Order(value = 0)
public class DelayJobRunner implements CommandLineRunner {

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Autowired
    IplayService iplayService;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    @Autowired
    GatewayInfoConf gatewayInfoConf;

    @Autowired
    RedisDelayQueuesUtil redisDelayQueuesUtil;

    @Autowired
    private ApplicationContext context;

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    @Autowired
    private DeviceAlarmCatch deviceAlarmCatch;

    @Async
    @Override
    public void run(String... args) throws Exception {
        while (true) {
            taskExecutor.execute(()->{
                try {
                    Set<String> keys = RedisCommonUtil.keys(redisTemplate, BusinessSceneConstants.REDISSON_DELAY_QUEUE_PREFIX +"{"+BusinessSceneConstants.ALARM_DELAY_PREFIX+"*");
                    if(!ObjectUtils.isEmpty(keys)){
                        for(String bKey : keys){
                            String substring = bKey.substring(22, bKey.length() - 1);
                            DeviceAlarm delayQueue = redisDelayQueuesUtil.getDelayQueue(substring);
                            if(!ObjectUtils.isEmpty(delayQueue)){
                                log.info("告警动态数据获取，value={}|,key={}", delayQueue,substring);
                                if(bKey.contains("HEART_BUSINESS")){
                                    //心跳
                                    deviceAlarmCatch.alarmMappingSend(delayQueue, AlarmEventTypeEnum.COMPOUND_HEARTBEAT);
                                }else {
                                    //结束
                                    deviceAlarmCatch.alarmMappingSend(delayQueue, AlarmEventTypeEnum.COMPOUND_END);
                                }
                            }
                        }

                    }else {
                        Thread.sleep(20);
                    }
                } catch (Exception e) {
                    log.error("(Redis延迟队列异常中断) {}", e.getMessage());
                }
            });

        }


    }

}
