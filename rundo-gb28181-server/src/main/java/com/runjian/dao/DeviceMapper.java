package com.runjian.dao;

import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.domain.dto.DeviceDto;
import org.apache.ibatis.annotations.*;
import org.intellij.lang.annotations.Language;
import org.springframework.stereotype.Repository;

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
    DeviceDto getDeviceByDeviceId(String deviceId);

    @Insert("INSERT INTO "+DEVICE_TABLE_NAME+" (#{device})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(DeviceDto device);

    @Update("UPDATE "+DEVICE_TABLE_NAME+" (#{device}) where id= #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(DeviceDto device);
}
