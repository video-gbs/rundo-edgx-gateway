package com.runjian.dao;

import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.domain.dto.DeviceSendDto;
import com.runjian.gb28181.bean.Device;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用于存储设备信息
 * @author chenjialing
 */
@Mapper
@Repository
public interface DeviceMapper {

    String DEVICE_TABLE_NAME = "rundo_device";
    /**
     * 根据设备id获取设备信息
     * @param deviceId
     * @return
     */
    @Select("SELECT * FROM "+DEVICE_TABLE_NAME+" WHERE device_id = #{deviceId}")
    Device getDeviceByDeviceId(String deviceId);

    @Insert("INSERT INTO "+DEVICE_TABLE_NAME+" (#{device})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(Device device);

    @Update("UPDATE "+DEVICE_TABLE_NAME+" (#{device}) where id= #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(Device device);

    /**
     * 删除
     * @param deviceId
     * @return
     */
    @Delete("DELETE FROM "+DEVICE_TABLE_NAME+" WHERE device_id=#{deviceId}")
    int remove(String deviceId);

    @Update("UPDATE "+DEVICE_TABLE_NAME+" set deleted = 1 where id= #{id}")
    int softRemove(String deviceId);

    /**
     * 根据设备id获取设备信息
     * @return
     */
    @Select("SELECT * FROM "+DEVICE_TABLE_NAME+" WHERE deleted = 0")
    List<DeviceSendDto> getAllDeviceList();


    /**
     * 查询所有在线的设备
     * @return
     */
    @Select("SELECT * FROM "+DEVICE_TABLE_NAME+" WHERE online = 1 and deleted = 0")
    List<Device> getOnlineDevices();
}
