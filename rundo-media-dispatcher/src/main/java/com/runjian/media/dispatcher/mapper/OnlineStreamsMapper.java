package com.runjian.media.dispatcher.mapper;

import com.runjian.media.dispatcher.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.media.dispatcher.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.media.dispatcher.dto.entity.OnlineStreamsEntity;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线流列表
 * @author chenjialing
 */
@Mapper
@Repository
public interface OnlineStreamsMapper {
    String ONLINE_STREAMS = "rundo_online_streams";

    /**
     * 添加
     * @param onlineStreamsEntity
     * @return
     */
    @Insert("INSERT INTO "+ONLINE_STREAMS+" (#{onlineStreamsEntity})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(OnlineStreamsEntity onlineStreamsEntity);

    /**
     * 添加
     * @param onlineStreamsEntity
     * @return
     */
    @Update("UPDATE "+ONLINE_STREAMS+" (#{onlineStreamsEntity}) where id= #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(OnlineStreamsEntity onlineStreamsEntity);
    /**
     * 查询流信息
     * @param streamId
     * @return
     */
    @Select("select * from "+ONLINE_STREAMS+" where stream_id = #{streamId} limit 1")
    OnlineStreamsEntity selectOne(String streamId);


    /**
     * 查询流的信息
     * @param deviceId
     * @param channelId
     * @param mediaType
     * @return
     */
    @Select("select * from "+ONLINE_STREAMS+" where device_id = #{deviceId} and channel_id = #{channelId} and media_type = #{mediaType} limit 1")
    OnlineStreamsEntity selectOne(String deviceId, String channelId, Integer mediaType);

    /**
     * 流播放列表删除
     * @param streamId
     * @return
     */
    @Delete("delete from "+ONLINE_STREAMS+" where stream_id = #{streamId}")
    int deleteBystreamId(String streamId);

    /**
     * 流播放列表删除
     * @return
     */
    @Delete("delete from "+ONLINE_STREAMS)
    int deleteAll();

    /**
     * 流播放列表删除
     * @param streamIds
     * @return
     */
    @Delete(" <script>" +
            "delete from "+ONLINE_STREAMS+" WHERE stream_id not in "+
            " <foreach collection='streamIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
            " </script>")
    int deleteBystreamIdList(List<String> streamIds);

    /**
     * 查询流信息列表
     * @param mediaServerId
     * @return
     */
    @Select("select * from "+ONLINE_STREAMS+" where media_server_id = #{mediaServerId}")
    List<OnlineStreamsEntity> selectStreamsByMediaServerId(String mediaServerId);


    /**
     * 查询全部流信息列表
     * @return
     */
    @Select("select * from "+ONLINE_STREAMS)
    List<OnlineStreamsEntity> selectAll();
    /**
     * 查询流信息列表
     * @param streamIds
     * @return
     */
    @Select(" <script>" +
            "select * FROM "+ONLINE_STREAMS+" WHERE stream_id in "+
            " <foreach collection='streamIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
            " </script>"
    )
    List<OnlineStreamsEntity> selectStreamsByStreamIds(List<String> streamIds);

    /**
     * 查询流信息列表
     * @param streamIds
     * @param checkTime
     * @return
     */
    @Select(" <script>" +
            "select * FROM "+ONLINE_STREAMS+" WHERE created_at &lt; #{checkTime} and stream_id in "+
            " <foreach collection='streamIds' item='item' open='(' separator=',' close=')'>#{item}</foreach>" +
            " </script>"
    )
    List<OnlineStreamsEntity> selectStreamsByCheckTime(List<String> streamIds, LocalDateTime checkTime);
}
