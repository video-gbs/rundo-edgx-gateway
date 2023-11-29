package com.runjian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayBusinessMsgType;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.redis.RedisCommonUtil;
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
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
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

    @Autowired
    DataSourceTransactionManager dataSourceTransactionManager;

    @Autowired
    TransactionDefinition transactionDefinition;


    @Override
    public synchronized List<DeviceChannel> resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList) {
        //获取通道原有数据
        return resetChannelsForcatalogLock(deviceId,deviceChannelList);
    }

    public  List<DeviceChannel> resetChannelsForcatalogLock(String deviceId, List<DeviceChannel> deviceChannelList){
        if (CollectionUtils.isEmpty(deviceChannelList)) {
            return null;
        }
        log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "通道同步--入库", "数据进行操作", deviceId);

        //查出数据库中已存在的channel 区分是add还是update
        List<DeviceChannel> deviceChannels = deviceChannelMapper.queryChannelsByDeviceId(deviceId);

        List<String> oldChannelCollect = deviceChannels.stream().map(DeviceChannel::getChannelId).collect(Collectors.toList());
        List<String> newChannelCollect = deviceChannelList.stream().map(DeviceChannel::getChannelId).collect(Collectors.toList());

        List<String> updateCollects = new ArrayList<>(oldChannelCollect);
        List<String> removeCollects = new ArrayList<>(oldChannelCollect);
        List<String> addCollects = new ArrayList<>(newChannelCollect);

        removeCollects.removeAll(newChannelCollect);
        addCollects.removeAll(oldChannelCollect);
        updateCollects.retainAll(newChannelCollect);

        List<DeviceChannel> removeChannels = deviceChannels.parallelStream()
                .filter(deviceChannel -> removeCollects.contains(deviceChannel.getChannelId()))
                .collect(Collectors.toList());

        List<DeviceChannel> addDeviceChannels = deviceChannelList.parallelStream()
                .filter(deviceChannel -> addCollects.contains(deviceChannel.getChannelId()))
                .collect(Collectors.toList());

        List<DeviceChannel> deviceChannelsUpdate = deviceChannelList.parallelStream()
                .filter(deviceChannel -> updateCollects.contains(deviceChannel.getChannelId()))
                .collect(Collectors.toList());

        List<Long> removeIdList = removeChannels.stream().map(DeviceChannel::getId).collect(Collectors.toList());

        TransactionStatus transactionStatus = dataSourceTransactionManager.getTransaction(transactionDefinition);

        try{

            if(!ObjectUtils.isEmpty(removeIdList)){

                //删除关联的节点，项目管理
                deviceChannelMapper.cleanChannelsByChannelIdList(removeIdList);
            }

            int limitCount = 1000;
            if(!CollectionUtils.isEmpty(addDeviceChannels)){
                if(addDeviceChannels.size() > limitCount){
                    for (int i = 0; i < addDeviceChannels.size(); i += limitCount) {
                        int toIndex = i + limitCount;
                        if (i + limitCount > addDeviceChannels.size()) {
                            toIndex = addDeviceChannels.size();
                        }
                        List<DeviceChannel> thisOne = addDeviceChannels.subList(i, toIndex);
                        deviceChannelMapper.batchAdd(thisOne);
                    }

                }else {
                    //直接执行
                    deviceChannelMapper.batchAdd(addDeviceChannels);

                }


            }
            if(!CollectionUtils.isEmpty(deviceChannelsUpdate)){
                //进行编辑数据操作
                if(deviceChannelsUpdate.size() > limitCount){
                    for (int i = 0; i < deviceChannelsUpdate.size(); i += limitCount) {
                        int toIndex = i + limitCount;
                        if (i + limitCount > deviceChannelsUpdate.size()) {
                            toIndex = deviceChannelsUpdate.size();
                        }
                        List<DeviceChannel> thisOne = deviceChannelsUpdate.subList(i, toIndex);
                        deviceChannelMapper.batchUpdate(thisOne);
                    }

                }else {
                    //直接执行
                    deviceChannelMapper.batchUpdate(deviceChannelsUpdate);

                }
            }
            log.info(LogTemplate.PROCESS_LOG_MSG_TEMPLATE, "通道同步--入库", "数据入库成功,deviceId={}",deviceId);

            dataSourceTransactionManager.commit(transactionStatus);
        }catch (Exception e){
            log.error(LogTemplate.PROCESS_LOG_TEMPLATE, "通道同步数据异常", "未知异常",e);
            dataSourceTransactionManager.rollback(transactionStatus);
        }


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
    public void updateByDeviceIdAndChannelId(DeviceChannel deviceChannel) {
         deviceChannelMapper.updateByDeviceAndChannelId(deviceChannel);
    }

    @Override
    public void updateByDeviceIdAndChannelId(DeviceChannel deviceChannel, int status) {
        deviceChannelMapper.updateStatusByDeviceAndChannelId(deviceChannel,status);
    }

    @Override
    public void addOne(DeviceChannel deviceChannel) {
        deviceChannelMapper.add(deviceChannel);
    }

    @Override
    public void recordInfo(RecordInfoReq recordInfoReq) {
        String deviceId = recordInfoReq.getDeviceId();
        String channelId = recordInfoReq.getChannelId();
        String startTime = recordInfoReq.getStartTime();
        String endTime = recordInfoReq.getEndTime();
        String businessSceneKey = GatewayBusinessMsgType.RECORD_INFO.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            Boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.RECORD_INFO, recordInfoReq.getMsgId(),0,null);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,获取录像信息，合并全局的请求",recordInfoReq);
                return;
            }
            Device device = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(device)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return ;
            }

            int sn  =  (int)((Math.random()*9+1)*100000);
            sipCommander.recordInfoQuery(device, channelId, startTime, endTime, sn, null, null, null, null);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
        //在异步线程进行解锁
    }


    @Override
    public void channelHardDelete(String deviceId, String channelId, String msgId) {

        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_HARD.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            //阻塞型,默认是30s无返回参数
            Boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_HARD, msgId, 0,null);
            //尝试获取锁
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,删除通道，合并全局的请求",msgId);
                return;
            }
            //删除通道
            deviceChannelMapper.hardDeleteByDeviceId(deviceId,channelId);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "删除通道", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelSoftDelete(String deviceId, String channelId, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_SOFT.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            //阻塞型,默认是30s无返回参数
            boolean b = redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_SOFT,msgId,0,null);
            if(!b){
                //加锁失败，不继续执行
                log.info(LogTemplate.PROCESS_LOG_TEMPLATE,"设备服务,软删除通道，合并全局的请求",msgId);
                return;
            }
            //软删除通道
//            deviceChannelMapper.softDeleteByDeviceIdAndChannelId(deviceId,channelId);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "软删除通道", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelDeleteRecover(String deviceId, String channelId, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_DELETE_RECOVER.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            //阻塞型,默认是30s无返回参数
            redisCatchStorageService.addBusinessSceneKey(businessSceneKey, GatewayBusinessMsgType.CHANNEL_DELETE_RECOVER,msgId,0,null);

            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.SUCCESS,true);

        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "软删除通道恢复", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
    }

    @Override
    public void channelTalk(String deviceId, String channelId,String dispacherUrl, String msgId) {
        String businessSceneKey = GatewayBusinessMsgType.CHANNEL_TALK.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            Device device = deviceService.getDevice(deviceId);
            sipCommander.audioBroadcastCmd(device,channelId,event -> {
                //广播指令下发失败
                String errorMsg = String.format("广播指令下发失败，错误码： %s, %s", event.statusCode, event.msg);
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,BusinessErrorEnums.UNKNOWN_ERROR,errorMsg);
            },eventResult -> {
                //成功进行缓存
                RedisCommonUtil.set(redisTemplate,BusinessSceneConstants.GATEWAY_BUSINESS_KEY+businessSceneKey,dispacherUrl,10);
            });
        }catch (Exception e){
            log.info(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "语音广播",e);
        }
    }
}
