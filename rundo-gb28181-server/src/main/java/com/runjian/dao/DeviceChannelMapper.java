package com.runjian.dao;

import com.runjian.gb28181.bean.DeviceChannel;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用于存储设备通道信息
 */
@Mapper
@Repository
public interface DeviceChannelMapper {



    @Update(value = {"UPDATE device_channel SET status=0 WHERE deviceId=#{deviceId}"})
    void offlineByDeviceId(String deviceId);
}
