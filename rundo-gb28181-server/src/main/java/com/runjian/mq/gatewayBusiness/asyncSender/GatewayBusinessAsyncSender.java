package com.runjian.mq.gatewayBusiness.asyncSender;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.mq.RabbitMqSender;
import com.runjian.common.mq.domain.GatewayMqDto;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.GatewayInfoConf;
import com.runjian.conf.mq.GatewaySignInConf;
import com.runjian.gb28181.bean.CatalogData;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.bean.HandlerCatchData;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.UuidUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author chenjialing
 */
@Component
@Slf4j
public class GatewayBusinessAsyncSender {

    @Autowired
    RabbitMqSender rabbitMqSender;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    GatewaySignInConf gatewaySignInConf;

    @Autowired
    CatalogDataCatch catalogDataCatch;

    @Autowired
    RedisTemplate redisTemplate;

    private ConcurrentLinkedQueue<BusinessSceneResp> taskQueue = new ConcurrentLinkedQueue<>();

    @Qualifier("taskExecutor")
    @Autowired
    private ThreadPoolTaskExecutor taskExecutor;

    //全消息处理
    public void sendforAllScene(BusinessSceneResp businessSceneResp){
        //先进先出，处理消息队列未能发送失败的场景
        taskQueue.offer(businessSceneResp);
        String mqGetQueue = gatewaySignInConf.getMqGetQueue();
        if(ObjectUtils.isEmpty(mqGetQueue)){
            //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送失败，业务队列暂时未初始化", businessSceneResp);
            return;
        }
        taskExecutor.execute(()->{
            while (!taskQueue.isEmpty()){
                BusinessSceneResp businessSceneRespPoll = taskQueue.poll();
                GatewayMsgType gatewayMsgType = businessSceneRespPoll.getGatewayMsgType();
                String msgId = businessSceneRespPoll.getMsgId();
                GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(gatewayMsgType.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,msgId);
                mqInfo.setData(businessSceneRespPoll.getData());
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "业务场景处理", "业务场景处理-mq信令发送处理", businessSceneResp);
                rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),mqInfo,true);
            }
        });
    }

    //注册mq消息发送
    @Async("taskExecutor")
    public void sendRegister(Device device){
        //构造消息请求体
        String mqGetQueue = gatewaySignInConf.getMqGetQueue();
        if(ObjectUtils.isEmpty(mqGetQueue)){
            //业务队列暂时未创建成功，无法发送消息 todo 后续做补偿机制，顺序进行消息的推送
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "设备注册-信令发送失败，业务队列暂时未初始化", device);
            return;
        }
        GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.REGISTER.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);
        mqInfo.setData(device);
        rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), mqGetQueue, UuidUtil.toUuid(),mqInfo,true);
    }

    /**
     * 通道消息发送
     * @param device
     */
    @Async("taskExecutor")
    public void sendCatalog(Device device){
        //优化catalogData的数据结构
        String deviceId = device.getDeviceId();
        boolean syncRunning = catalogDataCatch.isSyncRunning(deviceId);
        while (!syncRunning){
            try{
                Thread.sleep(10);
            }catch (InterruptedException e){

            }
            syncRunning = catalogDataCatch.isSyncRunning(deviceId);
        }
        //进行mq消息发送
        CatalogData data = catalogDataCatch.getData(deviceId);
        GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.CATALOG.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix,null);
        mqInfo.setCode(data.getCode());
        mqInfo.setMsg(data.getErrorMsg());
        mqInfo.setData(data.getChannelList());
        rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), gatewaySignInConf.getMqGetQueue(), UuidUtil.toUuid(),mqInfo,true);
    }

    /**
     * 设备信息的mq消息发送
     * @param device
     */
    @Async("taskExecutor")
    public void sendDeviceInfo(Device device){
        //
//        String deviceId = device.getDeviceId();
//        //
//        //获取指令发送的缓存状态
//        BusinessSceneResp businessSceneResp = (BusinessSceneResp<Device>)RedisCommonUtil.get(redisTemplate, BusinessSceneConstants.DEVICE_INFO_SCENE_KEY + deviceId);
//        GatewayMqDto mqInfo = redisCatchStorageService.getMqInfo(GatewayMsgType.DEVICEINFO.getTypeName(), GatewayCacheConstants.GATEWAY_BUSINESS_SN_INCR, GatewayCacheConstants.GATEWAY_BUSINESS_SN_prefix);
//
//        if(ObjectUtils.isEmpty(businessSceneResp)){
//            //缓存异常 发送失败指令
//            mqInfo.setCode(BusinessErrorEnums.REDIS_EXCEPTION.getErrCode());
//            mqInfo.setMsg(BusinessErrorEnums.REDIS_EXCEPTION.toString());
//            mqInfo.setData(null);
//            rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), gatewaySignInConf.getMqGetQueue(), UuidUtil.toUuid(),mqInfo,true);
//            return;
//        }
//        int i = 0;
//        while (businessSceneResp.getStatus().equals(BusinessSceneStatusEnum.ready)){
//            try{
//                Thread.sleep(sleepTime);
//            }catch (InterruptedException e){
//                e.printStackTrace();
//            }
//            i+=sleepTime;
//
//            //sleep时间大于5s结束循环
//            if(i>=maxSleepTime){
//                //判断
//                businessSceneResp = BusinessSceneResp.addSceneEnd(GatewayMsgType.DEVICEINFO,BusinessErrorEnums.BUSINESS_SCENE_EXCEPTION,null);
//                break;
//            }else {
//                businessSceneResp = (BusinessSceneResp<Device>)RedisCommonUtil.get(redisTemplate, BusinessSceneConstants.DEVICE_INFO_SCENE_KEY + deviceId);
//            }
//        }
//        //进行mq消息发送
//        if(ObjectUtils.isEmpty(businessSceneResp)){
//            mqInfo.setCode(BusinessErrorEnums.REDIS_EXCEPTION.getErrCode());
//            mqInfo.setMsg(BusinessErrorEnums.REDIS_EXCEPTION.toString());
//            mqInfo.setData(null);
//        }else {
//            mqInfo.setCode(businessSceneResp.getCode());
//            mqInfo.setMsg(businessSceneResp.getMsg());
//            mqInfo.setData(businessSceneResp.getData());
//        }
//        rabbitMqSender.sendMsgByExchange(gatewaySignInConf.getMqExchange(), gatewaySignInConf.getMqGetQueue(), UuidUtil.toUuid(),mqInfo,true);
    }
}

