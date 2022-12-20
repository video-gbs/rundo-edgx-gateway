package com.runjian.service.impl;

import com.runjian.dao.DeviceChannelMapper;
import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.service.IDeviceChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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
}
