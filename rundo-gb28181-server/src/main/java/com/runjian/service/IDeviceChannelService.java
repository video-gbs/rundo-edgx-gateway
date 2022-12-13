package com.runjian.service;

import com.runjian.gb28181.bean.DeviceChannel;

import java.util.List;

/**
 * @author chenjialing
 */
public interface IDeviceChannelService {

    /**
     * catlog查询结束后完全重写通道信息----流程优化
     * @param deviceId
     * @param deviceChannelList
     */
    boolean resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList);

    /**
     * 清空通道
     * @param deviceId
     */
    void cleanChannelsForDevice(String deviceId);
}
