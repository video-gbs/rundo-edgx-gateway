package com.runjian.service.impl;

import com.runjian.gb28181.bean.DeviceChannel;
import com.runjian.service.IDeviceChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author chenjialing
 */
@Service
@Slf4j
public class DeviceChannelServiceImpl implements IDeviceChannelService {
    @Override
    public boolean resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList) {
        return false;
    }

    @Override
    public void cleanChannelsForDevice(String deviceId) {

    }
}
