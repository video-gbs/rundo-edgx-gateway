package com.runjian.dao;

import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.conf.dao.SimpleUpdateExtendedLanguageDriver;
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

    String DEVICE_CHANNEL_TABLE_NAME = "rundo_device_channel";

    @Update(value = {"UPDATE "+DEVICE_CHANNEL_TABLE_NAME+" SET status=0 WHERE device_id=#{deviceId}"})
    void offlineByDeviceId(String deviceId);

    @Select("SELECT * FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id=#{deviceId}")
    List<DeviceChannel> queryChannelsByDeviceId(String deviceId);

    /**
     * 根据通道id删除相关通道信息
     * @param deviceIdList
     * @return
     */
    @Delete(" <script>" +
            "DELETE FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id in "+
            " <foreach collection='deviceIdList' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
            " </script>"
    )
    int cleanChannelsByDeviceIdList(List<String> deviceIdList);

    /**
     * 根据通道id删除相关通道信息
     * @param idList
     * @return
     */
    @Delete(" <script>" +
            "DELETE FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE id in "+
            " <foreach collection='idList' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
            " </script>"
    )
    int cleanChannelsByChannelIdList(List<Long> idList);

    /**
     * 删除设备通道
     * @param deviceId
     * @return
     */
    @Delete("DELETE FROM device_channel WHERE device_id=#{deviceId}")
    int cleanChannelsByDeviceId(String deviceId);
    /**
     * 批量进行数据添加
     * @param addChannels
     * @return
     */
    @Insert("<script> " +
                "insert into "+DEVICE_CHANNEL_TABLE_NAME+" " +
                "(channel_id, device_Id, channel_name, manufacturer, model, owner, civil_code, block, " +
                "  address, parental, parent_id, safety_way, register_way, cert_num, certifiable, err_code, secrecy, " +
                "  ip_address, port, password, ptz_type, status, longitude, latitude, business_group_id) " +
                "values " +
                "<foreach collection='addChannels' index='index' item='item' separator=','> " +
                "(#{item.channelId},#{item.deviceId},#{item.channelName},#{item.manufacturer},#{item.model}" +
                ",#{item.owner},#{item.civilCode},#{item.block},#{item.address},#{item.parental},#{item.parentId}" +
                ",#{item.safetyWay},#{item.registerWay},#{item.certNum},#{item.certifiable},#{item.errCode}" +
                ",#{item.secrecy},#{item.ipAddress},#{item.port},#{item.password},#{item.ptzType},#{item.status}" +
                ",#{item.longitude},#{item.latitude},#{item.businessGroupId}"+
                ") " +
                "</foreach> " +
            "</script> "
            )
    @Options(useGeneratedKeys = true,keyColumn = "id",keyProperty = "id")
    int batchAdd(List<DeviceChannel> addChannels);

    /**
     * 更新通道
     * @param deviceChannel
     * @return
     */
    @Update("update "+DEVICE_CHANNEL_TABLE_NAME+" (#{deviceChannel}) where id=#{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(DeviceChannel deviceChannel);

    @Select("SELECT * FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id=#{deviceId} and channel_id=#{channelId}")
    DeviceChannel queryChannelsByDeviceIdAndChannelId(String deviceId,String channelId);

}
