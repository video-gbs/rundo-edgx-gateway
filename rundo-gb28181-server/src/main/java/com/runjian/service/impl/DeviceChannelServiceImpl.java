package com.runjian.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.exception.BusinessErrorEnums;
import com.runjian.common.config.response.BusinessSceneResp;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.common.constant.BusinessSceneConstants;
import com.runjian.common.constant.GatewayMsgType;
import com.runjian.common.constant.LogTemplate;
import com.runjian.common.utils.BeanUtil;
import com.runjian.common.utils.redis.RedisCommonUtil;
import com.runjian.conf.UserSetting;
import com.runjian.dao.DeviceChannelMapper;
import com.runjian.domain.dto.DeviceDto;
import com.runjian.gb28181.bean.Device;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.gb28181.bean.RecordInfo;
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
    public boolean resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList) {
        //获取通道原有数据
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
            deviceChannelMapper.cleanChannelsByChannelIdList(idList);
        }

        if(!CollectionUtils.isEmpty(addCollects)){
            //进行添加数据组装
            for (DeviceChannel deviceChannel : deviceChannelList) {
                if(addCollects.contains(deviceChannel.getChannelId())){
                    addDeviceChannels.add(deviceChannel);
                }
            }
            deviceChannelMapper.batchAdd(addDeviceChannels);
        }
        if(!CollectionUtils.isEmpty(updateCollects)){
            //进行编辑数据操作
            for (DeviceChannel deviceChannel : deviceChannels) {
                if(updateCollects.contains(deviceChannel.getChannelId())){
                    //单独编辑入库
                    deviceChannelMapper.update(deviceChannel);
                }
            }
        }

        return true;
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
        String msgId = recordInfoReq.getMsgId();
        String businessSceneKey = GatewayMsgType.RECORD_INFO.getTypeName()+ BusinessSceneConstants.SCENE_SEM_KEY+deviceId+ BusinessSceneConstants.SCENE_STREAM_KEY+channelId;
        try {
            RLock lock = redissonClient.getLock(businessSceneKey);
            //阻塞型,默认是30s无返回参数
            lock.lock();
            //同设备同类型业务消息，加上全局锁
            BusinessSceneResp<Object> objectBusinessSceneResp = BusinessSceneResp.addSceneReady(GatewayMsgType.RECORD_INFO,msgId,userSetting.getBusinessSceneTimeout());
            boolean hset = RedisCommonUtil.hset(redisTemplate, BusinessSceneConstants.ALL_SCENE_HASH_KEY, businessSceneKey, objectBusinessSceneResp);
            if(!hset){
                throw new Exception("redis操作hashmap失败");
            }
            DeviceDto deviceDto = deviceService.getDevice(deviceId);
            if(ObjectUtils.isEmpty(deviceDto)){
                redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.RECORD_INFO,BusinessErrorEnums.DB_DEVICE_NOT_FOUND,null);
                return ;
            }

            int sn  =  (int)((Math.random()*9+1)*100000);
            Device device = new Device();
            BeanUtil.copyProperties(deviceDto,device);
            sipCommander.recordInfoQuery(device, channelId, startTime, endTime, sn, null, null, null, null);
        }catch (Exception e){
            log.error(LogTemplate.ERROR_LOG_TEMPLATE, "设备服务", "[命令发送失败] 查询设备信息", e);
            redisCatchStorageService.editBusinessSceneKey(businessSceneKey,GatewayMsgType.PLAY,BusinessErrorEnums.UNKNOWN_ERROR,null);

        }
        //在异步线程进行解锁
    }
}
