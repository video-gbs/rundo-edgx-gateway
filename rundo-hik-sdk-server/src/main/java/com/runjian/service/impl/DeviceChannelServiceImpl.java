package com.runjian.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.domain.dto.DeviceChannel;
import com.runjian.entity.DeviceChannelEntity;
import com.runjian.entity.DeviceEntity;
import com.runjian.mapper.DeviceChannelMapper;
import com.runjian.mapper.DeviceMapper;
import com.runjian.service.IDeviceChannelService;
import com.runjian.service.IDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DeviceChannelServiceImpl extends ServiceImpl<DeviceChannelMapper, DeviceChannelEntity> implements IDeviceChannelService {

    @Override
    public boolean resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList) {
        return false;
    }

    @Override
    public void cleanChannelsForDevice(String deviceId) {

    }

    @Override
    public DeviceChannel getOne(String deviceId, String channelId) {
        return null;
    }

    @Override
    public void recordInfo(RecordInfoReq recordInfoReq) {

    }
}
