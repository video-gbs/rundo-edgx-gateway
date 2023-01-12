package com.runjian.media.dispatcher.zlm.mapper;

import com.runjian.media.dispatcher.zlm.dto.PlatformAccountRsp;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Mapper
@Repository
public interface ProjectManagementMapper {


    /**
     * 根据账户id获取账户信息
     * @param platformId
     * @return
     */
    @Select("select * from rundo_platform_account_info where platform_id=#{platformId}")
    PlatformAccountRsp getOne(@Param("platformId") String platformId);

}
