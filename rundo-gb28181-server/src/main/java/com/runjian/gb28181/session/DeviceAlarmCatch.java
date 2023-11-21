package com.runjian.gb28181.session;
import com.runjian.common.commonDto.Gateway.dto.AlarmSendDto;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.CommonMqDto;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.DateUtils;
import com.runjian.common.utils.UuidUtil;
import com.runjian.conf.DynamicTask;
import com.runjian.conf.UserSetting;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.bean.*;
import com.runjian.mq.MqMsgDealService.MqInfoCommonDto;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceAlarmService;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.redis.RedisDelayQueuesUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.*;

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

    @Autowired
    DynamicTask dynamicTask;

    /**
     * 聚合过期时间
     */
    @Value("${AlarmConfig.polymerization-expire:1}")
    int polymerizationExpire;

    @Autowired
    RedissonClient redissonClient;

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> renewalTask;

    private Map<String, DeviceAlarm> alarmMap = new ConcurrentHashMap<>();

    public  void addReady(DeviceAlarm deviceAlarm) {
        String alarmType = deviceAlarm.getAlarmType();
        String alarmKey = deviceAlarm.getDeviceId()+ BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY +deviceAlarm.getChannelId()+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+deviceAlarm.getAlarmMethod();
        if(!ObjectUtils.isEmpty(alarmType)){
            alarmKey = alarmKey+BusinessSceneConstants.SCENE_STREAM_SPLICE_KEY+alarmType;
        }
        DeviceAlarm deviceAlarmOld = alarmMap.get(alarmKey);
        deviceAlarm.setLastExpireTime(System.currentTimeMillis());
        if(ObjectUtils.isEmpty(deviceAlarmOld)){
            //首次存在 发送开始 并放入map中
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "开始", alarmKey);
            alarmMappingSend(deviceAlarm,AlarmEventTypeEnum.COMPOUND_START);
            deviceAlarm.setLastHeartTime(System.currentTimeMillis()+5000);
            alarmMap.put(alarmKey,deviceAlarm);
        }else {
            long lastHeartTime = deviceAlarmOld.getLastHeartTime();
            deviceAlarm.setLastHeartTime(lastHeartTime);
            alarmMap.put(alarmKey,deviceAlarm);
        }
        if(renewalTask == null){
            renewalTask = executorService.scheduleAtFixedRate(() -> {
                alarmMap.forEach((key,value) ->{
                    long lastExpireTime = value.getLastExpireTime();
                    long lastHeartTime = value.getLastHeartTime();
                    long currentTime = System.currentTimeMillis();
                    if(currentTime>=lastHeartTime){
                        //可以发送心跳数据了
                        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "心跳", key);
                        value.setAlarmTime(DateUtils.getNow());
                        alarmMappingSend(value,AlarmEventTypeEnum.COMPOUND_HEARTBEAT);
                        value.setLastHeartTime(currentTime+5000);
                    }
                    if(currentTime - lastExpireTime >= polymerizationExpire* 1000L){
                        //数据过期，删除，并发送end消息
                        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "结束", key);
                        alarmMap.remove(key);
                        value.setAlarmTime(DateUtils.getNow());
                        alarmMappingSend(value,AlarmEventTypeEnum.COMPOUND_END);
                    }
                });
            }, 0, 1000, TimeUnit.MILLISECONDS); // 每1秒检查一次消息状态
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
                case "10":
                    alarmSendDto.setEventCode(AlarmEventCodeEnum.COVER_ALARM.getCode());
                    alarmSendDto.setEventDesc("遮挡告警");
                    break;
                case "11":
                    alarmSendDto.setEventCode(AlarmEventCodeEnum.COVER_ALARM.getCode());
                    alarmSendDto.setEventDesc("遮挡告警");
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
