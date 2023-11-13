package com.runjian.gb28181.session;
import com.alibaba.fastjson.JSON;
import com.runjian.common.commonDto.Gateway.dto.AlarmSendDto;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.UuidUtil;
import com.runjian.conf.UserSetting;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.domain.dto.DeviceSendDto;
import com.runjian.gb28181.bean.*;
import com.runjian.mq.MqMsgDealService.MqInfoCommonDto;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceAlarmService;
import com.runjian.service.IRedisCatchStorageService;
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


    @Autowired
    GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    UserSetting userSetting;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    MqInfoCommonDto mqInfoCommonDto;


    @Autowired
    GatewaySignInConf gatewaySignInConf;
    @Autowired
    RabbitMqSender rabbitMqSender;
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
            if(!redisDelayQueuesUtil.checkDelayQueueExist(alarmDelayKey)){
                //首次开始
                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, polymerizationExpire, TimeUnit.SECONDS,alarmDelayKey);
                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, 15, TimeUnit.SECONDS,alarmHeartKey);
                //开始
                alarmMappingSend(deviceAlarm,AlarmEventTypeEnum.COMPOUND_START);
                Thread thread = new Thread(() -> {
                    while (true){
                        DeviceAlarm delayQueueOne = redisDelayQueuesUtil.getDelayQueue(alarmDelayKey);
                        if(ObjectUtils.isEmpty(delayQueueOne)){
                            DeviceAlarm heartQueueOne = redisDelayQueuesUtil.getDelayQueue(alarmHeartKey);
                            if(!ObjectUtils.isEmpty(heartQueueOne)){
                                //发送告警的心跳

                                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, 15, TimeUnit.SECONDS,alarmHeartKey);
                                heartQueueOne.setAlarmTime(DateUtils.getNow());
                                alarmMappingSend(heartQueueOne,AlarmEventTypeEnum.COMPOUND_HEARTBEAT);
                                log.info("心跳："+alarmHeartKey);

                            }
                        }else {
                            delayQueueOne.setAlarmTime(DateUtils.getNow());
                            alarmMappingSend(delayQueueOne,AlarmEventTypeEnum.COMPOUND_END);
                            log.info("结束："+JSON.toJSONString(delayQueueOne));
                            //心跳队列移除
                            break;

                        }


                    }
                });
                thread.start();
            }else {
                //首次结束数据周期内到达  做聚合
                redisDelayQueuesUtil.remove(alarmDelayKey);
                //比较时间范围的
                redisDelayQueuesUtil.addDelayQueue(deviceAlarm, polymerizationExpire, TimeUnit.SECONDS,alarmDelayKey);
            }
        }





    }

    public synchronized void alarmMappingSend(DeviceAlarm deviceAlarm, AlarmEventTypeEnum alarmEventTypeEnum) {
        AlarmSendDto alarmSendDto = new AlarmSendDto();
        BeanUtil.copyProperties(deviceAlarm,alarmSendDto);
        alarmSendDto.setEventTime(deviceAlarm.getAlarmTime());
        alarmSendDto.setEventMsgType(alarmEventTypeEnum.getCode());
        if("5".equals(deviceAlarm.getAlarmMethod())) {
            switch (deviceAlarm.getAlarmType()) {
                case "2":
                    alarmSendDto.setEventCode(AlarmEventCodeEnum.MOVE_ALARM.getCode());
                    alarmSendDto.setEventDesc("移动侦测");
                    break;
                case "6":
                    alarmSendDto.setEventCode(AlarmEventCodeEnum.REGIONAL_ALARM.getCode());
                    alarmSendDto.setEventDesc("区域入侵");
                    break;
                case "5":
                    alarmSendDto.setEventCode(AlarmEventCodeEnum.TRIPPING_WIRE_ALARM.getCode());
                    alarmSendDto.setEventDesc("绊线入侵");
                    break;
                default:

                    break;
            }
            CommonMqDto mqInfo = mqInfoCommonDto.getMqInfo(GatewayBusinessMsgType.ALARM_MSG_NOTIFICATION.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix, null);
            mqInfo.setData(alarmSendDto);
            mqInfo.setCode(BusinessErrorEnums.SUCCESS.getErrCode());
            mqInfo.setMsg(BusinessErrorEnums.SUCCESS.getErrMsg());
            String mqGetQueue = gatewaySignInConf.getMqSetQueue();
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "告警消息-mq信令发送处理", mqInfo);
            rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(), mqInfo, true);
        }

    }

}
