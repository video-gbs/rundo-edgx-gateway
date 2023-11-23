package com.runjian.dao;

import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.gb28181.bean.DeviceAlarm;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用于存储设备的报警信息
 */
@Mapper
@Repository
public interface DeviceAlarmMapper {
    String TABLE_NAME = "rundo_device_alarm";
    @Insert("INSERT INTO "+TABLE_NAME+" (#{alarm})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
    int add(DeviceAlarm alarm);

}
