package com.runjian.domain.dto.commder;

import com.runjian.entity.DeviceChannelEntity;
import lombok.Data;

import java.util.List;

/**
 * @author chenjialing
 */
@Data
public class ChannelInfoDto {

    private int errorCode = 0;

    private List<DeviceChannelEntity> channelList;
}
