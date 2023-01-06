package com.runjian.domain.dto;

import com.runjian.gb28181.bean.DeviceChannel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author chenjialing
 */
@Data
public class CatalogMqSyncDto {

    private int total;
    private List<DeviceChannel> channelList;
    private int sucessTotal;

}
