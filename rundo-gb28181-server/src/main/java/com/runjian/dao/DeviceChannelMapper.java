package com.runjian.dao;

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


    @Select("SELECT * FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id=#{deviceId} and deleted = 0")
    List<DeviceChannel> queryUndeletedChannelsByDeviceId(String deviceId);
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
    @Update(" <script>" +
            "update  "+DEVICE_CHANNEL_TABLE_NAME+" SET status = 0  WHERE id in "+
            " <foreach collection='idList' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
            " </script>"
    )
    int cleanChannelsByChannelIdList(List<Long> idList);

    /**
     * 删除设备通道
     * @param deviceId
     * @return
     */
    @Delete("DELETE FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id=#{deviceId}")
    int cleanChannelsByDeviceId(String deviceId);

    /**
     * 删除设备通道
     * @param deviceId
     * @return
     */
    @Delete("DELETE FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id=#{deviceId} and channel_id = #{channelId}")
    int hardDeleteByDeviceId(String deviceId,String channelId);

    /**
     * 删除设备通道
     * @param deviceId
     * @return
     */
    @Delete("update "+DEVICE_CHANNEL_TABLE_NAME+" set deleted = 1 WHERE device_id=#{deviceId}")
    int softDeleteByDeviceId(String deviceId);

    /**
     * 删除设备通道恢复
     * @param deviceId
     * @return
     */
    @Delete("update "+DEVICE_CHANNEL_TABLE_NAME+" set deleted = 0 WHERE device_id=#{deviceId}")
    int softDeleteRecoverByDeviceId(String deviceId);
    /**
     * 删除通道
     * @param deviceId
     * @param channelId
     * @return
     */
    @Delete("update "+DEVICE_CHANNEL_TABLE_NAME+" set deleted = 1 WHERE device_id=#{deviceId} and channel_id = #{channelId}")
    int softDeleteByDeviceIdAndChannelId(String deviceId,String channelId);


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

    /**
     * 查询通道信息
     * @param deviceId
     * @param channelId
     * @return
     */
    @Select("SELECT * FROM "+DEVICE_CHANNEL_TABLE_NAME+" WHERE device_id=#{deviceId} and channel_id=#{channelId} and deleted = 0")
    DeviceChannel queryChannelsByDeviceIdAndChannelId(String deviceId,String channelId);

    /**
     * 批量更新
     * @param updateChannels
     * @return
     */
    @Update({"<script>" +
            "<foreach collection='updateChannels' item='item' separator=';'>" +
            " UPDATE " + DEVICE_CHANNEL_TABLE_NAME +
            " SET channel_name=#{item.channelName}" +
            "<if test='item.manufacturer != null'>, manufacturer=#{item.manufacturer}</if>" +
            "<if test='item.model != null'>, model=#{item.model}</if>" +
            "<if test='item.owner != null'>, owner=#{item.owner}</if>" +
            "<if test='item.civilCode != null'>, civil_code=#{item.civilCode}</if>" +
            "<if test='item.block != null'>, block=#{item.block}</if>" +
            "<if test='item.parentId != null'>, parent_id=#{item.parentId}</if>" +
            "<if test='item.safetyWay != null'>, safety_way=#{item.safetyWay}</if>" +
            "<if test='item.registerWay != null'>, register_way=#{item.registerWay}</if>" +
            "<if test='item.certNum != null'>, cert_num=#{item.certNum}</if>" +
            "<if test='item.certifiable != null'>, certifiable=#{item.certifiable}</if>" +
            "<if test='item.errCode != null'>, err_code=#{item.errCode}</if>" +
            "<if test='item.endTime != null'>, end_time=#{item.endTime}</if>" +
            "<if test='item.secrecy != null'>, secrecy=#{item.secrecy}</if>" +
            "<if test='item.ipAddress != null'>, ip_address=#{item.ipAddress}</if>" +
            "<if test='item.port != null'>, port=#{item.port}</if>" +
            "<if test='item.password != null'>, password=#{item.password}</if>" +
            "<if test='item.ptzType != null'>, ptz_Type=#{item.ptzType}</if>" +
            "<if test='item.status != null'>, status=#{item.status}</if>" +
            "<if test='item.longitude != null'>, longitude=#{item.longitude}</if>" +
            "<if test='item.latitude != null'>, latitude=#{item.latitude}</if>" +
            "<if test='item.parental != null'>, parental=#{item.parental}</if>" +
            "<if test='item.businessGroupId != null'>, business_group_id=#{item.businessGroupId}</if>" +
            "WHERE device_id=#{item.deviceId} AND channel_id=#{item.channelId}"+
            "</foreach>" +
            "</script>"})
    int batchUpdate(List<DeviceChannel> updateChannels);

}
