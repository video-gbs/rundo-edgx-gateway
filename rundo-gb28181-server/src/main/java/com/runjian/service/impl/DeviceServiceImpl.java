package com.runjian.service.impl;

import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.GatewayBusinessSceneResp;
import com.runjian.common.constant.*;
import com.runjian.common.utils.BeanUtil;
import com.runjian.conf.DynamicTask;
import com.runjian.conf.UserSetting;
import com.runjian.dao.DeviceChannelMapper;
import com.runjian.dao.DeviceCompatibleMapper;
import com.runjian.dao.DeviceMapper;
import com.runjian.domain.dto.DeviceSendDto;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
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
    UserSetting userSetting;

    @Autowired
    IRedisCatchStorageService redisCatchStorageService;


    @Override
    public  void  online(Device device) {

        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线", device);
        //转换为gb28181专用的bean
        Device deviceDb = getDevice(device.getDeviceId());
        device.setOnline(1);
        DeviceSendDto deviceSendDto = new DeviceSendDto();
        BeanUtil.copyProperties(device,deviceSendDto);
        // 第一次上线 或则设备之前是离线状态--进行通道同步和设备信息查询
        String businessSceneKey = GatewayBusinessMsgType.REGISTER.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId();
        try {
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.REGISTER,null);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备上线,加锁失败，合并全局的请求",device);
                return;
            }
            if (deviceDb == null) {
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线-首次注册,查询设备信息以及通道信息", device);
                deviceMapper.add(device);
                try {
                    //阻塞型,默认是30s无返回参数
                    sipCommander.deviceInfoQuery(device);
                }catch (Exception e){
                    log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
                    redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.REGISTER,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);

                }
            }else {
                log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备上线-更新,查询设备信息以及通道信息", device);
                //重新上线 发送mq
                deviceMapper.update(device);
                //发送mq设备上线信息

                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.REGISTER,BusinessErrorEnums.SUCCESS,device);

            }
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_MSG_TEMPLATE, "设备服务", "设备上线处理异常", e,device.getDeviceId());
        }



        if (device.getKeepaliveTime() == null) {
            device.setKeepaliveIntervalTime(60);
        }

        // 刷新过期任务
        String registerExpireTaskKey = VideoManagerConstants.REGISTER_EXPIRE_TASK_KEY_PREFIX + device.getDeviceId();
        // 如果三次心跳失败，则设置设备离线
        dynamicTask.startDelay(registerExpireTaskKey, ()-> offline(device),  (int)device.getKeepaliveIntervalTime()*1000*3);

    }

    @Override
    public void offline(Device device) {
        String businessSceneKey = GatewayBusinessMsgType.REGISTER.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId();
        boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.REGISTER, null);
        if (!b) {
            //加锁失败，不继续执行
            log.info(LogTemplate.PROCESS_LOG_TEMPLATE, "设备离线,加锁失败，合并全局的请求", device);
            return;
        }


        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备离线", device);
        //判断数据库中是否存在
        if(device.getId() == 0){
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "设备服务", "设备离线--设备信息不存在", device);
            return;
        }
        String deviceId = device.getDeviceId();
        String registerExpireTaskKey = VideoManagerConstants.REGISTER_EXPIRE_TASK_KEY_PREFIX + deviceId;
        dynamicTask.stop(registerExpireTaskKey);
        device.setOnline(0);
        deviceMapper.update(device);
        //进行通道离线
        deviceChannelMapper.offlineByDeviceId(deviceId);
        //  TODO离线释放所有ssrc
        //发送mq设备上线信息
        DeviceSendDto deviceSendDto = new DeviceSendDto();
        BeanUtil.copyProperties(device,deviceSendDto);
        redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.REGISTER,BusinessErrorEnums.SUCCESS,deviceSendDto);

    }

    @Override
    public Device getDevice(String deviceId) {
        return deviceMapper.getDeviceByDeviceId(deviceId);

    }

    @Override
    public void sync(Device device,String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CATALOG.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId();
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"通道信息同步请求",device);
        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CATALOG,msgId);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求,加锁失败，合并全局的请求",msgId);
                return;
            }
            int sn = (int)((Math.random()*9+1)*100000);
            catalogDataCatch.addReady(device,sn);
            sipCommander.catalogQuery(device, sn, event -> {
                String errorMsg = String.format("同步通道失败，错误码： %s, %s", event.statusCode, event.msg);
                log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "同步通道失败", errorMsg);
                catalogDataCatch.setChannelSyncEnd(device.getDeviceId(), errorMsg, BusinessErrorEnums.SIP_CATALOG_EXCEPTION.getErrCode());
            });
        }catch (Exception e){
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CATALOG,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);

        }

    }

    @Override
    public void updateDevice(Device device) {
        device.setCharset(device.getCharset().toUpperCase());
        deviceMapper.update(device);

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
//        String businessSceneKey = GatewayBusinessMsgType.DEVICEINFO.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+device.getDeviceId();
//        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求",device);
//        RLock lock = redissonClient.getLock(businessSceneKey);
//        try {
//            //阻塞型,默认是30s无返回参数
//            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayBusinessMsgType.DEVICEINFO,msgId,userSetting.getBusinessSceneTimeout(),null);
//            ArrayList<BusinessSceneResp> businessSceneRespArrayList = new ArrayList<>();
//            businessSceneRespArrayList.add(objectBusinessSceneResp);
//            RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, businessSceneRespArrayList);
//            //尝试获取锁
//            boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100,TimeUnit.MILLISECONDS);
//            if(!b){
//                //加锁失败，不继续执行
//                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息同步请求,加锁失败，合并全局的请求",msgId);
//                return;
//            }
//            sipCommander.deviceInfoQuery(device);
//        }catch (Exception e){
//            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
//            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICEINFO,BusinessErrorEnums.SIP_SEND_EXCEPTION,null);
//
//        }
//        //在异步线程进行解锁


    }

    @Override
    public void deviceDelete(String deviceId,String msgId) {
        //同设备同类型业务消息，加上全局锁
        String businessSceneKey = GatewayBusinessMsgType.DEVICE_DELETE.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+deviceId;
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息删除请求",deviceId+"|"+msgId);
        try {
            boolean b =  redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE,msgId);
            //尝试获取锁
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息删除请求,加锁失败，合并全局的请求",msgId);
                return;
            }
            Device deviceDto = getDevice(deviceId);
            if(ObjectUtils.isEmpty(deviceDto)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE,BusinessErrorEnums.SUCCESS,true);
                return ;
            }
            if(deviceDto.getOnline() == 0){
                //可以删除
                deviceMapper.remove(deviceId);
                deviceChannelMapper.cleanChannelsByDeviceId(deviceId);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE,BusinessErrorEnums.SUCCESS,true);

            }else {
                //不要删除
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE,BusinessErrorEnums.SUCCESS,false);
            }

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }


    }

    @Override
    public void deviceSoftDelete(String deviceId, String msgId) {
        //同设备同类型业务消息，加上全局锁
        String businessSceneKey = GatewayBusinessMsgType.DEVICE_DELETE_SOFT.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY+deviceId;
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息删除请求",deviceId+"|"+msgId);
        try {
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE_SOFT,msgId);
            //尝试获取锁
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备信息删除请求,加锁失败，合并全局的请求",msgId);
                return;
            }
            Device deviceDto = getDevice(deviceId);
            if(ObjectUtils.isEmpty(deviceDto)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE_SOFT,BusinessErrorEnums.SUCCESS,true);
                return ;
            }
            //可以删除
            deviceMapper.softRemove(deviceId);
            deviceChannelMapper.softDeleteByDeviceId(deviceId);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE_SOFT,BusinessErrorEnums.SUCCESS,true);


        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_DELETE_SOFT,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void deviceList(String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.DEVICE_TOTAL_SYNC.getTypeName()+BusinessSceneConstants.SCENE_SEM_KEY;
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备全量数据同步",msgId);
        try {
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_TOTAL_SYNC,msgId);
            //阻塞型,默认是30s无返回参数
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备全量数据同步,加锁失败，合并全局的请求",msgId);
                return;
            }
            List<DeviceSendDto> allDeviceList = deviceMapper.getAllDeviceList();
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_TOTAL_SYNC,BusinessErrorEnums.SUCCESS,allDeviceList);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "设备全量数据同步失败", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.DEVICE_TOTAL_SYNC,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public List<Device> getAllOnlineDevice() {
        return deviceMapper.getOnlineDevices();
    }
}
