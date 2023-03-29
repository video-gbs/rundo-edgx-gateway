package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.DeviceControlReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.UserSetting;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.transmit.cmd.impl.SIPCommander;
import com.runjian.service.IDeviceService;
import com.runjian.service.IPtzService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author chenjialing
 */
@Service
@Slf4j
public class PtzServiceImpl implements IPtzService {
    @Autowired
    IDeviceService deviceService;
    @Autowired
    RedissonClient redissonClient;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserSetting userSetting;

    @Autowired
    SIPCommander sipCommander;

//    case "left":
//        cmdCode = 2;
//        break;
//    case "right":
//        cmdCode = 1;
//        break;
//    case "up":
//        cmdCode = 8;
//        break;
//    case "down":
//        cmdCode = 4;
//        break;
//    case "upleft":
//        cmdCode = 10;
//        break;
//    case "upright":
//        cmdCode = 9;
//        break;
//    case "downleft":
//        cmdCode = 6;
//        break;
//    case "downright":
//        cmdCode = 5;
//        break;
//    case "zoomin":
//        cmdCode = 16;
//        break;
//    case "zoomout":
//        cmdCode = 32;
//        break;
//    case "stop":
//        cmdCode = 0;
    private List<Integer> commomCodeArray = Arrays.asList(2,1,8,4,10,9,6,5,16,32,0);

    @Override
    public void deviceControl(DeviceControlReq deviceControlReq) {

        String businessSceneKey = GatewayMsgType.PTZ_CONTROL.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceControlReq.getDeviceId()+BusinessSceneConstants.SCENE_STREAM_KEY+deviceControlReq.getChannelId();
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,deviceControlReq.getMsgId());
            //尝试获取锁
            boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求,加锁失败，合并全局的请求",deviceControlReq.getMsgId());
                return;
            }
            //参数校验
            //判断设备是否存在
            String deviceId = deviceControlReq.getDeviceId();
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return;

            }
            int cmdCode = deviceControlReq.getCmdCode();
            if(!commomCodeArray.contains(cmdCode)){

                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "ptz服务", "ptz操作失败", deviceControlReq);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.VALID_BIND_EXCEPTION_ERROR,null);
                return;
            }
            sipCommander.frontEndCmd(device, deviceControlReq.getChannelId(), cmdCode, deviceControlReq.getHorizonSpeed(), deviceControlReq.getVerticalSpeed(), deviceControlReq.getZoomSpeed());
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.SUCCESS,null);

        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "ptz服务", "ptz操作失败", deviceControlReq);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PTZ_CONTROL,BusinessErrorEnums.UNKNOWN_ERROR,null);
        }

    }
}
