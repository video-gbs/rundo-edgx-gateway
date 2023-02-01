package com.runjian.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.DynamicTask;
import com.runjian.conf.UserSetting;
import com.runjian.dao.DeviceChannelMapper;
import com.runjian.dao.DeviceCompatibleMapper;
import com.runjian.dao.DeviceMapper;
import com.runjian.domain.dto.CatalogMqSyncDto;
import com.runjian.domain.dto.DeviceDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.session.CatalogDataCatch;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.mq.gatewayBusiness.asyncSender.GatewayBusinessAsyncSender;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import com.runjian.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 设备业务
 * @author chenjialing
 */
@Service
@Slf4j
public class DeviceServiceImpl implements IDeviceService {



    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceChannelMapper deviceChannelMapper;

    @Autowired
    private DeviceCompatibleMapper deviceCompatibleMapper;


    @Autowired
    private ISIPCommander sipCommander;

    private final String  registerExpireTaskKeyPrefix = "device-register-expire-";


    @Autowired
    private CatalogDataCatch catalogDataCatch;

    @Autowired
    private DynamicTask dynamicTask;

    @Autowired
    private GatewayBusinessAsyncSender gatewayBusinessAsyncSender;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    UserSetting userSetting;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;


    @Override
    public void online(DeviceDto device) {

        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线", device);
        //转换为gb28181专用的bean
        Device deviceBean = new Device();


        // 第一次上线 或则设备之前是离线状态--进行通道同步和设备信息查询
        if (device.getCreatedAt() == null) {
            device.setOnline(1);
            BeanUtil.copyProperties(device, deviceBean);
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线-首次注册,查询设备信息以及通道信息", device.getDeviceId());
            deviceMapper.add(device);


            //发送mq设备上线信息
            BusinessSceneResp<Device> tBusinessSceneResp = BusinessSceneResp.addSceneEnd(GatewayMsgType.REGISTER, BusinessErrorEnums.SUCCESS, null, 0, LocalDateTime.now(), deviceBean);
            gatewayBusinessAsyncSender.sendforAllScene(tBusinessSceneResp);
        }else {

            if(device.getOnline() == 0){
                //重新上线 发送mq
                device.setOnline(1);
                BeanUtil.copyProperties(device, deviceBean);
                //发送mq设备上线信息
                BusinessSceneResp<Device> tBusinessSceneResp = BusinessSceneResp.addSceneEnd(GatewayMsgType.REGISTER, BusinessErrorEnums.SUCCESS, null, 0, LocalDateTime.now(), deviceBean);
                gatewayBusinessAsyncSender.sendforAllScene(tBusinessSceneResp);
            }


            deviceMapper.update(device);

        }
//        sync(deviceBean,null);
        // 刷新过期任务
        if(deviceCompatibleMapper.getByDeviceId(device.getDeviceId(), DeviceCompatibleEnum.HUAWEI_NVR_800.getType()) == null){
            //华为nvr800 不做定时过期限制
            String registerExpireTaskKey = registerExpireTaskKeyPrefix + device.getDeviceId();
            dynamicTask.startDelay(registerExpireTaskKey, ()-> offline(device), device.getExpires() * 1000);
        }

    }

    @Override
    public void offline(DeviceDto device) {
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备离线", device);
        //判断数据库中是否存在
        if(device.getId() == 0){
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备离线--设备信息不存在", device);
            return;
        }
        String deviceId = device.getDeviceId();
        String registerExpireTaskKey = registerExpireTaskKeyPrefix + deviceId;
        dynamicTask.stop(registerExpireTaskKey);
        device.setOnline(0);
        deviceMapper.update(device);
        //进行通道离线
        deviceChannelMapper.offlineByDeviceId(deviceId);
        //  TODO离线释放所有ssrc
//        List<SsrcTransaction> ssrcTransactions = streamSession.getSsrcTransactionForAll(deviceId, null, null, null);
//        if (ssrcTransactions != null && ssrcTransactions.size() > 0) {
//            for (SsrcTransaction ssrcTransaction : ssrcTransactions) {
//                mediaServerService.releaseSsrc(ssrcTransaction.getMediaServerId(), ssrcTransaction.getSsrc());
//                mediaServerService.closeRTPServer(ssrcTransaction.getMediaServerId(), ssrcTransaction.getStream());
//                streamSession.remove(deviceId, ssrcTransaction.getChannelId(), ssrcTransaction.getStream());
//            }
//        }
        Device deviceBean = new Device();
        BeanUtil.copyProperties(device,deviceBean);
        //发送mq设备上线信息
        BusinessSceneResp<Device> tBusinessSceneResp = BusinessSceneResp.addSceneEnd(GatewayMsgType.REGISTER, BusinessErrorEnums.SUCCESS, null, 0, LocalDateTime.now(), deviceBean);
        gatewayBusinessAsyncSender.sendforAllScene(tBusinessSceneResp);
    }

    @Override
    public DeviceDto getDevice(String deviceId) {
        return deviceMapper.getDeviceByDeviceId(deviceId);

    }

    @Override
    public void sync(Device device,String msgId) {
        String businessSceneKey = GatewayMsgType.CATALOG.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId();
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"通道信息同步请求",device);
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.CATALOG,msgId,userSetting.getBusinessSceneTimeout());
            boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
            if(!hset){
                throw new Exception("redis操作hashmap失败");
            }
            int sn = (int)((Math.random()*9+1)*100000);
            catalogDataCatch.addReady(device,sn);
            sipCommander.catalogQuery(device, sn, event -> {
                String errorMsg = String.format("同步通道失败，错误码： %s, %s", event.statusCode, event.msg);
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "同步通道失败", errorMsg);
                catalogDataCatch.setChannelSyncEnd(device.getDeviceId(), errorMsg, BusinessErrorEnums.SIP_CATALOG_EXCEPTION.getErrCode());
            });
        }catch (Exception e){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.CATALOG,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);

        }

    }

    @Override
    public void updateDevice(Device device) {
        device.setCharset(device.getCharset().toUpperCase());

        DeviceDto deviceDto = new DeviceDto();
        BeanUtil.copyProperties(device,deviceDto);
        deviceMapper.update(deviceDto);

    }

    @Override
    public boolean expire(Device device) {
        Instant registerTimeDate = Instant.from(DateUtil.formatter.parse(device.getRegisterTime()));
        Instant expireInstant = registerTimeDate.plusMillis(TimeUnit.SECONDS.toMillis(device.getExpires()));
        return expireInstant.isBefore(Instant.now());
    }

    @Override
    public void deviceInfoQuery(Device device,String msgId) {
        //同设备同类型业务消息，加上全局锁
        String businessSceneKey = GatewayMsgType.DEVICEINFO.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId();
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求",device);
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.DEVICEINFO,msgId,userSetting.getBusinessSceneTimeout());
            boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
            if(!hset){
                throw new Exception("redis操作hashmap失败");
            }
            sipCommander.deviceInfoQuery(device);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.DEVICEINFO,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);

        }
        //在异步线程进行解锁


    }

    @Override
    public void deviceDelete(String deviceId,String msgId) {
        //同设备同类型业务消息，加上全局锁
        String businessSceneKey = GatewayMsgType.DEVICE_DELETE.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+deviceId;
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求",deviceId+"|"+msgId);
        RLock lock = redissonClient.getLock(businessSceneKey);
        try {
            //阻塞型,默认是30s无返回参数
            lock.lock();
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.DEVICE_DELETE,msgId,userSetting.getBusinessSceneTimeout());
            boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
            if(!hset){
                throw new Exception("redis操作hashmap失败");
            }
            DeviceDto deviceDto = deviceMapper.getDeviceByDeviceId(deviceId);
            if(ObjectUtils.isEmpty(deviceDto)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.DEVICE_DELETE,BusinessErrorEnums.SUCCESS,true);
                return ;
            }
            if(deviceDto.getOnline() == 0){
                //可以删除
                deviceMapper.remove(deviceId);
                deviceChannelMapper.cleanChannelsByDeviceId(deviceId);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.DEVICE_DELETE,BusinessErrorEnums.SUCCESS,true);

            }else {
                //不要删除
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.DEVICE_DELETE,BusinessErrorEnums.SUCCESS,false);
            }

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.DEVICE_DELETE,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }


    }
}
