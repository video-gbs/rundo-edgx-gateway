package com.runjian.media.dispatcher.mapper;

import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.media.dispatcher.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.media.dispatcher.conf.dao.SimpleUpdateExtendedLanguageDriver;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author chenjialing
 * @date 2022/11/29 11:36
 */
@Mapper
@Repository
public interface GatewayTaskMapper {
    String TABLE = "rundo_gateway_task";

    /**
     * 新增
     * @param gatewayTask
     * @return
     */
    @Insert("INSERT INTO "+TABLE+" (#{gatewayTask})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(GatewayTask gatewayTask);


    /**
     * 根据msg_id修改消息状态
     * @param gatewayTask
     * @return
     */
    @Update("UPDATE "+TABLE+" (#{gatewayTask}) where  status = 0 and id = #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int updateById(GatewayTask gatewayTask);

    /**
     * 进行msgId相关消息查询
     * @param msgId
     * @return
     */
    @Select("SELECT * FROM "+TABLE+" WHERE msg_id= #{msgId}")
    GatewayTask getOne(String msgId);

    /**
     * 进行msgId相关消息查询
     * @param businessKey
     * @return
     */
    @Select("SELECT * FROM "+TABLE+" WHERE business_key= #{businessKey} and status = 0 order by id desc  limit 1")
    GatewayTask getOneByBusinessKey(String businessKey);

    /**
     * 进行msgId相关消息查询
     * @param businessKeys
     * @return
     */
    @Select("<script> " +
            "SELECT * FROM "+TABLE+" WHERE status = 0 and business_key in "+
            " <foreach collection='businessKeys' item='item' open='(' separator=',' close=')'>'${item}'</foreach>" +
            "</script> ")
    List<GatewayTask> getListByBusinessKey(Set<String> businessKeys);

    /**
     * 获取过期消息数据处理
     * @param now
     * @return
     */
    @Select("SELECT * FROM "+TABLE+" WHERE status = 0 and created_at <= #{now} ")
    List<GatewayTask> getExpireListByBusinessKey(LocalDateTime now);

}
