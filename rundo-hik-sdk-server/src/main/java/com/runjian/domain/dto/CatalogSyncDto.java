package com.runjian.domain.dto;

import com.runjian.entity.DeviceChannelEntity;
import lombok.Data;

import java.util.List;

/**
 *
 * @author chenjialing
 */
@Data
public class CatalogSyncDto {

    private int total;
    private List<DeviceChannelEntity> channelDetailList;
    private int num;

}
