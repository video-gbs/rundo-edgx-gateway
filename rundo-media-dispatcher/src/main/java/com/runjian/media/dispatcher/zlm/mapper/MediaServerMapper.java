package com.runjian.media.dispatcher.zlm.mapper;

import com.runjian.media.dispatcher.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.media.dispatcher.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;


@Mapper
@Repository
public interface MediaServerMapper {

    String MEDIA_SERVER_TABLE_NAME = "rundo_media_server";

    @Insert("INSERT INTO "+MEDIA_SERVER_TABLE_NAME+" (#{mediaServerItem})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(MediaServerItem mediaServerItem);


    @Update("UPDATE "+MEDIA_SERVER_TABLE_NAME+" (#{mediaServerItem}) where id= #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(MediaServerItem mediaServerItem);

    @Update("UPDATE "+MEDIA_SERVER_TABLE_NAME+" (#{mediaServerItem}) where ip= #{ip} and httpPort=#{httpPort} and type=\"zlm\"")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int updateByHostAndPort(MediaServerItem mediaServerItem);

    @Select("SELECT * FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE id=#{id}")
    MediaServerItem queryOne(String id);

    @Select("SELECT * FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE  type=\"zlm\"")
    List<MediaServerItem> queryAll();

    @Delete("DELETE FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE id=#{id}")
    void delOne(String id);

    @Select("DELETE FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE ip=#{host} and httpPort=#{port} and type=\"zlm\"")
    void delOneByIPAndPort(String host, int port);

    @Delete("DELETE FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE default_server=1 and type=\"zlm\"")
    int delDefault();

    @Select("SELECT * FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE ip=#{host} and http_port=#{port} and type=\"zlm\"")
    MediaServerItem queryOneByHostAndPort(String host, int port);

    @Select("SELECT * FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE default_server=1 and type=\"zlm\"")
    MediaServerItem queryDefault();


    @Select("SELECT * FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE  id=#{id}")
    MediaServerItem queryOneDefault(String id);

    @Select("SELECT * FROM "+MEDIA_SERVER_TABLE_NAME+" WHERE type=\"zlm\" and status=1")
    MediaServerItem queryOneOnline();
}
