package com.runjian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.runjian.common.commonDto.Gateway.req.RecordInfoReq;
import com.runjian.common.config.response.CommonResponse;
import com.runjian.domain.dto.CatalogSyncDto;
import com.runjian.domain.dto.DeviceChannel;
import com.runjian.domain.dto.commder.RecordAllItem;
import com.runjian.entity.DeviceChannelEntity;

import java.util.List;

/**
 * @author chenjialing
 */
public interface IDeviceChannelService extends IService<DeviceChannelEntity> {

    /**
     * catlog查询结束后完全重写通道信息----流程优化
     * @param deviceId
     * @param deviceChannelList
     */
    boolean resetChannelsForcatalog(String deviceId, List<DeviceChannel> deviceChannelList);

    /**
     * 清空通道
     * @param deviceId
     */
    void cleanChannelsForDevice(String deviceId);

    /**
     * 获取一个通道
     * @param deviceId
     * @param channelId
     * @return
     */
    DeviceChannel getOne(String deviceId,String channelId);

    /**
     * 录像列表
     * @param recordInfoReq
     */
    RecordAllItem recordInfo(RecordInfoReq recordInfoReq);

    /**
     * 通道同步
     * @param id
     * @return
     */
    CommonResponse<CatalogSyncDto> channelSync(Long id);

    /**
     * 通道删除
     * channelDbId
     */
    void channelHardDelete(long channelDbId);

    /**
     * 软删除通道
     * channelDbId
     */
    void channelSoftDelete(long channelDbId);
}
