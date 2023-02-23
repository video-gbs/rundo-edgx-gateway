package com.runjian.dao;

import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.gb28181.bean.Device;
import org.apache.ibatis.annotations.*;
import org.intellij.lang.annotations.Language;
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

    /**
     * 根据设备id获取设备信息
     * @return
     */
    @Select("SELECT * FROM "+DEVICE_TABLE_NAME)
    List<Device> getAllDeviceList();
}
