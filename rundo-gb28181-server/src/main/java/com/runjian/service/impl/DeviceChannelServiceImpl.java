package com.runjian.service.impl;

import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.conf.UserSetting;
import com.runjian.dao.DeviceChannelMapper;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.transmit.cmd.ISIPCommander;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import com.runjian.service.IRedisCatchStorageService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class DeviceChannelServiceImpl implements IDeviceChannelService {
    @Autowired
    private DeviceChannelMapper deviceChannelMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    UserSetting userSetting;

    @Autowired
    private ISIPCommander sipCommander;
    @Autowired
    IRedisCatchStorageService redisCatchStorageService;

    @Autowired
    IDeviceService deviceService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public synchronized List<DeviceChannel> resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList) {
        //获取通道原有数据
        log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"通道同步",deviceChannelList);
        List<DeviceChannel> deviceChannels = deviceChannelMapper.queryChannelsByDeviceId(deviceId);
        //组装增删改的数据
        List<String> updateCollects = new ArrayList<>();
        List<String> addCollects = new ArrayList<>();
        List<String> removeCollects = new ArrayList<>();
        List<String> oldChannelCollect = deviceChannels.stream().map(DeviceChannel::getChannelId).collect(Collectors.toList());
        List<String> newChannelCollect = deviceChannelList.stream().map(DeviceChannel::getChannelId).collect(Collectors.toList());
        //组装数据
        for (String s : oldChannelCollect) {
            removeCollects.add(s);
            updateCollects.add(s);
        }

        for (String s : newChannelCollect) {
            addCollects.add(s);
        }
        //组装待删除的数据
        boolean b = removeCollects.removeAll(newChannelCollect);
        //组装待添加的数据
        boolean b1 = addCollects.removeAll(oldChannelCollect);
        //状态待修改的数据
        boolean b2 = updateCollects.retainAll(newChannelCollect);

        List<DeviceChannel> addDeviceChannels = new ArrayList<>();
        if(!CollectionUtils.isEmpty(removeCollects)){
            //进行删除数据组装
            List<Long> idList = new ArrayList<>();
            for (DeviceChannel deviceChannel : deviceChannels) {
                if(removeCollects.contains(deviceChannel.getChannelId())){
                    long id = deviceChannel.getId();
                    idList.add(id);
                }
            }
            log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"通道下线",idList);
            deviceChannelMapper.cleanChannelsByChannelIdList(idList);
        }

        if(!CollectionUtils.isEmpty(addCollects)){
            //进行添加数据组装
            for (DeviceChannel deviceChannel : deviceChannelList) {
                if(addCollects.contains(deviceChannel.getChannelId())){
                    addDeviceChannels.add(deviceChannel);
                }
            }
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE,"通道新增",addDeviceChannels,deviceChannelList);
            deviceChannelMapper.batchAdd(addDeviceChannels);
        }
        if(!CollectionUtils.isEmpty(updateCollects)){
            //进行编辑数据操作
            List<DeviceChannel> deviceChannelsUpdate = new ArrayList<>();
            for (DeviceChannel deviceChannel : deviceChannelList) {
                if(updateCollects.contains(deviceChannel.getChannelId())){
                    //单独编辑入库
                    deviceChannelsUpdate.add(deviceChannel);
                }
            }
            if(!CollectionUtils.isEmpty(deviceChannelsUpdate)){
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"通道编辑",deviceChannelsUpdate);
                deviceChannelMapper.batchUpdate(deviceChannelsUpdate);

            }
        }
        //从数据库中重新查找 过滤被删除的通道

        return deviceChannelMapper.queryUndeletedChannelsByDeviceId(deviceId);
    }

    @Override
    public void cleanChannelsForDevice(String deviceId) {
        deviceChannelMapper.cleanChannelsByDeviceId(deviceId);
    }

    @Override
    public DeviceChannel getOne(String deviceId, String channelId) {
        return deviceChannelMapper.queryChannelsByDeviceIdAndChannelId(deviceId, channelId);
    }

    @Override
    public void recordInfo(RecordInfoReq recordInfoReq) {
        String deviceId = recordInfoReq.getDeviceId();
        String channelId = recordInfoReq.getChannelId();
        String startTime = recordInfoReq.getStartTime();
        String endTime = recordInfoReq.getEndTime();
        String businessSceneKey = GatewayBusinessMsgType.RECORD_INFO.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            Boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.RECORD_INFO, recordInfoReq.getMsgId());
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,获取录像信息，合并全局的请求",recordInfoReq);
                return;
            }
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.RECORD_INFO,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return ;
            }

            int sn  =  (int)((Math.random()*9+1)*100000);
            sipCommander.recordInfoQuery(device, channelId, startTime, endTime, sn, null, null, null, null);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.RECORD_INFO,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
        //在异步线程进行解锁
    }


    @Override
    public void channelHardDelete(String deviceId, String channelId, String msgId) {

        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_HARD.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            RLock lock = redissonClient.getLock(businessSceneKey);
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_HARD,msgId);
            //尝试获取锁
            boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,删除通道，合并全局的请求",msgId);
                return;
            }
            //删除通道
            deviceChannelMapper.hardDeleteByDeviceId(deviceId,channelId);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CHANNEL_DELETE_HARD,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "删除通道", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CHANNEL_DELETE_HARD,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelSoftDelete(String deviceId, String channelId, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_SOFT.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            RLock lock = redissonClient.getLock(businessSceneKey);
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_SOFT,msgId);
            //尝试获取锁
            boolean b = lock.tryLock(0,userSetting.getBusinessSceneTimeout()+100, TimeUnit.MILLISECONDS);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,软删除通道，合并全局的请求",msgId);
                return;
            }
            //软删除通道
            deviceChannelMapper.softDeleteByDeviceIdAndChannelId(deviceId,channelId);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CHANNEL_DELETE_SOFT,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "软删除通道", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayBusinessMsgType.CHANNEL_DELETE_SOFT,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }
}
