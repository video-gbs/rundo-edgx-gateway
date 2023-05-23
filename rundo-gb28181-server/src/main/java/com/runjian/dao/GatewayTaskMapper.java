package com.runjian.dao;

import com.runjian.common.commonDto.Gateway.dto.GatewayTask;
import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.domain.dto.DeviceCompatibleDto;
import com.runjian.gb28181.bean.Device;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    @Update("UPDATE "+TABLE+" (#{gatewayTask}) where id = #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int updateById(GatewayTask gatewayTask);

    /**
     * 进行msgId相关消息查询
     * @param msgId
     * @return
     */
    @Select("SELECT * FROM "+TABLE+" WHERE msg_id= #{msgId}")
    GatewayTask getOne(String msgId);

}
