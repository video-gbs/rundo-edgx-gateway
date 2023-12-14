package com.runjian.domain.dto;

import com.runjian.gb28181.bean.DeviceChannel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author chenjialing
 */
@Data
public class CatalogMqSyncSubscribeDto {
    /**
     * 上线 ON
     * 离线 OFF
      * VLOST 视频丢失
      * DEFECT故障
      * ADD 增加
      * DEL 删除
      * UPDATE 更新
     */
    private String catalogEvent;
    private List<DeviceChannel> channelDetailList;

}
