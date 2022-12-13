package com.runjian.dao;

import com.runjian.domain.dto.DeviceCompatibleDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author chenjialing
 * @date 2022/11/29 11:36
 */
@Mapper
@Repository
public interface DeviceCompatibleMapper {
    String DEVICE_COMPATIBLE = "rundo_device_compatible";

    @Select("select * from "+DEVICE_COMPATIBLE+" where deleted=0")
    List<DeviceCompatibleDto> getAll();

    @Select("select * from "+DEVICE_COMPATIBLE+" where deleted=0 and device_id = #{deviceId} and type = #{type}")
    DeviceCompatibleDto getByDeviceId(String deviceId,int type);
}
