package com.runjian.media.dispatcher.zlm.mapper;

import com.runjian.media.dispatcher.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.media.dispatcher.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.media.dispatcher.zlm.dto.MediaServerItem;
import com.runjian.media.dispatcher.zlm.dto.dao.GatewayBind;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * 网关的绑定信息
 * @author chenjialing
 */
@Mapper
@Repository
public interface GatewayBindMapper {
    String GATEWAY_BIND_TABLE_NAME = "rundo_gateway_bind";

    /**
     * 添加
     * @param gatewayBind
     * @return
     */
    @Insert("INSERT INTO "+GATEWAY_BIND_TABLE_NAME+" (#{gatewayBind})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(GatewayBind gatewayBind);

    /**
     * 编辑
     * @param gatewayBind
     * @return
     */
    @Update("UPDATE "+GATEWAY_BIND_TABLE_NAME+" (#{gatewayBind}) where id= #{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(GatewayBind gatewayBind);

    /**
     * 通过网关id获取信息
     * @param gatewayId
     * @return
     */
    @Select("SELECT * FROM "+GATEWAY_BIND_TABLE_NAME+" WHERE gateway_id=#{gatewayId}")
    GatewayBind queryOneByGatewayId(String gatewayId);

}
