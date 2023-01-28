package com.runjian.dao;


import com.runjian.conf.dao.SimpleInsertExtendedLanguageDriver;
import com.runjian.conf.dao.SimpleUpdateExtendedLanguageDriver;
import com.runjian.common.commonDto.Gateway.dto.EdgeGatewayInfoDto;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author chenjialing
 */
@Mapper
@Repository
public interface GatewayInfoMapper {

    String GATEWAY_INFO_TABLE="rundo_gateway_info";

    /**
     * 获取网关配置信息
     * @return
     */
    @Select("select * from "+GATEWAY_INFO_TABLE)
    EdgeGatewayInfoDto getConfig();

    /**
     * 更新
     * @param edgeGatewayInfoDto
     * @return
     */
    @Update("update "+GATEWAY_INFO_TABLE+" (#{edgeGatewayInfoDto}) where id=#{id}")
    @Lang(SimpleUpdateExtendedLanguageDriver.class)
    int update(EdgeGatewayInfoDto edgeGatewayInfoDto);

    @Insert("insert into "+GATEWAY_INFO_TABLE+" (#{edgeGatewayInfoDto})")
    @Lang(SimpleInsertExtendedLanguageDriver.class)
    int add(EdgeGatewayInfoDto edgeGatewayInfoDto);
}
